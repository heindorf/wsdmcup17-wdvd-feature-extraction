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

package org.wsdmcup17.wdvd.extraction.features.item.misc;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.StatementGroup;
import org.wikidata.wdtk.datamodel.interfaces.Value;
import org.wsdmcup17.wdvd.extraction.features.FeatureBooleanValue;
import org.wsdmcup17.wdvd.extraction.features.FeatureImpl;
import org.wsdmcup17.wdvd.extraction.revision.interfaces.Revision;

public class IsHuman extends FeatureImpl {

	static final Logger logger = LoggerFactory.getLogger(IsHuman.class);

	@Override
	public FeatureBooleanValue calculate(Revision revision) {
		boolean result = false;

		ItemDocument itemDocument = revision.getItemDocument();

		if (itemDocument != null) {
			List<StatementGroup> statementGroups =
					itemDocument.getStatementGroups();

			for (StatementGroup statementGroup: statementGroups) {
				String property = statementGroup.getProperty().getId();

				if (property.equals("P31")) {
					for (Statement statement: statementGroup.getStatements()) {
						Value value = statement.getValue();
						if ((value instanceof ItemIdValue)
								&& (((ItemIdValue) value).getId().equals("Q5"))) {
							result = true;
						}
					}
				}
			}
		}

		return new FeatureBooleanValue(result);
	}

}
