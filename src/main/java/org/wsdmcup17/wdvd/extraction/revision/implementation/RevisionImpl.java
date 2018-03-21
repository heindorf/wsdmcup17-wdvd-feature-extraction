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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.dumpfiles.ExtendedMwRevisionImpl;
import org.wikidata.wdtk.dumpfiles.MwRevision;
import org.wsdmcup17.wdvd.extraction.features.Feature;
import org.wsdmcup17.wdvd.extraction.features.FeatureValue;
import org.wsdmcup17.wdvd.extraction.features.revision.misc.ContentType;
import org.wsdmcup17.wdvd.extraction.features.user.misc.IsBotUser;
import org.wsdmcup17.wdvd.extraction.revision.interfaces.ContentTypeIndicator;
import org.wsdmcup17.wdvd.extraction.revision.interfaces.GeoInformation;
import org.wsdmcup17.wdvd.extraction.revision.interfaces.ParsedComment;
import org.wsdmcup17.wdvd.extraction.revision.interfaces.RevertMethod;
import org.wsdmcup17.wdvd.extraction.revision.interfaces.Revision;
import org.wsdmcup17.wdvd.extraction.revision.interfaces.TextRegex;

public class RevisionImpl extends ExtendedMwRevisionImpl implements Revision {
	static final Logger logger = LoggerFactory.getLogger(Revision.class);

	private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssX";

	private ParsedComment parsedComment;
	private long sessionId;
	private ItemDocument itemDocument;
	private TextRegex textRegex;
	private TextRegex prevTextRegex;

	private String revisionTags;
	private GeoInformation geoInformation;

	private Map<Feature, FeatureValue> featureValues = new HashMap<>();

	private Map<RevertMethod, Revision> revertingRevision = new HashMap<>();

	private float score;

	public static final RevisionImpl DUMMY_REVISION = new RevisionImpl();

	public RevisionImpl() {
		super();
	}

	public RevisionImpl(MwRevision revision) {
		super(revision);
	}

	//TODO: Update this constructor if the attributes have changed
	@SuppressWarnings("unchecked")
	public RevisionImpl(Revision revision) {
		super(revision);
		this.parsedComment = revision.getRawParsedComment();
		this.sessionId = revision.getSessionId();
		this.revertingRevision = new HashMap<>();
		this.revertingRevision.putAll(
				revision.getRevertingRevision()); // shallow copy
		this.revisionTags = revision.getRevisionTags();
		this.geoInformation = revision.getGeoInformation();

		this.itemDocument = revision.getItemDocument();
		this.textRegex = revision.getTextRegex();
		this.prevTextRegex = revision.getPrevTextRegex();

		this.featureValues =
				((HashMap<Feature, FeatureValue>)
						((HashMap<Feature, FeatureValue>) revision.getFeatureValues())
						.clone()); //shallow copy
		this.score = revision.getScore();
	}

	@Override
	public ParsedComment getParsedComment() {
		// Lazy Generation
		if (parsedComment == null) {
			parsedComment = new ParsedCommentImpl(getComment());
		}

		return parsedComment;
	}

	@Override
	public ParsedComment getRawParsedComment() {
		return parsedComment;
	}

	@Override
	public long getSessionId() {
		return sessionId;
	}

	@Override
	public void setRollbackReverted(Revision revertingRevision) {
		this.revertingRevision.put(RevertMethodImpl.ROLLBACK, revertingRevision);
	}

	@Override
	public void setUndoRestoreReverted(Revision revertingRevision) {
		this.revertingRevision.put(RevertMethodImpl.UNDO_RESTORE, revertingRevision);
	}

	@Override
	public void setSha1Reverted(Revision revertingRevision) {
		this.revertingRevision.put(RevertMethodImpl.SHA1, revertingRevision);
	}

	@Override
	public void setDownloadedSha1Reverted(Revision revertingRevision) {
		this.revertingRevision.put(RevertMethodImpl.DOWNLOADED_SHA1, revertingRevision);
	}

	@Override
	public void setReverted(RevertMethod method, Revision revertingRevision) {
		this.revertingRevision.put(method, revertingRevision);
	}

	@Override
	public boolean hasBotContributor() {
		return IsBotUser.isBot(getContributor());
	}

	@Override
	public void setSessionId(long sessionId) {
		this.sessionId = sessionId;
	}

	@Override
	public boolean wasRollbackReverted() {
		return wasReverted(RevertMethodImpl.ROLLBACK);
	}

	@Override
	public boolean wasUndoRestoreReverted() {
		return wasReverted(RevertMethodImpl.UNDO_RESTORE);
	}

	@Override
	public boolean wasSha1Reverted() {
		return wasReverted(RevertMethodImpl.SHA1);
	}

	@Override
	public boolean wasDownloadedSha1Reverted() {
		return wasReverted(RevertMethodImpl.DOWNLOADED_SHA1);
	}

	@Override
	public boolean wasReverted(RevertMethod method) {
		return (revertingRevision.get(method) != null);
	}

	@SuppressWarnings("unused")
	private String getDumpSHA1() {
		return super.getSHA1();
	}

	@Override
	public void setRevisionTags(String revisionTags) {
		this.revisionTags = revisionTags;
	}

	@Override
	public String getRevisionTags() {
		return revisionTags;
	}

	@Override
	public int getItemId() {
		int result = -1;
		String title = getPrefixedTitle();
		if (title != null && title.startsWith("Q")) {
			title = title.substring(1);
		} else {
			logger.warn(
					"Revision: " + getRevisionId()
					+ ": non-well-formed prefixedTitle: " + title);
		}
		try {
			result = Integer.parseInt(title);
		} catch (NumberFormatException e) {
			logger.warn(
					"Revision: " + getRevisionId()
					+ ": non-well-formed prefixedTitle: " + title);
		}

		return result;
	}

	@Override
	public Revision getRevertingRevision(RevertMethod method) {
		return revertingRevision.get(method);
	}

	@Override
	public Map<RevertMethod, Revision> getRevertingRevision() {
		return revertingRevision;
	}

	@Override
	public Date getDate() {
		return getDate(getTimeStamp());
	}

	public static Date getDate(String timestamp) {
		Date result = null;

		try {
			// formatter is not thread safe
			DateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
			result = formatter.parse(timestamp);
		} catch (ParseException e) {
			logger.warn("Invalid time stamp: " + timestamp);
		}

		return result;
	}

	@Override
	public ContentTypeIndicator getContentType() {
		return ContentType.getContentTypeFromString(
				getParsedComment().getAction1());
	}

	@Override
	public boolean contributorEquals(Revision revision) {
		boolean notNull = (this.getContributor() != null)
				&& (revision != null);

		return notNull && this.getContributor().equals(revision.getContributor());
	}

	@Override
	public void setItemDocument(ItemDocument itemDocument) {
		this.itemDocument = itemDocument;
	}

	@Override
	public ItemDocument getItemDocument() {
		return itemDocument;
	}

	public static int getItemIdFromString(String str) {
		int result = -1;
		if (str != null && str.startsWith("Q")) {
			str = str.substring(1);
		} else {
			logger.warn(
					"Title: " + str
					+ ": non-well-formed item string: " + str);
		}
		try {
			result = Integer.parseInt(str);
		} catch (NumberFormatException e) {
			logger.warn(
					"Title: " + str +
					": non-well-formed item string: " + str);
		}

		return result;
	}

	@Override
	public void setFeatureValue(Feature f, FeatureValue v) {
		featureValues.put(f, v);
	}

	@Override
	public Map<Feature, FeatureValue> getFeatureValues() {
		return featureValues;
	}

	@Override
	public void setGeoInformation(GeoInformation geoInformation) {
		this.geoInformation = geoInformation;

	}

	@Override
	public GeoInformation getGeoInformation() {
		return this.geoInformation;
	}

	@Override
	public float getScore() {
		return this.score;
	}

	@Override
	public void setScore(float score) {
		this.score = score;
	}

	@Override
	public TextRegex getTextRegex() {
		return textRegex;
	}

	@Override
	public void setTextRegex(TextRegex textRegex) {
		this.textRegex = textRegex;
	}

	@Override
	public TextRegex getPrevTextRegex() {
		return prevTextRegex;
	}

	@Override
	public void setPrevTextRegex(TextRegex prevTextRegex) {
		this.prevTextRegex = prevTextRegex;
	}

}
