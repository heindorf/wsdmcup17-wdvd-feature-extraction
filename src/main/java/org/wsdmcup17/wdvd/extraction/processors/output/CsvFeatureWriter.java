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

package org.wsdmcup17.wdvd.extraction.processors.output;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.wsdmcup17.wdvd.extraction.features.Feature;
import org.wsdmcup17.wdvd.extraction.processors.AbstractRevisionProcessor;
import org.wsdmcup17.wdvd.extraction.processors.RevisionProcessor;
import org.wsdmcup17.wdvd.extraction.revision.interfaces.Revision;
import org.wsdmcup17.wdvd.extraction.streams.AsyncOutputStream;

public class CsvFeatureWriter extends AbstractRevisionProcessor {
	File featureFile;

	static final int BUFFER_SIZE = 1 * 1024 * 1024;
	static final int BZIP2_BLOCKSIZE = BZip2CompressorOutputStream.MIN_BLOCKSIZE;

	private List<Feature> features;

	FeatureCSVPrinter featurePrinter;

	OutputStream outputStream;

	public CsvFeatureWriter(
			RevisionProcessor processor, File featureFile, List<Feature> features) {
		super(processor);
		this.processor = processor;
		this.featureFile = featureFile;
		this.features = features;
	}

	@Override
	public void startRevisionProcessing() {
		logger.info("Starting (" + featureFile + ")...");

		try {
			outputStream =
					new AsyncOutputStream(
						new BZip2CompressorOutputStream(
							new BufferedOutputStream(
							new FileOutputStream(featureFile)),
							BZIP2_BLOCKSIZE),
						"Feature Writer Output Stream",
						BUFFER_SIZE);

			featurePrinter = new FeatureCSVPrinter(features, outputStream);

		} catch (IOException e) {
			logger.error("", e);
		}

		processor.startRevisionProcessing();
	}

	@Override
	public void processRevision(Revision revision) {
		try {
			featurePrinter.printFeatures(revision);

		} catch (IOException e) {
			logger.error("", e);
		}

		processor.processRevision(revision);
	}


	@Override
	public void finishRevisionProcessing() {
		logger.debug("Starting to finish...");

		processor.finishRevisionProcessing();

		try {
			featurePrinter.close();
			outputStream.close();
		} catch (IOException e) {
			logger.error("", e);
		}

		logger.info("Finished.");
	}

}
