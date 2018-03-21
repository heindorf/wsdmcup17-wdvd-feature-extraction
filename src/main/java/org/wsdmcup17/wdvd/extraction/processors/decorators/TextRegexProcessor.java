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

import java.util.regex.Matcher;

import org.wsdmcup17.wdvd.extraction.features.word.ProportionOfLinksAdded;
import org.wsdmcup17.wdvd.extraction.features.word.ProportionOfQidAdded;
import org.wsdmcup17.wdvd.extraction.features.word.misc.ProportionOfLanguageAdded;
import org.wsdmcup17.wdvd.extraction.processors.AbstractRevisionProcessor;
import org.wsdmcup17.wdvd.extraction.processors.RevisionProcessor;
import org.wsdmcup17.wdvd.extraction.revision.implementation.TextRegexImpl;
import org.wsdmcup17.wdvd.extraction.revision.interfaces.Revision;
import org.wsdmcup17.wdvd.extraction.revision.interfaces.TextRegex;

public class TextRegexProcessor extends AbstractRevisionProcessor {

	private boolean matchLanguages;

	private Matcher languageMatcher;
	private Matcher linkMatcher;
	private Matcher qidMatcher;

	public TextRegexProcessor(RevisionProcessor processor, boolean matchLanguages) {
		super(processor);
		this.matchLanguages = matchLanguages;
	}

	@Override
	public void startRevisionProcessing() {
		logger.info("Starting...");

		// Instances of this (Pattern) class are immutable and are safe for use
		// by multiple concurrent threads. Instances of the Matcher class are
		// not safe for such use.
		languageMatcher = ProportionOfLanguageAdded.pattern.matcher("");
		linkMatcher = ProportionOfLinksAdded.pattern.matcher("");
		qidMatcher = ProportionOfQidAdded.pattern.matcher("");

		if (processor != null) {
			processor.startRevisionProcessing();
		}
	}

	@Override
	public void processRevision(Revision revision) {
		int numberOfLanguageWords = 0;
		if (matchLanguages) {
			numberOfLanguageWords =
					regexCount(revision.getText(), languageMatcher);
		}

		int numberOfLinks = regexCount(revision.getText(), linkMatcher);
		int numberOfQids = regexCount(revision.getText(), qidMatcher);

		TextRegex textRegex = new TextRegexImpl();

		textRegex.setNumberOfLanguageWords(numberOfLanguageWords);
		textRegex.setNumberOfLinks(numberOfLinks);
		textRegex.setNumberOfQids(numberOfQids);

		revision.setTextRegex(textRegex);

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

	// Matcher is not thread safe and should only be used within one thread
	private static int regexCount(String str, Matcher matcher) {
		int count = 0;

		if (str != null) {
			matcher.reset(str);
			while (matcher.find()) {
				count++;
			}
		}

		return count;
	}

}
