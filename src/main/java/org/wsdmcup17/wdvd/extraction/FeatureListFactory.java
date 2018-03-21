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

package org.wsdmcup17.wdvd.extraction;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wsdmcup17.wdvd.extraction.features.Feature;
import org.wsdmcup17.wdvd.extraction.features.character.AlphanumericRatio;
import org.wsdmcup17.wdvd.extraction.features.character.AsciiRatio;
import org.wsdmcup17.wdvd.extraction.features.character.BracketRatio;
import org.wsdmcup17.wdvd.extraction.features.character.DigitRatio;
import org.wsdmcup17.wdvd.extraction.features.character.LatinRatio;
import org.wsdmcup17.wdvd.extraction.features.character.LongestCharacterSequence;
import org.wsdmcup17.wdvd.extraction.features.character.LowerCaseRatio;
import org.wsdmcup17.wdvd.extraction.features.character.NonLatinRatio;
import org.wsdmcup17.wdvd.extraction.features.character.PunctuationRatio;
import org.wsdmcup17.wdvd.extraction.features.character.UpperCaseRatio;
import org.wsdmcup17.wdvd.extraction.features.character.WhitespaceRatio;
import org.wsdmcup17.wdvd.extraction.features.item.LogCumItemUniqueUsers;
import org.wsdmcup17.wdvd.extraction.features.item.misc.IsHuman;
import org.wsdmcup17.wdvd.extraction.features.item.misc.IsLivingPerson;
import org.wsdmcup17.wdvd.extraction.features.item.misc.NumberOfAliases;
import org.wsdmcup17.wdvd.extraction.features.item.misc.NumberOfBadges;
import org.wsdmcup17.wdvd.extraction.features.item.misc.NumberOfDescriptions;
import org.wsdmcup17.wdvd.extraction.features.item.misc.NumberOfLabels;
import org.wsdmcup17.wdvd.extraction.features.item.misc.NumberOfProperties;
import org.wsdmcup17.wdvd.extraction.features.item.misc.NumberOfQualifiers;
import org.wsdmcup17.wdvd.extraction.features.item.misc.NumberOfReferences;
import org.wsdmcup17.wdvd.extraction.features.item.misc.NumberOfSitelinks;
import org.wsdmcup17.wdvd.extraction.features.item.misc.NumberOfStatements;
import org.wsdmcup17.wdvd.extraction.features.meta.CommentTail;
import org.wsdmcup17.wdvd.extraction.features.meta.ItemId;
import org.wsdmcup17.wdvd.extraction.features.meta.RevisionId;
import org.wsdmcup17.wdvd.extraction.features.meta.RevisionSessionId;
import org.wsdmcup17.wdvd.extraction.features.meta.Timestamp;
import org.wsdmcup17.wdvd.extraction.features.meta.UserId;
import org.wsdmcup17.wdvd.extraction.features.meta.UserName;
import org.wsdmcup17.wdvd.extraction.features.revision.CommentLength;
import org.wsdmcup17.wdvd.extraction.features.revision.IsLatinLanguage;
import org.wsdmcup17.wdvd.extraction.features.revision.PositionWithinSession;
import org.wsdmcup17.wdvd.extraction.features.revision.RevisionAction;
import org.wsdmcup17.wdvd.extraction.features.revision.RevisionLanguage;
import org.wsdmcup17.wdvd.extraction.features.revision.RevisionPrevAction;
import org.wsdmcup17.wdvd.extraction.features.revision.RevisionSubaction;
import org.wsdmcup17.wdvd.extraction.features.revision.RevisionTags;
import org.wsdmcup17.wdvd.extraction.features.revision.misc.ContentType;
import org.wsdmcup17.wdvd.extraction.features.revision.misc.MinorRevision;
import org.wsdmcup17.wdvd.extraction.features.revision.misc.Param1;
import org.wsdmcup17.wdvd.extraction.features.revision.misc.Param3;
import org.wsdmcup17.wdvd.extraction.features.revision.misc.Param4;
import org.wsdmcup17.wdvd.extraction.features.revision.misc.RevisionHashTag;
import org.wsdmcup17.wdvd.extraction.features.revision.misc.RevisionSize;
import org.wsdmcup17.wdvd.extraction.features.sentence.CommentCommentSimilarity;
import org.wsdmcup17.wdvd.extraction.features.sentence.CommentLabelSimilarity;
import org.wsdmcup17.wdvd.extraction.features.sentence.CommentSitelinkSimilarity;
import org.wsdmcup17.wdvd.extraction.features.sentence.CommentTailLength;
import org.wsdmcup17.wdvd.extraction.features.statement.ItemValue;
import org.wsdmcup17.wdvd.extraction.features.statement.LiteralValue;
import org.wsdmcup17.wdvd.extraction.features.statement.Property;
import org.wsdmcup17.wdvd.extraction.features.statement.misc.DataType;
import org.wsdmcup17.wdvd.extraction.features.user.CumUserUniqueItems;
import org.wsdmcup17.wdvd.extraction.features.user.IsPrivilegedUser;
import org.wsdmcup17.wdvd.extraction.features.user.IsRegisteredUser;
import org.wsdmcup17.wdvd.extraction.features.user.UserCityName;
import org.wsdmcup17.wdvd.extraction.features.user.UserContinentCode;
import org.wsdmcup17.wdvd.extraction.features.user.UserCountryCode;
import org.wsdmcup17.wdvd.extraction.features.user.UserCountyName;
import org.wsdmcup17.wdvd.extraction.features.user.UserRegionCode;
import org.wsdmcup17.wdvd.extraction.features.user.UserTimeZone;
import org.wsdmcup17.wdvd.extraction.features.user.misc.IsAdminUser;
import org.wsdmcup17.wdvd.extraction.features.user.misc.IsAdvancedUser;
import org.wsdmcup17.wdvd.extraction.features.user.misc.IsBotUser;
import org.wsdmcup17.wdvd.extraction.features.user.misc.IsCuratorUser;
import org.wsdmcup17.wdvd.extraction.features.user.misc.UserSecondsSinceFirstRevision;
import org.wsdmcup17.wdvd.extraction.features.user.misc.UserSecondsSinceFirstRevisionRegistered;
import org.wsdmcup17.wdvd.extraction.features.word.BadWordRatio;
import org.wsdmcup17.wdvd.extraction.features.word.ContainsLanguageWord;
import org.wsdmcup17.wdvd.extraction.features.word.ContainsURL;
import org.wsdmcup17.wdvd.extraction.features.word.LanguageWordRatio;
import org.wsdmcup17.wdvd.extraction.features.word.LongestWord;
import org.wsdmcup17.wdvd.extraction.features.word.LowerCaseWordRatio;
import org.wsdmcup17.wdvd.extraction.features.word.ProportionOfLinksAdded;
import org.wsdmcup17.wdvd.extraction.features.word.ProportionOfQidAdded;
import org.wsdmcup17.wdvd.extraction.features.word.UpperCaseWordRatio;
import org.wsdmcup17.wdvd.extraction.features.word.misc.ProportionOfLanguageAdded;


public class FeatureListFactory {

	static final Logger logger = LoggerFactory.getLogger(FeatureListFactory.class);

	private FeatureListFactory() {

	}

	private static List<Feature> getFeatureListInternal() {
		List<Feature> l = new ArrayList<>();

		////////////////////////////////////////////////////////
		// Meta features (used for computing statistics,
		// character n-grams, bag-of-words model, ...)
		////////////////////////////////////////////////////////
		l.add(new RevisionId());
		l.add(new RevisionSessionId());
		l.add(new Timestamp());

		l.add(new UserId());
		l.add(new UserName());

		l.add(new ItemId());

		l.add(new ContentType());
		l.add(new CommentTail());

		////////////////////////////////////////////////////////
		// Character features
		////////////////////////////////////////////////////////
		l.add(new AlphanumericRatio());
		l.add(new AsciiRatio());
		l.add(new BracketRatio());
		l.add(new DigitRatio());
		l.add(new LatinRatio());
		l.add(new LongestCharacterSequence());
		l.add(new LowerCaseRatio());
		l.add(new NonLatinRatio());
		l.add(new PunctuationRatio());
		l.add(new UpperCaseRatio());
		l.add(new WhitespaceRatio());

		////////////////////////////////////////////////////////
		// Word features
		////////////////////////////////////////////////////////
		l.add(new BadWordRatio());
		l.add(new ContainsLanguageWord());
		l.add(new ContainsURL());
		l.add(new LanguageWordRatio());
		l.add(new LongestWord());
		l.add(new LowerCaseWordRatio());
		l.add(new ProportionOfLinksAdded());
		l.add(new ProportionOfQidAdded());
		l.add(new UpperCaseWordRatio());

		// Used by ORES baseline
		l.add(new ProportionOfLanguageAdded());

		////////////////////////////////////////////////////////
		// Sentence features
		////////////////////////////////////////////////////////
		l.add(new CommentCommentSimilarity());
		l.add(new CommentLabelSimilarity());
		l.add(new CommentSitelinkSimilarity());
		l.add(new CommentTailLength());

		////////////////////////////////////////////////////////
		// Statement features
		////////////////////////////////////////////////////////
		l.add(new ItemValue());
		l.add(new LiteralValue());
		l.add(new Property());

		// Misc features
		l.add(new DataType());

		////////////////////////////////////////////////////////
		// User features
		////////////////////////////////////////////////////////
		l.add(new CumUserUniqueItems());
		l.add(new IsPrivilegedUser());
		l.add(new IsRegisteredUser());
		l.add(new UserCityName());
		l.add(new UserContinentCode());
		l.add(new UserCountryCode());
		l.add(new UserCountyName());
		l.add(new UserName());
		l.add(new UserRegionCode());
		l.add(new UserTimeZone());

		// Used by ORES baseline
		l.add(new IsAdminUser());
		l.add(new IsAdvancedUser());
		l.add(new IsBotUser());
		l.add(new IsCuratorUser());
		l.add(new UserSecondsSinceFirstRevisionRegistered());
		l.add(new UserSecondsSinceFirstRevision());

		////////////////////////////////////////////////////////
		// Item features
		////////////////////////////////////////////////////////
		l.add(new LogCumItemUniqueUsers());

		// Used by ORES baseline
		l.add(new IsHuman());
		l.add(new IsLivingPerson());
		l.add(new NumberOfAliases());
		l.add(new NumberOfBadges());
		l.add(new NumberOfDescriptions());
		l.add(new NumberOfLabels());
		l.add(new NumberOfProperties());
		l.add(new NumberOfQualifiers());
		l.add(new NumberOfReferences());
		l.add(new NumberOfSitelinks());
		l.add(new NumberOfStatements());

		////////////////////////////////////////////////////////
		// Revision features
		////////////////////////////////////////////////////////
		l.add(new CommentLength());
		l.add(new IsLatinLanguage());
		l.add(new PositionWithinSession());
		l.add(new RevisionAction());
		l.add(new RevisionLanguage());
		l.add(new RevisionPrevAction());
		l.add(new RevisionSubaction());
		l.add(new RevisionTags());

		// Misc features
		l.add(new MinorRevision());
		l.add(new Param1());
		l.add(new Param3());
		l.add(new Param4());
		l.add(new RevisionHashTag());
		l.add(new RevisionSize());

		return l;
	}

	public static List<Feature> getFeatures() {
		List<Feature> featureList = getFeatureListInternal();

		featureList = removeDuplicates(featureList);

		return featureList;
	}

	private static List<Feature> removeDuplicates(List<Feature> list) {
		LinkedHashSet<Feature> linkedHashSet = new LinkedHashSet<>();

		for (Feature feature: list) {
			if (!linkedHashSet.contains(feature)) {
				linkedHashSet.add(feature);
			} else {
				logger.debug("Removing duplicate feature: " + feature.getName());
			}
		}

		List<Feature> result = new ArrayList<>(linkedHashSet);
		return result;

	}

}
