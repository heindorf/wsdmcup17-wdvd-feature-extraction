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

package org.wsdmcup17.wdvd.extraction.processors.preprocessing;

import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.dumpfiles.MwRevision;
import org.wikidata.wdtk.dumpfiles.MwRevisionProcessor;
import org.wsdmcup17.wdvd.MetadataParser;
import org.wsdmcup17.wdvd.extraction.processors.RevisionProcessor;
import org.wsdmcup17.wdvd.extraction.revision.implementation.GeoInformationImpl;
import org.wsdmcup17.wdvd.extraction.revision.implementation.RevisionImpl;
import org.wsdmcup17.wdvd.extraction.revision.interfaces.GeoInformation;
import org.wsdmcup17.wdvd.extraction.revision.interfaces.Revision;

public class ReceiveProcessor implements MwRevisionProcessor {

	static final Logger logger = LoggerFactory.getLogger(ReceiveProcessor.class);

	private BlockingQueue<CSVRecord> metadataQueue;

	private RevisionProcessor processor;

	public ReceiveProcessor(
			RevisionProcessor processor, BlockingQueue<CSVRecord> metaQueue) {
		this.processor = processor;
		this.metadataQueue = metaQueue;
	}

	public void setMetadataQueue(BlockingQueue<CSVRecord> metadataQueue) {
		this.metadataQueue = metadataQueue;
	}

	@Override
	public void startRevisionProcessing(
		String siteName, String baseUrl, Map<Integer, String> namespaces
	) {
		logger.info("Starting...");
		processor.startRevisionProcessing();
	}

	@Override
	public void processRevision(MwRevision mwRevision) {
		// Retrieve corresponding metadata from metadata queue.
		CSVRecord metadata = getMetadata();

		Revision revision = new RevisionImpl(mwRevision);

		if (Long.valueOf(metadata.get(MetadataParser.REVISION_ID))
				!= revision.getRevisionId()) {
			throw new RuntimeException("Revision id is out of sync");
		}

		revision.setSessionId(
				Long.valueOf(metadata.get(MetadataParser.REVISION_SESSION_ID)));

		GeoInformation geoInformation = new GeoInformationImpl(
				-1,
				-1,
				metadata.get(MetadataParser.USER_COUNTRY_CODE),
				metadata.get(MetadataParser.USER_CONTINENT_CODE),
				metadata.get(MetadataParser.USER_TIME_ZONE),
				metadata.get(MetadataParser.USER_REGION_CODE),
				metadata.get(MetadataParser.USER_CITY_NAME),
				metadata.get(MetadataParser.USER_COUNTY_NAME));

		revision.setGeoInformation(geoInformation);

		revision.setRevisionTags(
				metadata.get(MetadataParser.REVISION_TAGS));

		processor.processRevision(revision);

	}

	private CSVRecord getMetadata() {
		try {
			return metadataQueue.take();
		} catch (InterruptedException e) {
			logger.error("", e);
			return null;
		}
	}

	@Override
	public void finishRevisionProcessing() {
		logger.debug("Starting to finish...");
		processor.finishRevisionProcessing();
		logger.info("Finished!");
	}

}
