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

package org.wsdmcup17.wdvd.extraction.features.revision.misc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wsdmcup17.wdvd.extraction.features.FeatureImpl;
import org.wsdmcup17.wdvd.extraction.features.FeatureStringValue;
import org.wsdmcup17.wdvd.extraction.revision.implementation.ContentTypeImpl;
import org.wsdmcup17.wdvd.extraction.revision.interfaces.ContentTypeIndicator;
import org.wsdmcup17.wdvd.extraction.revision.interfaces.Revision;

public class ContentType extends FeatureImpl {

	static final Logger logger = LoggerFactory.getLogger(ContentType.class);

	@Override
	public FeatureStringValue calculate(Revision revision) {
		String result = "" + getContentTypeFromString(
				revision.getParsedComment().getAction1());

		return new FeatureStringValue(result);
	}

	public static ContentTypeIndicator getContentTypeFromString(String action) {
		ContentTypeIndicator result;

		if (action == null) {
			result = ContentTypeImpl.MISC;
		} else {
			switch (action) {
			case "wbcreateclaim":
			case "wbsetclaim":
			case "wbremoveclaims":
			case "wbsetclaimvalue":
			case "wbsetreference":
			case "wbremovereferences":
			case "wbsetqualifier":
			case "wbremovequalifiers":
				result = ContentTypeImpl.STATEMENT;
				break;
			case "wbsetsitelink":
			case "wbcreateredirect":
			case "clientsitelink":
			case "wblinktitles":
				result = ContentTypeImpl.SITELINK;
				break;
			case "wbsetaliases":
			case "wbsetdescription":
			case "wbsetlabel":
			case "wbsetlabeldescriptionaliases":
				result = ContentTypeImpl.TEXT;
				break;
			case "wbeditentity":
			case "wbsetentity":
			case "special":
			case "wbcreate":
			case "wbmergeitems":
			case "rollback":
			case "undo":
			case "restore":
			case "pageCreation":
			case "emptyComment":
			case "setPageProtection":
			case "changePageProtection":
			case "removePageProtection":
			case "unknownCommentType":
			case "null":
			case "":
				result = ContentTypeImpl.MISC;
				break;
			default:
				logger.debug("Unknown content type of: " + action);
				result = ContentTypeImpl.MISC;
			}
		}
		return result;
	}

}
