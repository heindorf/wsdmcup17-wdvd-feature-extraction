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

package org.wsdmcup17.wdvd.extraction.features.sentence;

import org.apache.commons.lang3.StringUtils;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.MonolingualTextValue;
import org.wsdmcup17.wdvd.extraction.features.FeatureFloatValue;
import org.wsdmcup17.wdvd.extraction.features.FeatureImpl;
import org.wsdmcup17.wdvd.extraction.revision.interfaces.Revision;

public class CommentLabelSimilarity extends FeatureImpl {

	private static String getEnglishLabel(ItemDocument itemDocument) {
		String result = null;

		if (itemDocument != null) {
			MonolingualTextValue label = itemDocument.getLabels().get("en");
			if (label != null) {
				result = label.getText();
			}
		}

		return result;
	}

	@Override
	public FeatureFloatValue calculate(Revision revision) {
		Float result = null;

		String suffixComment = revision.getParsedComment().getSuffixComment();

		if (suffixComment != null) {

			ItemDocument itemDocument = revision.getItemDocument();

			String englishLabel = getEnglishLabel(itemDocument);

			if (englishLabel != null) {
				suffixComment = suffixComment.trim();
				englishLabel = englishLabel.trim();

				result = (float) StringUtils.getJaroWinklerDistance(
						englishLabel, suffixComment);
			}
		}

		return new FeatureFloatValue(result);
	}

}
