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

package org.wsdmcup17.wdvd.extraction.features.user.misc;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wsdmcup17.wdvd.extraction.features.FeatureBooleanValue;
import org.wsdmcup17.wdvd.extraction.features.FeatureImpl;
import org.wsdmcup17.wdvd.extraction.revision.interfaces.Revision;

public class IsLocalBureaucrat extends FeatureImpl {

	static final Logger logger =
			LoggerFactory.getLogger(IsLocalBureaucrat.class);

	static final UserSet userSet;

	static {
		List<String> botnames = null;
		try {
			// All bureaucrats active in the middle of 2016
			// (at the time of the validation/test set)
			// https://www.wikidata.org/wiki/Wikidata:Bureaucrats/Timeline
			final String FILE_NAME = "users/2017_04_03_AllLocalBureaucrats.txt";
			InputStream input =
					IsLocalBureaucrat.class.getClassLoader()
					.getResourceAsStream(FILE_NAME);
			if (input == null) {
				input = new FileInputStream("src/main/resources/" + FILE_NAME);
			}
			botnames =  IOUtils.readLines(input, StandardCharsets.UTF_8);
		} catch (IOException e) {
			logger.error("", e);
		}

		userSet = new UserSet(botnames);
	}

	@Override
	public FeatureBooleanValue calculate(Revision revision) {
		String contributor = revision.getContributor();

		boolean result = isLocalBureaucrat(contributor);

		return new FeatureBooleanValue(result);
	}

	public static boolean isLocalBureaucrat(String contributor) {
		return userSet.contains(contributor);
	}

}
