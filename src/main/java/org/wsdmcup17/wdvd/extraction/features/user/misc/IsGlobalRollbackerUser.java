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

public class IsGlobalRollbackerUser extends FeatureImpl {

	static final UserSet userSet;
	static {

		// Taken from
		// http://meta.wikimedia.org/w/index.php?title=Special%3AGlobalUsers&username=&group=global-rollbacker&limit=500
		// last updated: January 26, 2016
		String[] globalRollbackerNames = { "-revi", ".snoopy.", "Addihockey10",
				"Ah3kal", "Alan", "Aldnonymous", "Alison", "Atcovi",
				"Avicennasis", "Az1568", "Baiji", "Beetstra", "Bencmq",
				"Billinghurst", "Biplab Anand", "Church of emacs", "Courcelles",
				"Dalibor Bosits", "Defender", "Deu", "EdBever", "Erwin",
				"Eurodyne", "Ezarate", "Fabexplosive", "Finnrind", "Glaisher",
				"HakanIST", "Hazard-SJ", "Hercule", "Holder", "Hydriz",
				"Iluvatar", "Incnis Mrsi", "Infinite0694", "Jafeluv",
				"Jamesofur", "Jasper Deng", "Juliancolton", "Kanjy", "Krinkle",
				"Ks-M9", "Leyo", "LlamaAl", "Matiia", "Maximillion Pegasus",
				"Mercy", "Mike.lifeguard", "MoiraMoira", "Morphypnos",
				"Nastoshka", "NuclearWarfare", "PiRSquared17", "Restu20",
				"Rschen7754", "Rxy", "Seewolf", "Syum90", "Techman224",
				"Toto Azéro", "Uğurkent", "VasilievVV", "Vogone", "Waihorace",
				"Wiki13", "Xqt", "Ymblanter", "YourEyesOnly", "和平奮鬥救地球" };

		userSet = new UserSet(Arrays.asList(globalRollbackerNames));
	}

	@Override
	public FeatureBooleanValue calculate(Revision revision) {
		return new FeatureBooleanValue(
				userSet.strContains(revision.getContributor()));
	}

	public static boolean isGlobalRollbacker(String contributor) {
		return userSet.contains(contributor);
	}

}
