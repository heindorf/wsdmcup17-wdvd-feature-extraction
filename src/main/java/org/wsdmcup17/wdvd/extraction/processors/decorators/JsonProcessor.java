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

package org.wsdmcup17.wdvd.extraction.processors.decorators;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.helpers.DatamodelConverter;
import org.wikidata.wdtk.datamodel.implementation.DataObjectFactoryImpl;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.json.jackson.JacksonItemDocument;
import org.wikidata.wdtk.datamodel.json.jackson.JacksonTermedStatementDocument;
import org.wsdmcup17.wdvd.extraction.processors.AbstractRevisionProcessor;
import org.wsdmcup17.wdvd.extraction.processors.RevisionProcessor;
import org.wsdmcup17.wdvd.extraction.revision.implementation.RevisionImpl;
import org.wsdmcup17.wdvd.extraction.revision.interfaces.Revision;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

// executed in parallel by several threads
public class JsonProcessor extends AbstractRevisionProcessor {

	// Initialize ObjectMapper only once to improve performance
	// see http://wiki.fasterxml.com/JacksonBestPracticesPerformance
	static final ObjectMapper newFormatMapper = new ObjectMapper();

	static final boolean VERBOSE_EXCEPTION_LOGGING = false;

	private static final ObjectReader newFormatReader = newFormatMapper
			.reader(JacksonTermedStatementDocument.class)
			.with(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);

	static ObjectMapper redirectMapper = new ObjectMapper();

	// parsing problems
	private SummaryStatistics emptyJsonStatistics = new SummaryStatistics();
	private SummaryStatistics inconsistentJsonXMLStatistics = new SummaryStatistics();
	private SummaryStatistics jsonExceptionStatistics = new SummaryStatistics();
	private SummaryStatistics nullPointerExceptionStatistics = new SummaryStatistics();

	// parsing successfull
	private SummaryStatistics newJsonStatistics = new SummaryStatistics();
	private SummaryStatistics redirectStatistics = new SummaryStatistics();

	public JsonProcessor(RevisionProcessor processor, int number) {
		super(processor);
		this.logger = LoggerFactory.getLogger("" + JsonProcessor.class + number);
	}

	@Override
	public void startRevisionProcessing() {
		logger.info("Starting...");

		if (processor != null) {
			processor.startRevisionProcessing();
		}
	}

	@Override
	public void processRevision(Revision revision) {
		// Issue in the database dump: sometimes the text element is empty.
		// Those itemDocuments are discarded.
		if (revision.getText().equals("")) {
			emptyJsonStatistics.addValue(revision.getRevisionId());
			logger.debug("Empty text element: Revision " + revision.getRevisionId());
		} else {
			ParsingResult parsingResult;

			try {
				// Might throw a JSONException or NullPointerException (see below)
				parsingResult = parseJson(revision.getText());

				// Does the item document represent a redirect?
				// Those itemDocuments are discarded.
				if (parsingResult.jsonVersion == JsonVersion.REDIRECT) {
					redirectStatistics.addValue(revision.getRevisionId());
				} else {
					// Issue in the database dump: sometimes the item id in
					// the JSON contradicts the item id in the XML.
					// Those itemDocuments are discarded.
					int jsonItemId = RevisionImpl.getItemIdFromString(
							parsingResult.itemDocument.getItemId().getId());
					if (jsonItemId != revision.getItemId()) {
						inconsistentJsonXMLStatistics.addValue(
							revision.getRevisionId());
						logger.debug("Inconsistent JSON: "
								+ "Revision " + revision.getRevisionId()
								+ ": XML item id Q" + revision.getItemId()
								+" <-> JSON item id Q" + jsonItemId);
					} else {
						// Everything is fine: set this item document

						if (parsingResult.jsonVersion == JsonVersion.NEW) {
							newJsonStatistics.addValue(
								revision.getRevisionId());
						}

						revision.setItemDocument(parsingResult.itemDocument);
					}
				}

				// Issue in the database dump: sometimes the JSON contains an
				// invalid globe coordinate. Those itemDocuments are discarded.
			} catch (NullPointerException e) {
				nullPointerExceptionStatistics.addValue(revision.getRevisionId());

				logRevisionException(e, revision);

				// Issue in the database dump: sometimes the JSON in the text
				// element cannot be parsed. Those itemDocuments are discarded.
			} catch (JSONException e) {
				jsonExceptionStatistics.addValue(revision.getRevisionId());

				logRevisionException(e, revision);
			}

		}

		if (processor != null) {
			processor.processRevision(revision);
		}

	}

	@Override
	public void finishRevisionProcessing() {
		logger.debug("Starting to finish...");

		if (processor != null) {
			processor.finishRevisionProcessing();
		}

		logger.info(
				"Revisions with empty text element: "
				+ emptyJsonStatistics.getN());
		logger.info(
				"Revisions with inconsistency (JSON <-> XML): "
				+ inconsistentJsonXMLStatistics.getN());
		logger.info(
				"Revisions with JSONException: "
				+ jsonExceptionStatistics.getN());
		logger.info(
				"Revisions with NullPointerException: "
				+ nullPointerExceptionStatistics.getN());

		logger.info(
				"Revisions with new JSON format: "
				+ newJsonStatistics.getN());
		logger.info(
				"Revisions with redirects: "
				+ redirectStatistics.getN());

		logger.info("Finished.");
	}

	public void logRevisionException(Exception e, Revision revision) {
		String logMessage = e.getClass().getSimpleName() + ": " + e.getMessage() + "\n"
				+ revision.toString();

		if (VERBOSE_EXCEPTION_LOGGING) {
			logMessage += " \n" + revision.getText() + "\n" + e.toString();
		}

		logger.debug(logMessage);
	}


	/**
	 * Parses the JSON contained in the XML 'text' element and returns an item document.
	 * If the 'text' element represents a redirect, this method returns null.
	 * If the 'text' element cannot be parsed, a JSONException is thrown.
	 *
	 */
	public static ParsingResult parseJson(String text) throws JSONException {
		ParsingResult result;

		// try to read the new Json format
		try {
			JacksonItemDocument jacksonItemDocument = newFormatReader.readValue(text);
			jacksonItemDocument.setSiteIri(Datamodel.SITE_WIKIDATA);

			result = new ParsingResult(JsonVersion.NEW, jacksonItemDocument);
		} catch (Exception e) {
			// Is it a redirect? Then do not log the exception
			try {
				redirectMapper.readValue(text, JacksonRedirectDocument.class);

				result = new ParsingResult(JsonVersion.REDIRECT, null);
			} catch (Exception e2) {
				throw new JSONException(
						"Format could neither be parsed as JSON, nor as a redirect.", e);
			}
		}

		if (result.itemDocument != null) {
			// Convert from Jackson to Object representation which consumes less
			// memory and is easier to work with.
			DatamodelConverter converter = new DatamodelConverter(new DataObjectFactoryImpl());
			// Throws a NullPointerException in some rare circumstances
			// (if the globe coordinate is null)
			result.itemDocument = converter.copy(result.itemDocument);
		}

		return result;
	}

	public SummaryStatistics getEmptyJsonStatistics() {
		return emptyJsonStatistics;
	}

	public SummaryStatistics getInconsistentJsonXMLStatistics() {
		return inconsistentJsonXMLStatistics;
	}

	public SummaryStatistics getJsonExceptionStatistics() {
		return jsonExceptionStatistics;
	}

	public SummaryStatistics getNullPointerExceptionStatistics() {
		return nullPointerExceptionStatistics;
	}

	public SummaryStatistics getNewJsonStatistics() {
		return newJsonStatistics;
	}

	public SummaryStatistics getRedirectStatistics() {
		return redirectStatistics;
	}
}

enum JsonVersion { NEW, OLD, REDIRECT }

class ParsingResult {
	ItemDocument itemDocument;
	JsonVersion jsonVersion;

	public ParsingResult(JsonVersion jsonVersion, ItemDocument itemDocument) {
		this.jsonVersion = jsonVersion;
		this.itemDocument = itemDocument;
	}
}

class JSONException extends Exception {
	private static final long serialVersionUID = 1L;
	JSONException(String str) {
		super(str);
	}

	JSONException(String str, Exception e) {
		super(str, e);
	}
}
