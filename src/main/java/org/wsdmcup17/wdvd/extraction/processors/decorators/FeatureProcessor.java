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

package org.wsdmcup17.wdvd.extraction.processors.decorators;

import java.util.List;

import org.wsdmcup17.wdvd.extraction.features.Feature;
import org.wsdmcup17.wdvd.extraction.features.FeatureValue;
import org.wsdmcup17.wdvd.extraction.processors.AbstractRevisionProcessor;
import org.wsdmcup17.wdvd.extraction.processors.RevisionProcessor;
import org.wsdmcup17.wdvd.extraction.revision.interfaces.Revision;

public class FeatureProcessor extends AbstractRevisionProcessor {

	List<Feature> features;

	public FeatureProcessor(
			RevisionProcessor processor, List<Feature> features) {
		super(processor);
		this.processor = processor;
		this.features = features;
	}

	@Override
	public void startRevisionProcessing() {
		logger.info("Starting...");
		if (processor != null) {
			processor.startRevisionProcessing();
		}

	}

	@Override
	public void processRevision(Revision revision) {
		for (Feature feature: features) {
			FeatureValue value = feature.calculate(revision);
			revision.setFeatureValue(feature, value);
		}

		if (processor != null) {
			processor.processRevision(revision);
		}

	}

	@Override
	public void finishRevisionProcessing() {
		logger.debug("Starting to finish...");

		if (processor != null) {
			processor.finishRevisionProcessing();
		}
		logger.info("Finished.");
	}

}
