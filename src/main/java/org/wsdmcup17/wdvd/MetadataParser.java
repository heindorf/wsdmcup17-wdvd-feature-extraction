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

package org.wsdmcup17.wdvd;

import java.io.IOException;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MetadataParser {

	private static final Logger
	LOG = LoggerFactory.getLogger(MetadataParser.class);

	private static final String
		UTF_8 = "UTF-8";

	public static final String
		REVISION_ID = "REVISION_ID",
		REVISION_SESSION_ID = "REVISION_SESSION_ID",
		USER_COUNTRY_CODE = "USER_COUNTRY_CODE",
		USER_CONTINENT_CODE = "USER_CONTINENT_CODE",
		USER_TIME_ZONE = "USER_TIME_ZONE",
		USER_REGION_CODE = "USER_REGION_CODE",
		USER_CITY_NAME = "USER_CITY_NAME",
		USER_COUNTY_NAME = "USER_COUNTY_NAME",
		REVISION_TAGS = "REVISION_TAGS";

	private static final String[]
		META_HEADER = {
			REVISION_ID,
			REVISION_SESSION_ID,
			USER_COUNTRY_CODE,
			USER_CONTINENT_CODE,
			USER_TIME_ZONE,
			USER_REGION_CODE,
			USER_CITY_NAME,
			USER_COUNTY_NAME,
			REVISION_TAGS
		};

	private static final CSVFormat
		CSV_FORMAT = CSVFormat.RFC4180.withHeader(META_HEADER);

	public static CSVRecord deserialize(byte[] bytes) throws IOException {
		String line = new String(bytes, UTF_8);
		return deserialize(line);
	}

	public static CSVRecord deserialize(String string) throws IOException {
		try {
			CSVParser parser = CSVParser.parse(string, CSV_FORMAT);

			List<CSVRecord> records = parser.getRecords();

			// CSV header contained in records?
			if (records.size() > 1) {
				CSVRecord record = records.get(0);
				checkHeader(record);

				return records.get(1);
			} else {
				return records.get(0);
			}
		} catch (Throwable e) {
			LOG.error("Unable to parse \"" + string + "\"", e);
			throw e;
		}
	}

	private static void checkHeader(CSVRecord record) {
		for (int i = 0; i < META_HEADER.length; i++) {
			if (!record.get(i).equals(META_HEADER[i])) {
				throw new RuntimeException("Invalid CSV Header");
			}
		}
	}
}
