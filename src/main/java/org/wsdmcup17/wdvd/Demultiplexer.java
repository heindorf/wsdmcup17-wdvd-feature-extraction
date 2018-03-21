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
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;

/**
 * Thread to demultiplex revisions and meta data. The resulting revisions are
 * provided as an {@link OutputStream} that can, for example, be processed with
 * Wikidata Toolkit. The metadata is parsed and put in a queue for further
 * processing.
 */
public class Demultiplexer implements Runnable {

	private static final Logger
		LOG = Logger.getLogger(Demultiplexer.class);

	private static final String
		LOG_MSG_END_OF_ITEM_STREAM = "End of item stream.",
		LOG_MSG_MISSING_DATA_AFTER_METADATA = "Missing data after metadata %s";

	private BlockingQueue<CSVRecord> metadataQueue;
	private DataInputStream dataStream;
	private PipedOutputStream revisionOutputStream;

	public Demultiplexer(
		DataInputStream inputStream, BlockingQueue<CSVRecord> metaQueue,
		PipedOutputStream revisionOutputStream
	) {
		this.dataStream = inputStream;
		this.metadataQueue = metaQueue;
		this.revisionOutputStream = revisionOutputStream;
	}

	@Override
	public void run() {
		try {
			demultiplexStream();
		} catch (Throwable e) {
			LOG.error("", e);
		}
	}

	private void demultiplexStream() throws IOException, InterruptedException {
		try {
			while (true) {
				// Read metadata from stream and queue it.
				CSVRecord metadata;
				byte[] bytes = readNextItem(dataStream);
				if (bytes == null) { // end of stream
					break;
				}

				metadata = MetadataParser.deserialize(bytes);
				metadataQueue.put(metadata);

				// Read corresponding revision from stream and forward it.
				bytes = readNextItem(dataStream);
				if (bytes == null) { // end of stream
					logMissingDataAfterMetadata(metadata);
					break;
				} else {
					revisionOutputStream.write(bytes);
					revisionOutputStream.flush();
				}
			}
		} finally {
			this.revisionOutputStream.close();
		}
	}

	private static byte[] readNextItem(DataInputStream dataStream)
	throws IOException {
		try {
			int length = dataStream.readInt();
			byte[] bytes = new byte[length];
			dataStream.readFully(bytes);
			return bytes;
		} catch (EOFException e) {
			LOG.info(LOG_MSG_END_OF_ITEM_STREAM);
			return null;
		}
	}

	private static void logMissingDataAfterMetadata(CSVRecord metadata) {
		LOG.error(String.format(LOG_MSG_MISSING_DATA_AFTER_METADATA, metadata));
	}
}
