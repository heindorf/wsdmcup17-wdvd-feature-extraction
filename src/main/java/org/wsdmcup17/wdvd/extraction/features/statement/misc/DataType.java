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

package org.wsdmcup17.wdvd.extraction.features.statement.misc;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wsdmcup17.wdvd.extraction.features.FeatureImpl;
import org.wsdmcup17.wdvd.extraction.features.FeatureStringValue;
import org.wsdmcup17.wdvd.extraction.revision.interfaces.Revision;

public class DataType extends FeatureImpl {

	static final Logger logger = LoggerFactory.getLogger(DataType.class);

	static Map<String, String> datatypes = new HashMap<>();

	static {

		try {
			// Source: https://www.wikidata.org/wiki/Special:ListProperties/math?datatype=

			Map<String, String> files = new HashMap<>();
			files.put("2017_04_04_commonsMedia.txt", "commonsMedia");
			files.put("2017_04_04_external-id.txt", "external-id");
			files.put("2017_04_04_globe-coordinate.txt", "globe-coordinate");
			files.put("2017_04_04_math.txt", "math");
			files.put("2017_04_04_monolingualtext.txt", "monolingualtext");
			files.put("2017_04_04_quantity.txt", "quantity");
			files.put("2017_04_04_string.txt", "string");
			files.put("2017_04_04_time.txt", "time");
			files.put("2017_04_04_url.txt", "url");
			files.put("2017_04_04_wikibase-item.txt", "wikibase-item");
			files.put("2017_04_04_wikibase-property.txt", "wikibase-property");


			for (String filename: files.keySet()) {
				String filepath = "datatypes/" + filename;
				InputStream input =
						DataType.class.getClassLoader().getResourceAsStream(filepath);
				if (input == null) {
					input = new FileInputStream("src/main/resources/" + filepath);
				}
				List<String> properties =
						IOUtils.readLines(input, StandardCharsets.UTF_8);
				for (String property: properties) {
					String type = files.get(filename);
					if (type == null) {
						throw new IllegalStateException(
								"Wrong initialization of data types?");
					}

					if (datatypes.get(property) != null) {
						logger.warn(
								"datatype of property is overwritten: " + property);
					}

					datatypes.put(property, type);
				}

			}

		} catch (IOException e) {
			logger.error("", e);
		}
	}


	@Override
	public FeatureStringValue calculate(Revision revision) {
		String result = null;

		if (revision.getParsedComment() != null) {
			String property = revision.getParsedComment().getProperty();

			result = datatypes.get(property);
		}

		return new FeatureStringValue(result);
	}

}
