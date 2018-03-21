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

public class IsGlobalAbuseFilterHelper extends FeatureImpl {

	static final Logger logger = LoggerFactory.getLogger(IsGlobalAbuseFilterHelper.class);

	static final UserSet userSet;

	static {
		List<String> botnames = null;
		try {
			// Downloaded on 2017-04-03
			// https://www.wikidata.org/w/index.php?title=Special%3AListUsers&username=&group=oversight&limit=500
			final String FILE_NAME = "users/2017_04_03_AllGlobalAbuseFilterHelpers.txt";
			InputStream input =
					IsGlobalAbuseFilterHelper.class.getClassLoader()
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

		boolean result = userSet.contains(contributor);

		return new FeatureBooleanValue(result);
	}

	public static boolean isGlobalAbuseFilterHelper(String contributor) {
		return userSet.contains(contributor);
	}

}
