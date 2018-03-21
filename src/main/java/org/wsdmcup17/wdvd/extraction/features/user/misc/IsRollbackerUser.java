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

public class IsRollbackerUser extends FeatureImpl {

	static final UserSet userSet;
	static {
		// source:
		// http://www.wikidata.org/w/index.php?title=Special:ListUsers&limit=500&username=&group=rollbacker&uselang=en
		// last updated: January 26, 2016
		String[] rollbackerNames = { "0x010C", "AFlorence", "Abián",
				"Aconcagua", "Aftabuzzaman", "Ahonc", "Amire80", "Andrew Gray",
				"Ankry", "Arash.pt", "Asqueladd", "Aude", "Avocato", "Ayack",
				"Base", "Bináris", "Bluemersen", "Brackenheim", "Byfserag",
				"Callanecc", "Cekli829", "ChongDae", "Chris troutman",
				"ChristianKl", "Chrumps", "Closeapple", "DangSunAlt",
				"Danmichaelo", "Danrok", "Darafsh", "David1010", "Denny",
				"Dereckson", "Deskana", "Dough4872", "Dusti", "Ebe123", "Emaus",
				"EoRdE6", "Epicgenius", "Espeso", "FDMS4", "Faux", "GZWDer",
				"Galaktos", "Geohakkeri", "GeorgeBarnick", "Gire 3pich2005",
				"Haplology", "Helgi-S", "Helmoony", "Holger1959", "IXavier",
				"Ibrahim.ID", "Indu", "Innocent bystander", "Irn",
				"It Is Me Here", "Ivanhercaz", "Izno", "Jarould",
				"Jasper Deng (alternate)", "Jayadevp13", "Jdx", "Jeblad",
				"Jianhui68", "Josve05a", "Jura1", "Kasir", "Kevinhksouth",
				"Kharkiv07", "Koavf", "KrBot", "Kwj2772", "Lingveno",
				"Liuxinyu970226", "LydiaPintscher", "M4r51n", "MZMcBride",
				"Magnus Manske", "Makecat", "Marek Mazurkiewicz", "Mateusz.ns",
				"Matiia", "Max Changmin", "Mediran", "Meisam", "Merlissimo",
				"Milad A380", "MisterSynergy", "Mjbmr", "Montgomery",
				"Morgankevinj", "Music1201", "MusikAnimal", "Máté", "NBS",
				"NahidSultan", "Namnguyenvn", "NatigKrolik", "Natuur12",
				"Nirakka", "Nojan", "Obaid Raza", "Osiris", "Palosirkka",
				"Petr Matas", "Petrb", "PinkAmpersand", "Poulpy", "Powerek38",
				"Pratyya Ghosh", "PublicAmpersand", "Py4nf", "Pzoxicuvybtnrm",
				"Rachmat04", "Razr nation old", "Reach Out to the Truth",
				"Reaper35", "Revi~wikidatawiki", "Rschen7754 public", "SHOTHA",
				"Samee", "Schniggendiller", "Scott5114", "Silvonen", "SimmeD",
				"Soap", "Stang", "Steinsplitter", "Strakhov", "Sumone10154",
				"Sunfyre", "Superchilum", "TBrandley", "TCN7JM", "TeleMania",
				"The Polish", "The Rambling Man", "Thibaut120094", "Tom Morris",
				"Totemkin", "Vacation9", "Vyom25", "WTM", "Wnme", "Wylve",
				"Yair rand", "Yamaha5", "Ypnypn", "Zerabat", "~riley",
				"Йо Асакура", "Красный", "آرش", "علاء", "محمد عصام" };

		userSet = new UserSet(Arrays.asList(rollbackerNames));
	}


	@Override
	public FeatureBooleanValue calculate(Revision revision) {
		return new FeatureBooleanValue(
				userSet.strContains(revision.getContributor()));
	}

	public static boolean isRollbacker(String contributor) {
		return userSet.contains(contributor);
	}

}
