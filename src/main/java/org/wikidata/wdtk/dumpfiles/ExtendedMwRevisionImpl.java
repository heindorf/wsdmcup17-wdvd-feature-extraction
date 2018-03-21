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

package org.wikidata.wdtk.dumpfiles;

import org.wikidata.wdtk.dumpfiles.MwRevision;
import org.wikidata.wdtk.dumpfiles.MwRevisionImpl;

public class ExtendedMwRevisionImpl extends MwRevisionImpl {

	String sha1;
	boolean isMinor;
	String parentId;
	boolean isCommentDeleted;
	boolean isTextDeleted;

	public ExtendedMwRevisionImpl() {
		super();
	}

	public ExtendedMwRevisionImpl(MwRevision mwRevision) {
		super(mwRevision);
		if (mwRevision instanceof ExtendedMwRevisionImpl) {
			this.sha1 =
					((ExtendedMwRevisionImpl) mwRevision).sha1;
			this.isMinor =
					((ExtendedMwRevisionImpl) mwRevision).isMinor;
			this.parentId =
					((ExtendedMwRevisionImpl) mwRevision).parentId;
			this.isCommentDeleted =
					((ExtendedMwRevisionImpl) mwRevision).isCommentDeleted;
			this.isTextDeleted =
					((ExtendedMwRevisionImpl) mwRevision).isTextDeleted;
		}
	}

	@Override
	void resetCurrentRevisionData() {
		super.resetCurrentRevisionData();
		sha1 = null;
		isMinor = false;
		parentId = null;
		isCommentDeleted = false;
		isTextDeleted = false;
	}



	public String getSHA1() {
		return sha1;
	}

	public boolean isMinor() {
		return isMinor;
	}

	public boolean isCommentDeleted() {
		return isCommentDeleted;
	}

	public boolean isTextDeleted() {
		return isTextDeleted;
	}

	public String getParentId() {
		return parentId;
	}


	@Override
	public String toString() {

		String textLength =
				(this.text != null) ? "" + this.text.length() : "null";

		// avoid null pointer exception
		return "Revision " + this.revisionId + " of page " + this.prefixedTitle
				+ " (ns " + this.namespace + ", id " + this.pageId
				+ "). Created at " + this.timeStamp + " by " + this.contributor
				+ " (" + this.contributorId + ") with comment \""
				+ this.comment + "\". Model " + this.model + " (" + this.format
				+ "). Text length: " + textLength;
	}

}
