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

package org.wsdmcup17.wdvd.extraction.processors.output;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.wsdmcup17.wdvd.extraction.features.Feature;
import org.wsdmcup17.wdvd.extraction.features.FeatureValue;
import org.wsdmcup17.wdvd.extraction.revision.interfaces.Revision;

public class FeatureCSVPrinter implements Closeable {
	private OutputStream outputStream;
	private List<Feature> features;

	CSVPrinter csvPrinter;

	public FeatureCSVPrinter(List<Feature> features, OutputStream outputStream) throws IOException {
		this.outputStream = outputStream;
		this.features = features;

		OutputStreamWriter writer =
				new OutputStreamWriter(outputStream, "utf-8");

		String[] header = new String[features.size()];

		for (int i = 0; i < features.size(); i++) {
			header[i] = features.get(i).getName();
		}


		csvPrinter = CSVFormat.RFC4180.withHeader(header).print(writer);
	}

	public void printFeatures(Revision revision) throws IOException {
		List<String> record = new ArrayList<>(features.size());

		for (int i = 0; i < features.size(); i++) {
			Feature feature = features.get(i);
			FeatureValue featureValue = revision.getFeatureValues().get(feature);
			String writeString;

			if (featureValue == null) {
				throw new RuntimeException("Feature returned null (Revision "
						+ revision.getRevisionId()
						+ ", Feature "
						+ feature.getName() + ")");
			} else {
				writeString = featureValue.toString();
			}

			record.add(writeString);
		}

		csvPrinter.printRecord(record);
	}

	@Override
	public void close() throws IOException {
		csvPrinter.close();
		outputStream.close();
	}


}
