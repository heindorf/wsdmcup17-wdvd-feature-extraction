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

import java.lang.reflect.Field;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.dumpfiles.MwDumpFormatException;
import org.wikidata.wdtk.dumpfiles.MwRevisionDumpFileProcessor;
import org.wikidata.wdtk.dumpfiles.MwRevisionProcessor;


public class ExtendedMwRevisionDumpFileProcessor
		extends MwRevisionDumpFileProcessor {

	static final Logger logger =
			LoggerFactory.getLogger(ExtendedMwRevisionDumpFileProcessor.class);

	static final String A_DELETED = "deleted";

	public ExtendedMwRevisionDumpFileProcessor(
			MwRevisionProcessor mwRevisionProcessor) {
		super(mwRevisionProcessor);

		// A little hack to change the final field mwRevision
		try {
			setFinalField(
				this,
				this.getClass().getSuperclass().getDeclaredField("mwRevision"),
				new ExtendedMwRevisionImpl());
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	static void setFinalField(Object owner, Field field, Object newValue)
			throws Exception {
		field.setAccessible(true);
		field.set(owner, newValue);
	}

	// for parsing compressed revisions
	void setXMLReader(XMLStreamReader xmlReader) {
		this.xmlReader = xmlReader;
	}

	// In the dump file are xml elements of the form
	// <comment deleted="deleted" />
	// <text deleted="deleted" />
	//
	// WDTK cannot process them
	@Override
	void processXmlRevision() throws XMLStreamException, MwDumpFormatException {
		this.mwRevision.resetCurrentRevisionData();

		this.xmlReader.next(); // skip current start tag
		while (this.xmlReader.hasNext()) {
			switch (this.xmlReader.getEventType()) {

			case XMLStreamConstants.START_ELEMENT:
				switch (this.xmlReader.getLocalName()) {
				case MwRevisionDumpFileProcessor.E_REV_COMMENT:
					((ExtendedMwRevisionImpl) this.mwRevision).isCommentDeleted =
						(this.xmlReader.getAttributeValue(
								null, ExtendedMwRevisionDumpFileProcessor.A_DELETED)
						!= null);
					this.mwRevision.comment = this.xmlReader.getElementText();
					break;
				case MwRevisionDumpFileProcessor.E_REV_TEXT:
					((ExtendedMwRevisionImpl) this.mwRevision).isTextDeleted =
						(this.xmlReader.getAttributeValue(
								null, ExtendedMwRevisionDumpFileProcessor.A_DELETED)
						!= null);
					this.mwRevision.text = this.xmlReader.getElementText();
					break;
				case MwRevisionDumpFileProcessor.E_REV_TIMESTAMP:
					this.mwRevision.timeStamp = this.xmlReader.getElementText();
					break;
				case MwRevisionDumpFileProcessor.E_REV_FORMAT:
					this.mwRevision.format = this.xmlReader.getElementText();
					break;
				case MwRevisionDumpFileProcessor.E_REV_MODEL:
					this.mwRevision.model = this.xmlReader.getElementText();
					break;
				case MwRevisionDumpFileProcessor.E_REV_CONTRIBUTOR:
					processXmlContributor();
					break;
				case MwRevisionDumpFileProcessor.E_REV_ID:
					this.mwRevision.revisionId = Long.valueOf(
							this.xmlReader.getElementText());
					break;
				case MwRevisionDumpFileProcessor.E_REV_PARENT_ID:
					((ExtendedMwRevisionImpl) this.mwRevision).parentId =
						this.xmlReader.getElementText();
					break;
				case MwRevisionDumpFileProcessor.E_REV_SHA1:
					((ExtendedMwRevisionImpl) this.mwRevision).sha1 =
						this.xmlReader.getElementText();
					break;
				case MwRevisionDumpFileProcessor.E_REV_MINOR:
					((ExtendedMwRevisionImpl) this.mwRevision).isMinor = true;
					break;
				default:
					throw new MwDumpFormatException("Unexpected element \""
							+ this.xmlReader.getLocalName() + "\" in revision.");
				}

				break;

			case XMLStreamConstants.END_ELEMENT:
				if (MwRevisionDumpFileProcessor.E_PAGE_REVISION
						.equals(this.xmlReader.getLocalName())) {
					this.mwRevisionProcessor.processRevision(this.mwRevision);
					return;
				}
				break;
			}

			this.xmlReader.next();
		}
	}
}
