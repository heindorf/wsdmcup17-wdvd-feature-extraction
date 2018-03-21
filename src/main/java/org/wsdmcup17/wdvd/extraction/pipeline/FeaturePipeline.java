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

package org.wsdmcup17.wdvd.extraction.pipeline;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.wikidata.wdtk.dumpfiles.MwRevisionProcessor;
import org.wsdmcup17.wdvd.extraction.FeatureListFactory;
import org.wsdmcup17.wdvd.extraction.features.Feature;
import org.wsdmcup17.wdvd.extraction.processors.RevisionProcessor;
import org.wsdmcup17.wdvd.extraction.processors.controlflow.ParallelProcessor;
import org.wsdmcup17.wdvd.extraction.processors.controlflow.StartFinishProcessor;
import org.wsdmcup17.wdvd.extraction.processors.decorators.FeatureProcessor;
import org.wsdmcup17.wdvd.extraction.processors.decorators.JsonProcessor;
import org.wsdmcup17.wdvd.extraction.processors.decorators.PrevTextRegexProcessor;
import org.wsdmcup17.wdvd.extraction.processors.decorators.TextRegexProcessor;
import org.wsdmcup17.wdvd.extraction.processors.output.CsvFeatureWriter;
import org.wsdmcup17.wdvd.extraction.processors.preprocessing.ReceiveProcessor;
import org.wsdmcup17.wdvd.extraction.processors.preprocessing.SendProcessor;
import org.wsdmcup17.wdvd.extraction.processors.statistics.StatisticsProcessor;

public class FeaturePipeline implements Pipeline {
	static final int REGEX_THREADS = 12;
	static final boolean LANGUAGE_REGEX_ENABLE = true;

	StartFinishProcessor startFinishProcessor;

	ReceiveProcessor receiveProcessor;
	SendProcessor sendProcessor;

	public FeaturePipeline(File featureFile) {
		List<Feature> features = FeatureListFactory.getFeatures();

		sendProcessor = new SendProcessor();

		RevisionProcessor nextProcessor;

		nextProcessor = sendProcessor;

		nextProcessor = new CsvFeatureWriter(
				nextProcessor, featureFile, features);
		nextProcessor = new FeatureProcessor(nextProcessor, features);

		nextProcessor = new PrevTextRegexProcessor(nextProcessor);

		List<RevisionProcessor> parallelProcessorList = new ArrayList<>();
		for (int i = 0; i < REGEX_THREADS; i++) {
			RevisionProcessor textRegexProcessor =
					new TextRegexProcessor(null, LANGUAGE_REGEX_ENABLE);
			parallelProcessorList.add(textRegexProcessor);
		}
		nextProcessor = new ParallelProcessor(
				parallelProcessorList, null, nextProcessor, "textRegex");

		nextProcessor = new JsonProcessor(nextProcessor, 1);
		nextProcessor = new StatisticsProcessor(nextProcessor);

		startFinishProcessor = new StartFinishProcessor(nextProcessor);
		nextProcessor = startFinishProcessor;

		receiveProcessor = new ReceiveProcessor(
				nextProcessor, null);
	}

	@Override
	public void start() {
		startFinishProcessor.startRevisionProcessingExplicitly();
	}

	@Override
	public MwRevisionProcessor getFirstProcessor() {
		return receiveProcessor;
	}

	@Override
	public void set(
			BlockingQueue<CSVRecord> metaQueue, CSVPrinter resultPrinter) {
		receiveProcessor.setMetadataQueue(metaQueue);
		sendProcessor.setResultPrinter(resultPrinter);
	}

	@Override
	public void stop() {
		startFinishProcessor.finishRevisionProcessingExplicitly();
	}

	@Override
	public void flush() {
		startFinishProcessor.flush();
	}
}
