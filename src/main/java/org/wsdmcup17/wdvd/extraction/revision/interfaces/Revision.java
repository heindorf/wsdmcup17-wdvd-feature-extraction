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

package org.wsdmcup17.wdvd.extraction.revision.interfaces;

import java.util.Date;
import java.util.Map;

import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.dumpfiles.MwRevision;
import org.wsdmcup17.wdvd.extraction.features.Feature;
import org.wsdmcup17.wdvd.extraction.features.FeatureValue;

public interface Revision extends MwRevision {

	ParsedComment getRawParsedComment();

	ParsedComment getParsedComment();

	long getSessionId();

	void setRollbackReverted(Revision revertingRevision);

	void setUndoRestoreReverted(Revision revertingRevision);

	void setSha1Reverted(Revision revertingRevision);

	void setDownloadedSha1Reverted(Revision revertingRevision);

	void setReverted(RevertMethod method, Revision revertingRevision);

	boolean hasBotContributor();

	boolean isMinor();

	void setSessionId(long sessionId);

	boolean wasRollbackReverted();

	boolean wasUndoRestoreReverted();

	boolean wasSha1Reverted();

	boolean wasDownloadedSha1Reverted();

	boolean wasReverted(RevertMethod method);

	void setRevisionTags(String tags);

	String getRevisionTags();

	int getItemId();

	Map<RevertMethod, Revision> getRevertingRevision();

	Revision getRevertingRevision(RevertMethod method);

	Date getDate();

	ContentTypeIndicator getContentType();

	ItemDocument getItemDocument();

	boolean contributorEquals(Revision revision);

	void setItemDocument(ItemDocument itemDocument);

	Map<Feature, FeatureValue> getFeatureValues();

	void setFeatureValue(Feature f, FeatureValue v);

	void setGeoInformation(GeoInformation geoInformation);

	public GeoInformation getGeoInformation();

	float getScore();

	void setScore(float score);

	TextRegex getTextRegex();
	void setTextRegex(TextRegex textRegex);

	TextRegex getPrevTextRegex();
	void setPrevTextRegex(TextRegex prevTextRegex);

}
