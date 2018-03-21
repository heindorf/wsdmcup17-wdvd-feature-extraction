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

public class IsAdminUser extends FeatureImpl {

	static UserSet userSet;

	static {

		// source:
		// http://www.wikidata.org/w/index.php?title=Special%3AListUsers&username=&group=sysop
		// last updated: January 26, 2016
		String[] adminNames = { "-revi", "Ajraddatz", "AmaryllisGardener",
				"Andre Engels", "Andreasmperu", "Arkanosis", "Ash Crow",
				"Calak", "Caliburn", "Conny", "Courcelles", "Csigabi",
				"Delusion23", "Dexbot", "Ebrahim", "Epìdosis", "Eurodyne",
				"Fomafix", "HakanIST", "Harmonia Amanda", "Hazard-SJ",
				"Hoo man", "Jared Preston", "Jasper Deng", "Jianhui67",
				"Jon Harald Søby", "Ladsgroup", "LadyInGrey", "Lakokat",
				"Lymantria", "Matěj Suchánek", "Mbch331", "Multichill",
				"Mushroom", "Nikki", "Nikosguard", "Pamputt", "Pasleim",
				"Penn Station", "Pyb", "Rippitippi", "Romaine", "Rschen7754",
				"Rzuwig", "Sannita", "Sjoerddebruin", "Sotiale", "Stryn",
				"Taketa", "ValterVB", "Vogone", "Wagino 20100516", "Whym",
				"YMS", "Ymblanter", "Zolo", "יונה בנדלאק", "분당선M" };

		userSet = new UserSet(Arrays.asList(adminNames));
	}

	@Override
	public FeatureBooleanValue calculate(Revision revision) {
		return new FeatureBooleanValue(
				userSet.strContains(revision.getContributor()));
	}

	public static boolean isAdmin(String contributor) {
		return userSet.contains(contributor);
	}

}
