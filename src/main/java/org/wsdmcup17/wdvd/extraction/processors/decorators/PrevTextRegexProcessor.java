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

import org.wsdmcup17.wdvd.extraction.processors.AbstractRevisionProcessor;
import org.wsdmcup17.wdvd.extraction.processors.RevisionProcessor;
import org.wsdmcup17.wdvd.extraction.revision.interfaces.Revision;
import org.wsdmcup17.wdvd.extraction.revision.interfaces.TextRegex;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

// ATTENTION: This processor is not thread safe!
public class PrevTextRegexProcessor extends AbstractRevisionProcessor {

	Long2ObjectOpenHashMap<TextRegex> map = new Long2ObjectOpenHashMap<>();

	public PrevTextRegexProcessor(RevisionProcessor processor) {
		super(processor);
	}

	@Override
	public void startRevisionProcessing() {
		logger.info("Starting...");

		processor.startRevisionProcessing();
	}

	@Override
	public void processRevision(Revision revision) {
		// look up previous TextRegex
		long key = revision.getItemId();
		TextRegex prevTextRegex = map.get(key);
		revision.setPrevTextRegex(prevTextRegex);

		// set current TextRegex for next lookup
		TextRegex textRegex = revision.getTextRegex();
		map.put(key, textRegex);

		processor.processRevision(revision);
	}

	@Override
	public void finishRevisionProcessing() {
		logger.debug("Starting to finish...");

		processor.finishRevisionProcessing();

		logger.info("Finished.");
	}

}
