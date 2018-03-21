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

import java.util.Arrays;

import org.wsdmcup17.wdvd.extraction.features.FeatureBooleanValue;
import org.wsdmcup17.wdvd.extraction.features.FeatureImpl;
import org.wsdmcup17.wdvd.extraction.revision.interfaces.Revision;

public class IsGlobalStewardUser extends FeatureImpl {

	static final UserSet userSet;
	static {

		// Taken from
		// http://meta.wikimedia.org/w/index.php?title=Special%3AGlobalUsers&username=&group=steward&limit=500
		// last updated: January 26, 2016
		String[] stewardnames = { "Ajraddatz", "Avraham", "Barras", "Bennylin",
				"Bsadowski1", "DerHexer", "Einsbor", "Hoo man", "Jyothis",
				"Linedwell", "MBisanz", "MF-Warburg", "MarcoAurelio",
				"Mardetanha", "Masti", "Matanya", "Melos", "Mentifisto",
				"NahidSultan", "Pmlineditor", "QuiteUnusual", "RadiX",
				"Ruslik0", "Savh", "Shanmugamp7", "Stryn", "Tegel", "Teles",
				"Trijnstel", "Vituzzu" };

		userSet = new UserSet(Arrays.asList(stewardnames));
	}

	@Override
	public FeatureBooleanValue calculate(Revision revision) {
		return new FeatureBooleanValue(
				userSet.strContains(revision.getContributor()));
	}

	public static boolean isGlobalSteward(String contributor) {
		return userSet.contains(contributor);
	}

}
