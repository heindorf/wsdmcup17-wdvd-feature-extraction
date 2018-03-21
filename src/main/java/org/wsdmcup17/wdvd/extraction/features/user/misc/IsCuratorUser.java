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

import org.wsdmcup17.wdvd.extraction.features.FeatureBooleanValue;
import org.wsdmcup17.wdvd.extraction.features.FeatureImpl;
import org.wsdmcup17.wdvd.extraction.revision.interfaces.Revision;

public class IsCuratorUser extends FeatureImpl {

	@Override
	public FeatureBooleanValue calculate(Revision revision) {
		String contributor = revision.getContributor();
		if (contributor == null) {
			return new FeatureBooleanValue(null);
		}

		boolean result = isCuratorUser(contributor);

		return new FeatureBooleanValue(result);
	}

	/*
	 * Compare
	 *   - Sarabadani et al. 2017 and
	 *   - wb_vandalism/feature_lists/wikibase.py and
	 *   - revscoring/extractors/api/extractor.py
	 *   - https://www.wikidata.org/wiki/Special:ListUsers
	 *
	 * Curator: The user is a member of the
	 * "rollbacker", "abusefilter", "autopatrolled", or "reviewer" group
	 *
	 * Comments regarding reimplementation:
	 *   - ORES only checks local user groups
	 *   - There are no local abuse filter helpers in Wikidata as of 2016
	 *   - There are no local autopatrolled users in Wikidata as of 2016
	 *     (they are called "autoconfirmed" but ORES queries for "autopatrolled")
	 *   - There are no local reviewer users in Wikidata as of 2016
	 */
	public static boolean isCuratorUser(String contributor) {
		if (contributor == null) {
			return false;
		}

		boolean result = IsRollbackerUser.isRollbacker(contributor);

		return result;
	}

}
