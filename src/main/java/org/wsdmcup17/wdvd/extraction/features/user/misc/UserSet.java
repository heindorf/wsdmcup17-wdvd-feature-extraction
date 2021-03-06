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

package org.wsdmcup17.wdvd.extraction.features.user.misc;

import java.util.HashSet;
import java.util.List;

public class UserSet {

	HashSet<String> users = new HashSet<>();

	public UserSet(List<String> names) {
		for (String name: names) {
			String tmp = name.trim();
			tmp = tmp.toLowerCase();
			users.add(tmp);
		}
	}


	public Boolean strContains(String name) {
		if (name == null) {
			return null;
		}

		return contains(name);
	}

	public boolean contains(String name) {
		boolean result = false;
		if (name != null) {
			String tmp = name.toLowerCase();
			result = users.contains(tmp);
		}

		return result;
	}

}
