/*
 * WSDM Cup 2017 Baselines
 *
 * Copyright (c) 2017 Stefan Heindorf, Martin Potthast, Gregor Engels, Benno Stein
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.wsdmcup17.wdvd;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Writer;
import java.net.ConnectException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;
import org.wikidata.wdtk.dumpfiles.DumpContentType;
import org.wikidata.wdtk.dumpfiles.ExtendedMwRevisionDumpFileProcessor;
import org.wikidata.wdtk.dumpfiles.MwDumpFile;
import org.wikidata.wdtk.dumpfiles.MwDumpFileProcessor;
import org.wikidata.wdtk.dumpfiles.MwLocalDumpFile;
import org.wikidata.wdtk.dumpfiles.MwRevisionProcessor;
import org.wsdmcup17.wdvd.extraction.pipeline.FeaturePipeline;
import org.wsdmcup17.wdvd.extraction.pipeline.Pipeline;

public class Client {

	private static final Logger
		LOG = Logger.getLogger(Client.class);

	private static final String
		LOG_MSG_CONNECTING_TO = "\n"
		+ "##################################################\n"
		+ "# Connecting to %s:%d\n"
		+ "##################################################",
		LOG_MSC_CONNECTION_EXCEPTION = "\n"
		+ "##################################################\n"
		+ "# Could not connect to %s:%d\n"
		+ "##################################################",
		CRLF = "\r\n",
		URI_PROTOCOL_TCP = "tcp://",
		THREAD_NAME_DEMULTIPLEXER = "Demultiplexer",
		MW_TOOLKIT_INPUT_STREAM = "INPUT STREAM";

	private static final String[]
		RESULT_CSV_HEADER = { "REVISION_ID", "VANDALISM_SCORE" };

	private static final CSVFormat
		CSV_FORMAT = CSVFormat.RFC4180.withHeader(RESULT_CSV_HEADER);

	private static final int
		PIPE_SIZE = 32 * 1024 * 1024,
		QUEUE_CAPACITY = 128;

	private String serverAddress;
	private String accessToken;
	private File featureFile;

	public Client(String serverAddress, String accessToken, File featureFile) {
		this.serverAddress = serverAddress;
		this.accessToken = accessToken;
		this.featureFile = featureFile;
	}

	public void start()
	throws URISyntaxException {
		URI uri = new URI(URI_PROTOCOL_TCP + serverAddress);
		String host = uri.getHost();
		int port = uri.getPort();

		Pipeline pipeline = new FeaturePipeline(featureFile);

		pipeline.start();

		try {
			while (true) {
				start2(pipeline, host, port);
				port = port + 1;
			}
		} catch (ConnectException e) {
			LOG.warn(String.format(LOG_MSC_CONNECTION_EXCEPTION, host, port));
		} catch (Throwable e) {
			LOG.error("", e);
		} finally {
			pipeline.stop();
		}
	}

	private void start2(Pipeline pipeline, String host, int port)
	throws UnknownHostException, IOException, InterruptedException {
		LOG.info(String.format(LOG_MSG_CONNECTING_TO, host, port));
		try (
			Socket socket = createSocket(host, port);
			// Multiplexed revision and metadata stream from server.
			InputStream dataStreamPlain = socket.getInputStream();
			// Result stream to server.
			OutputStream resultStreamPlain = socket.getOutputStream();
		) {
			// First send the access token to server to authenticate.
			resultStreamPlain.write((accessToken + CRLF).getBytes());
			resultStreamPlain.flush();

			// Assuming the token is accepted, proceed to process revisions.
			processRevisions(socket, pipeline, dataStreamPlain, resultStreamPlain);

		}

	}

	private static void processRevisions(
		final Socket socket,
		final Pipeline pipeline,
		final InputStream dataStreamPlain,
		final OutputStream resultStreamPlain
	) throws IOException, InterruptedException {
		// Wrap the streams for convenient usage.
		try (
			DataInputStream dataStream = new DataInputStream(dataStreamPlain);
			Writer resultWriter = new OutputStreamWriter(resultStreamPlain);
			CSVPrinter resultPrinter = new CSVPrinter(resultWriter, CSV_FORMAT);
		) {
			processRevisions(pipeline, dataStream, resultPrinter);

			pipeline.flush();
		}
	}

	private static void processRevisions(Pipeline pipeline,
		DataInputStream dataStream, CSVPrinter resultPrinter
	) throws IOException, InterruptedException {
		try (
			// Pipes to forward the revision stream to the revision processor.
			PipedOutputStream revisionOutputStream = new PipedOutputStream();
			PipedInputStream revisionInputStream =
				new PipedInputStream(revisionOutputStream, PIPE_SIZE);
		) {
			// Queue that stores metadata for revisions received from server.
			BlockingQueue<CSVRecord> metaQueue =
					new ArrayBlockingQueue<>(QUEUE_CAPACITY);

			// Thread that demultiplexes the data stream from the server,
			// writing revisions to the output stream and metadata to the queue.
			Thread demultiplexerThread = createDemultiplexerThread(
					dataStream, revisionOutputStream, metaQueue);

			// Event-driven revision processor based on Wikidata toolkit.
			pipeline.set(metaQueue, resultPrinter);

			MwDumpFileProcessor revisionProcessor = createRevisionProcessor(
					pipeline.getFirstProcessor());

			// Start processing revisions.
			// Note: the processor closes the stream and thus the socket.
			revisionProcessor.processDumpFileContents(
					revisionInputStream, createMwDumpFile());

			// Wait for the demultiplexer thread to terminate.
			demultiplexerThread.join();

			LOG.debug("Closing revision stream ...");
		}
	}

	private static MwDumpFileProcessor createRevisionProcessor(
		MwRevisionProcessor revisionProcessor
	) {
		return new ExtendedMwRevisionDumpFileProcessor(revisionProcessor);
	}

	private static Thread createDemultiplexerThread(
		DataInputStream dataStream,	PipedOutputStream revisionOutputStream,
		BlockingQueue<CSVRecord> metadataQueue
	) {
		Demultiplexer d = new Demultiplexer(
				dataStream, metadataQueue, revisionOutputStream);
		Thread demultiplexerThread = new Thread(d, THREAD_NAME_DEMULTIPLEXER);
		demultiplexerThread.start();
		return demultiplexerThread;
	}

	private static MwDumpFile createMwDumpFile() {
		MwLocalDumpFile mwDumpFile = new MwLocalDumpFile(
				MW_TOOLKIT_INPUT_STREAM, DumpContentType.FULL, null, null);
		mwDumpFile.prepareDumpFile();
		return mwDumpFile;
	}

	private static Socket createSocket(String host, int port)
	throws UnknownHostException, IOException {
		return new Socket(host, port);
	}
}
