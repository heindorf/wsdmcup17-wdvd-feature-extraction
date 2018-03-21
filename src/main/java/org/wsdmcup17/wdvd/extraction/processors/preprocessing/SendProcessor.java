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

package org.wsdmcup17.wdvd.extraction.processors.preprocessing;

import java.io.IOException;

import org.apache.commons.csv.CSVPrinter;
import org.wsdmcup17.wdvd.extraction.processors.AbstractRevisionProcessor;
import org.wsdmcup17.wdvd.extraction.revision.interfaces.Revision;

public class SendProcessor extends AbstractRevisionProcessor {

	private CSVPrinter resultPrinter;

	public SendProcessor() {
		super(null);
	}

	public void setResultPrinter(CSVPrinter resultPrinter) {
		logger.debug("Changing result printer ...");
		this.resultPrinter = resultPrinter;
	}

	@Override
	public void startRevisionProcessing() {
		logger.info("Starting...");
	}

	@Override
	public void processRevision(Revision revision) {
		sendClassificationScore(revision.getRevisionId(), revision.getScore());
	}

	@Override
	public void finishRevisionProcessing() {
		logger.debug("Starting to finish...");
		try {
			resultPrinter.close();
		} catch (IOException e) {
			logger.error("", e);
		}
		logger.info("Finished.");
	}


	private void sendClassificationScore(
			long revisionId, float classificationScore
		) {
			try {
				resultPrinter.print(revisionId);
				resultPrinter.print(classificationScore);
				resultPrinter.println();
				resultPrinter.flush();
			} catch (IOException e) {
				logger.error("", e);
				throw new RuntimeException(e);
			}
		}

}
