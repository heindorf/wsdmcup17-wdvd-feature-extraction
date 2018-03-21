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

package org.wsdmcup17.wdvd.extraction.streams;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncOutputStream extends PipedOutputStream {

	static final Logger logger = LoggerFactory.getLogger(AsyncOutputStream.class);

	Thread thread;

	public AsyncOutputStream(
			final OutputStream outputStream,
			final String threadName,
			final int bufferSize) throws IOException {
		final PipedInputStream pipedInputStream = new PipedInputStream(bufferSize);
		this.connect(pipedInputStream);

		thread = new Thread(threadName) {
			@Override
			public void run() {
				try {
					IOUtils.copy(pipedInputStream, outputStream);

					pipedInputStream.close();
					outputStream.close();
				} catch (Throwable e) {
					logger.error("", e);
				}
			}
		};
		thread.start();
	}

	@Override
	public void close() throws IOException {
		super.close();

		try {
			thread.join();
		} catch (InterruptedException e) {
			logger.error("", e);
		}
	}

}
