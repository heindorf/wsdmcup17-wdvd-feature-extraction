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

package org.wsdmcup17.wdvd.extraction.revision.implementation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wsdmcup17.wdvd.extraction.revision.interfaces.ParsedComment;

/**
 * Parses the comment of a revision.
 *
 * For Wikibase comments, compare:
 *   https://github.com/wikimedia/mediawiki-extensions-Wikibase/blob/master/docs/summaries.txt
 *   https://github.com/wikimedia/mediawiki-extensions-Wikibase/blob/master/lib/includes/Summary.php
 *   https://github.com/wikimedia/mediawiki-extensions-Wikibase/blob/master/repo/includes/SummaryFormatter.php
 *
 *
 * For Wikimedia comments, compare:
 *    https://github.com/wikimedia/mediawiki/blob/master/languages/i18n/en.json
 *
 * Changes compared to CIKM-16 paper
 *   Hashtag Support:
 *   https://phabricator.wikimedia.org/T123529
 *   https://phabricator.wikimedia.org/T123636
 *
 *
 */
public class ParsedCommentImpl implements ParsedComment {
	static final Logger logger = LoggerFactory.getLogger(ParsedCommentImpl.class);

	static final Pattern ROBUST_ROLLBACK_PATTERN = Pattern.compile(
			".*\\bReverted\\s*edits\\s*by\\s*\\[\\[Special:Contributions\\/([^\\|\\]]*)\\|.*",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	static final Pattern PRECISE_ROLLBACK_PATTERN = Pattern.compile(
			"^Reverted edits by \\[\\[Special:Contributions\\/([^\\|\\]]*)\\|\\1\\]\\] \\(\\[\\[User talk:\\1\\|talk\\]\\]\\) to last revision by \\[\\[User:([^\\|\\]]*)\\|\\2\\]\\]$");
	static final Pattern ROBUST_UNDO_PATTERN =  Pattern.compile(
			".*\\b(Undo|Undid)\\b.*revision\\s*(\\d+).*",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	static final Pattern PRECISE_UNDO_PATTERN =  Pattern.compile(
			".*\\b(Undo|Undid) revision (\\d+) by \\[\\[Special:Contributions\\/([^|]*)\\|\\3\\]\\] \\(\\[\\[User talk:\\3\\|talk\\]\\]\\).*");
	static final Pattern ROBUST_RESTORE_PATTERN =  Pattern.compile(
			".*\\bRestored?\\b.*revision\\s*(\\d+).*",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	static final Pattern PRECISE_RESTORE_PATTERN =  Pattern.compile(
			".*\\bRestored? revision (\\d+) by \\[\\[Special:Contributions\\/([^|]*)\\|\\2\\]\\].*");

	String text;

	String action1;
	String action2;
	String[] parameters = new String[0];
	String suffixComment;
	String property;
	String dataValue;
	String itemValue;
	String hashTag;


	public ParsedCommentImpl(String comment) {
		this.text = comment;

		if (comment != null) {
			if (isRollback(comment)) {
				action1 = "rollback";
			} else if (isUndo(comment)) {
				action1 = "undo";
			} else if (isRestore(comment)) {
				action1 = "restore";
			} else if (isPageCreation(comment)) {
				action1 = "pageCreation";
			} else if ("".equals(comment)) {
				action1 = "emptyComment";
			} else if (isSetPageProtection(comment)) {
				action1 = "setPageProtection";
			} else if (isChangePageProtection(comment)) {
				action1 = "changePageProtection";
			} else if (isRemovePageProtection(comment)) {
				action1 = "removePageProtection";
			} else {
				boolean result = parseNormalComment(comment);

				if (result == false) {
					action1 = "unknownCommentType";
					logger.debug("unknown comment type: " + comment);
				}
			}
		}
	}

	// Parse a comment of the form /* action1-action2: param1, param2, ... */ value #hashtag
	// or of the form              /* action1 */ value #hashtag
	// @param comment
	// returns whether it is a normal comment, i.e., it contains /* ...*/
	private boolean parseNormalComment(String comment) {
		boolean result = false;

		int asteriskStart = comment.indexOf("/*");

		// Is there something of the form /* ... */?
		if (asteriskStart != -1) {
			result = true;


			int asteriskEnd = comment.indexOf("*/", asteriskStart);

			// Is the closing ... */ missing?
			// (The comment was shortened because it was too long)
			if (asteriskEnd == -1) {
				asteriskEnd = comment.length();
				suffixComment = "";
			} else {
				suffixComment = comment.substring(asteriskEnd + 2);
			}

			int colon = comment.indexOf(':');
			// denotes the end of action1 or action2 respectively
			int actionsEnd;
			if (colon != -1 && colon < asteriskEnd) {
				actionsEnd = colon;
			} else {
				actionsEnd = asteriskEnd;
			}

			int hyphenPos = comment.indexOf('-');

			// Does the action consist of two parts?
			if (hyphenPos > -1 && hyphenPos < actionsEnd) {
				action1 = comment.substring(asteriskStart + 3, hyphenPos);
				action2 = comment.substring(hyphenPos + 1, actionsEnd);
			} else {
				action1 = comment.substring(asteriskStart + 3, actionsEnd);
			}

			// Are there parameters?
			if (colon != -1 && colon < asteriskEnd) {
				String tmp = comment.substring(colon + 1, asteriskEnd);
				tmp = tmp.trim();
				parameters = tmp.split("\\|");
			}
		} else {
			suffixComment = comment;
		}

		// Is there a hashtag?
		int hashTagIndex = suffixComment.indexOf('#');
		if (hashTagIndex != -1) {
			hashTag = suffixComment.substring(hashTagIndex);
			suffixComment = suffixComment.substring(0, hashTagIndex);
			// remove trailing blank
			if (suffixComment.endsWith(" ")) {
				suffixComment =
						suffixComment.substring(
								0, suffixComment.length() - 1);
			}
			// remove trailing comma
			if (suffixComment.endsWith(",")) {
				suffixComment =
						suffixComment.substring(
								0, suffixComment.length() - 1);
			}
		}

		property = getProperty(suffixComment);
		dataValue = getDataValue(suffixComment);
		itemValue = getItemValue(suffixComment);


		action1 = trim(action1);
		action2 = trim(action2);

		for (int i = 0; i < parameters.length; i++) {
			parameters[i] = trim(parameters[i]);
		}

		return result;
	}


	private static String trim(String str) {
		String result = str;
		if (str != null) {
			result = str.trim();
		}
		return result;
	}

	public static boolean isRollback(String comment) {
		boolean result = false;

		if (comment != null) {
			String tmp = comment.trim();

			result = ROBUST_ROLLBACK_PATTERN.matcher(tmp).matches();

			if (result != PRECISE_ROLLBACK_PATTERN.matcher(tmp).matches()) {
				logger.debug(
						"Robust but not precise rollback match "
						+ "(result = " + result + ") : " + tmp);
			}
			if (result != tmp.startsWith("Reverted")) {
				logger.debug(
						"Difference to original rollback pattern: "
						+ "(result = " + result + ") : " + tmp);
			}
		}

		return result;
	}

	public static boolean isUndo(String comment) {
		boolean result = false;
		if (comment != null) {
			String tmp = comment.trim();

			result = ROBUST_UNDO_PATTERN.matcher(comment).matches();

			if (result != PRECISE_UNDO_PATTERN.matcher(tmp).matches()) {
				logger.debug(
						"Robust but not precise undo match"
						+ "(result = " + result + ") : " + tmp);
			}
		}

		return result;
	}

	public static boolean isRestore(String comment) {
		boolean result = false;

		if (comment != null) {
			String tmp = comment.trim();

			result = ROBUST_RESTORE_PATTERN.matcher(tmp).matches();

			if (result != PRECISE_RESTORE_PATTERN.matcher(tmp).matches()) {
				logger.debug(
						"Robust but not precise restore match"
						+ "(result = " + result + ") : " + tmp);
			}
		}

		return result;
	}

	public static boolean isPageCreation(String comment) {
		boolean result = false;

		if (comment != null) {
			String tmp = comment.trim();
			result = (tmp.startsWith("Created page"));
		}

		return result;
	}

	public static boolean isSetPageProtection(String comment) {
		boolean result = false;

		if (comment != null) {
			String tmp = comment.trim();
			result = tmp.startsWith("Protected");
		}

		return result;
	}

	public static boolean isRemovePageProtection(String comment) {
		boolean result = false;

		if (comment != null) {
			String tmp = comment.trim();
			result = tmp.startsWith("Removed protection");
		}

		return result;
	}

	public static boolean isChangePageProtection(String comment) {
		boolean result = false;

		if (comment != null) {
			String tmp = comment.trim();
			result = tmp.startsWith("Changed protection");
		}

		return result;
	}

	private static String getProperty(String comment) {
		String result = null;

		if (comment != null) {
			String pattern = "[[Property:";

			int index1 = comment.indexOf(pattern);
			int index2 = comment.indexOf("]]", index1 + pattern.length());

			if (index1 != -1 && index2 != -1) {
				result = comment.substring(index1 + pattern.length(), index2);
			}
		}

		return result;
	}

	private static String getDataValue(String comment) {
		String result = null;

		if (comment != null) {
			String antiPattern = "]]: [[Q";

			if (!comment.contains(antiPattern)) {
				String pattern = "]]: ";

				int index1 = comment.indexOf(pattern);

				if (index1 != -1) {
					result = comment.substring(index1 + pattern.length());
				}
			}
		}

		return result;
	}

	private static String getItemValue(String comment) {
		String result = null;

		if (comment != null) {
			String pattern = "]]: [[Q";

			int index1 = comment.indexOf(pattern);
			int index2 = comment.indexOf("]]", index1 + pattern.length());

			if (index1 != -1 && index2 != -1) {
				result = comment.substring(index1 + pattern.length(), index2);
			}
		}

		return result;
	}


	public static String getRevertedContributor(String comment) {
		String origResult = null;
		String pattern = "[[Special:Contributions/";
		int startIndex = comment.indexOf(pattern);
		int endIndex = comment.indexOf('|');
		if (endIndex > startIndex) {
			origResult =
					comment.substring(startIndex + pattern.length(), endIndex);
		}

		String result = "null";
		Matcher matcher = ROBUST_ROLLBACK_PATTERN.matcher(comment);
		if (matcher.matches()) {
			result = matcher.group(1);
		}

		if (!result.equals(origResult)) {
			logger.debug("Difference to original contributor: " + comment);
		}

		return result;
	}


	public static String getRevertedToContributor(String comment) {
		String result = "null";
		Matcher matcher = PRECISE_ROLLBACK_PATTERN.matcher(comment);
		if (matcher.matches()) {
			result = matcher.group(2);
		}
		return result;
	}

	public static long getUndoneRevisionId(String comment) {
		long result;

		Matcher matcher = ROBUST_UNDO_PATTERN.matcher(comment);
		if (matcher.matches()) {
			String str = matcher.group(2);
			result = Long.parseLong(str);
		} else {
			result = -1;
		}
		return result;
	}

	public static long getRestoredRevisionId(String comment) {
		long result;

		Matcher matcher = ROBUST_RESTORE_PATTERN.matcher(comment);
		if (matcher.matches()) {
			String str = matcher.group(1);
			result = Long.parseLong(str);
		} else {
			result = -1;
		}
		return result;
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public String getAction1() {
		return action1;
	}

	@Override
	public String getAction2() {
		return action2;
	}

	@Override
	public String[] getParameters() {
		return parameters;
	}

	@Override
	public String getSuffixComment() {
		return suffixComment;
	}

	@Override
	public String getHashTag() {
		return hashTag;
	}

	@Override
	public String getProperty() {
		return property;
	}

	@Override
	public String getDataValue() {
		return dataValue;
	}

	@Override
	public String getItemValue() {
		return itemValue;
	}
}
