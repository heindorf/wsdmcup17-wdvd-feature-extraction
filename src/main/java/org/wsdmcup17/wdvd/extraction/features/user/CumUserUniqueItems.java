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

package org.wsdmcup17.wdvd.extraction.features.user;

import java.util.HashMap;

import org.wsdmcup17.wdvd.extraction.features.FeatureImpl;
import org.wsdmcup17.wdvd.extraction.features.FeatureIntegerValue;
import org.wsdmcup17.wdvd.extraction.revision.interfaces.Revision;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class CumUserUniqueItems extends FeatureImpl {

	// Fastutil does not utilize the equals method for
	// reference-based types: http://fastutil.di.unimi.it/docs/overview-summary.html
	Object2ObjectOpenHashMap<String, LongSet> map =
			new Object2ObjectOpenHashMap<>();

	// HashMap using string equality
	HashMap<String, String> strMap = new HashMap<>();

	@Override
	public FeatureIntegerValue calculate(Revision revision) {
		// Make sure that same strings have the same reference
		String key = revision.getContributor();
		if (!strMap.containsKey(key)) {
			strMap.put(key, key);
		}
		key = strMap.get(key);

		if (!map.containsKey(key)) {
			map.put(key, new LongOpenHashSet());
		}

		LongSet itemIds = map.get(key);

		itemIds.add(revision.getItemId());

		int result = itemIds.size();

		return new FeatureIntegerValue(result);
	}

}
