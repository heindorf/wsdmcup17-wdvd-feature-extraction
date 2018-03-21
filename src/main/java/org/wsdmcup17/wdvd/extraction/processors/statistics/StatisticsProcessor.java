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

package org.wsdmcup17.wdvd.extraction.processors.statistics;

import org.wsdmcup17.wdvd.extraction.processors.AbstractRevisionProcessor;
import org.wsdmcup17.wdvd.extraction.processors.RevisionProcessor;
import org.wsdmcup17.wdvd.extraction.revision.interfaces.Revision;

public class StatisticsProcessor extends AbstractRevisionProcessor {

	private static final String
		LOG_MSG_STARTING = "Starting...",
		LOG_MSG_CURRENT_STATUS = "Current status:",
		LOG_MSG_FINAL_RESULT = "Final result:",
		LOG_MSG_NUM_REVISIONS = "   Number of revisions: %s",
		LOG_MSG_NUM_REVISIONS_REGISTERED =
			"   Number of registered revisions: %s";

private static final int
	LOG_STATISTICS_INTERVAL = 10000;

	private long lastLogTime;
	private int numRevisions;
	private int numRevisionsFromRegisteredUsers;

	public StatisticsProcessor(RevisionProcessor processor) {
		super(processor);
	}

	@Override
	public void startRevisionProcessing() {
		logger.info(LOG_MSG_STARTING);
		lastLogTime = System.currentTimeMillis();

		processor.startRevisionProcessing();
	}

	@Override
	public void processRevision(Revision revision) {
		updateStatistics(revision);

		processor.processRevision(revision);
	}

	@Override
	public void finishRevisionProcessing() {
		logger.debug("Starting to finish...");

		processor.finishRevisionProcessing();

		logger.info(LOG_MSG_FINAL_RESULT);
		printStatistics();

		logger.info("Finished.");
	}

	private void updateStatistics(Revision revision) {
		numRevisions++;
		if (revision.hasRegisteredContributor()) {
			numRevisionsFromRegisteredUsers++;
		}
		long currentTime = System.currentTimeMillis();
		if (currentTime > lastLogTime + LOG_STATISTICS_INTERVAL) {
			logger.info(LOG_MSG_CURRENT_STATUS);
			printStatistics();
			lastLogTime = currentTime;
		}
	}

	private void printStatistics() {
		logger.info(String.format(LOG_MSG_NUM_REVISIONS, numRevisions));
		logger.info(String.format(LOG_MSG_NUM_REVISIONS_REGISTERED,
				numRevisionsFromRegisteredUsers));
	}
}
