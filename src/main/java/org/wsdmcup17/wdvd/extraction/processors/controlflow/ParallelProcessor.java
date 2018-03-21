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

package org.wsdmcup17.wdvd.extraction.processors.controlflow;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wsdmcup17.wdvd.extraction.processors.RevisionProcessor;
import org.wsdmcup17.wdvd.extraction.revision.implementation.RevisionImpl;
import org.wsdmcup17.wdvd.extraction.revision.interfaces.Revision;


/**
 * Calls the method parallelProcessor.processRevision for every Revision. The
 * order of those method calls is not known. The method
 * parallelProcessor.processRevision must be thread-safe. Finally,
 * nextProcessor.processRevision is called in the same order that this class
 * received them originally.
 *
 */
public class ParallelProcessor implements RevisionProcessor {
	static final Logger logger = LoggerFactory.getLogger(ParallelProcessor.class);

	static final int MAX_QUEUE_SIZE = 100;

	final AtomicLong seq = new AtomicLong(0);

	private LinkedBlockingQueue<FIFOEntry<Revision>> incomingQueue =
			new LinkedBlockingQueue<>(MAX_QUEUE_SIZE);
	private PriorityBlockingQueue<FIFOEntry<Revision>> outgoingQueue =
			new PriorityBlockingQueue<>();

	private List<RevisionProcessor> workProcessors;
	private Reducer reducer;
	private RevisionProcessor nextProcessor;

	private String name;

	private List<Thread> workerThreads = new ArrayList<>();
	private Thread collectorThread;
	private Collector collector;

	public ParallelProcessor(
			List<RevisionProcessor> workProcessors,
			Reducer reducer,
			RevisionProcessor nextProcessor,
			String name) {
		this.workProcessors = workProcessors;
		this.nextProcessor = nextProcessor;
		this.reducer = reducer;
		this.name = name;
	}

	@Override
	public void startRevisionProcessing() {
		logger.info("Starting...");

		collector = new Collector(outgoingQueue, nextProcessor, workProcessors.size());

		for (int i = 0; i < workProcessors.size(); i++) {
			workProcessors.get(i).startRevisionProcessing();

			Runnable runnable = new Worker(
					incomingQueue, outgoingQueue, collector, workProcessors.get(i));
			Thread thread = new Thread(
					runnable, "Parallel Revision Processor " + name + " " + i);
			workerThreads.add(thread);
			thread.start();
		}

		collectorThread = new Thread(collector, "Collector Revision Processor");
		collectorThread.start();
	}

	@Override
	public void processRevision(Revision revision) {
		// While the outgoing queue is too large, we have to wait (because the
		// outgoing queue is unbounded).
		// We must wait in front of the incoming queue and not in front of the
		// outgoing queue. If we waited in front of the outgoing queue, it can
		// happen that the queue never gets the next element n + 1 because it
		// cannot be inserted in the queue.
		synchronized (collector.synchronizerNonFull) {
			while (outgoingQueue.size() >= MAX_QUEUE_SIZE) {
				try {
					collector.synchronizerNonFull.wait();
				} catch (InterruptedException e) {
					logger.error("", e);
				}
			}
		}

		// Put revision into incoming queue.
		// Workers will retrieve revisions from there and put them in the
		// outgoing queue.
		try {
			incomingQueue.put(new FIFOEntry<>(revision, seq.getAndIncrement()));
		} catch (InterruptedException e) {
			logger.error("", e);
		}
	}

	@Override
	public void finishRevisionProcessing() {
		logger.debug("Starting to finish...");

		logger.debug("Notifying workers to stop ...");
		// Make the worker threads finish their work
		for (int i = 0; i < workProcessors.size(); i++) {
			try {
				incomingQueue.put(FIFOEntry.DONE);
			} catch (InterruptedException e) {
				logger.error("", e);
			}
		}

		logger.debug("Waiting for the workers to stop ...");
		// wait for all worker threads to finish
		for (int i = 0; i < workProcessors.size(); i++) {
			try {
				workerThreads.get(i).join();
			} catch (InterruptedException e) {
				logger.error("", e);
			}
		}

		logger.debug("Waiting for collector to stop...");
		// wait for the collector thread to finish
		// (and call nextWorker.finishRevisionProcessing() method)
		try {
			collectorThread.join();
		} catch (InterruptedException e) {
			logger.error("", e);
		}

		logger.debug("Log the finishing of all workers ...");
		for (int i = 0; i < workProcessors.size(); i++) {
				workProcessors.get(i).finishRevisionProcessing();
		}

		if (reducer != null) {
			logger.debug("Reduce ...");
			reducer.reduce(workProcessors);
		}

		logger.info("Finished.");
	}

	@Override
	public void flush() {
		logger.debug("Flushing...");

		this.processRevision(FIFOEntry.FLUSH_REVISION);

		while (!collector.isFlushed()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				logger.error("", e);
			}
		}

		collector.resetFlushed();

		logger.debug("Flushing...done.");
	}
}

/**
 * There are several workers which take an element from the incoming queue,
 * process it, and put it in the outgoing queue.
 *
 */
class Worker implements Runnable {
	static final Logger logger = LoggerFactory.getLogger(Worker.class);

	private LinkedBlockingQueue<FIFOEntry<Revision>> incomingQueue;
	private PriorityBlockingQueue<FIFOEntry<Revision>> outgoingQueue;
	private RevisionProcessor workProcessor;
	private Collector collector;


	public Worker(LinkedBlockingQueue<FIFOEntry<Revision>> incomingQueue,
			PriorityBlockingQueue<FIFOEntry<Revision>> outgoingQueue,
			Collector collector,
			RevisionProcessor workProcessor) {
		this.incomingQueue = incomingQueue;
		this.outgoingQueue = outgoingQueue;
		this.workProcessor = workProcessor;
		this.collector = collector;
	}


	/**
	 * Take element from incoming queue, process it, and put it in outgoing queue.
	 */
	@Override
	public void run() {
		try {
			FIFOEntry<Revision> entry;

			entry = incomingQueue.take();

			while (entry != FIFOEntry.DONE) {
				Revision revision = entry.getEntry();

				if (revision != FIFOEntry.FLUSH_REVISION) {
					workProcessor.processRevision(entry.getEntry());
				}

				outgoingQueue.put(entry);
				synchronized (collector.synchronizerNonEmpty) {
					collector.synchronizerNonEmpty.notifyAll();
				}

				try {
					entry = incomingQueue.take();
				} catch (InterruptedException e) {
					logger.error("", e);
				}
			}

			outgoingQueue.put(FIFOEntry.DONE);
			synchronized (collector.synchronizerNonEmpty) {
				collector.synchronizerNonEmpty.notifyAll();
			}

		} catch (Throwable e1) {
			logger.error("", e1);
		}
	}
}

/**
 * There is exactly one Collector which takes elements from the outgoing queue,
 * and forwards them to the next processor. It preserves the original order of
 * the elements.
 *
 */
class Collector implements Runnable {
	static final Logger logger = LoggerFactory.getLogger(Collector.class);
	private RevisionProcessor nextProcessor;
	private AbstractQueue<FIFOEntry<Revision>> outgoingQueue;
	private int runningWorkerThreads;

	// they should NOT be static because there can be several ParallelProcessors
	final Object synchronizerNonEmpty = new Object();
	final Object synchronizerNonFull = new Object();

	private volatile boolean flushed = false;


	public Collector(
			AbstractQueue<FIFOEntry<Revision>> outgoingQueue,
			RevisionProcessor nextProcessor,
			int runningWorkerThreads) {
		this.outgoingQueue = outgoingQueue;
		this.nextProcessor = nextProcessor;
		this.runningWorkerThreads = runningWorkerThreads;
	}

	@Override
	public void run() {
		try {
			nextProcessor.startRevisionProcessing();



			long lastSeqNum = -1;

			while (runningWorkerThreads > 0) {
				FIFOEntry<Revision> peek = null;
				// We have to wait for the outgoingQueue to contain at least one element
				synchronized (synchronizerNonEmpty) {
					peek = outgoingQueue.peek();
					while (peek == null) {
						try { synchronizerNonEmpty.wait(); } catch (InterruptedException e) { logger.error("", e); }
						peek = outgoingQueue.peek();
					}
				}
				if (peek == FIFOEntry.DONE) {
					outgoingQueue.remove(FIFOEntry.DONE);
					synchronized (synchronizerNonFull) {
						synchronizerNonFull.notifyAll();
					}

					runningWorkerThreads--;
				} else if (peek.getSeqNum() == lastSeqNum + 1) {
					outgoingQueue.remove(peek);
					synchronized (synchronizerNonFull) {
						synchronizerNonFull.notifyAll();
					}

					Revision revision = peek.getEntry();
					if (revision != FIFOEntry.FLUSH_REVISION) {
						nextProcessor.processRevision(peek.getEntry());
					} else {
						nextProcessor.flush();
						flushed = true;
					}
					lastSeqNum = peek.getSeqNum();
				}
			}

			nextProcessor.finishRevisionProcessing();
		} catch (Throwable t) {
			logger.error("", t);
		}
	}

	public boolean isFlushed() {
		return flushed;
	}

	public void resetFlushed() {
		flushed = false;
	}
}

class FIFOEntry<E> implements Comparable<FIFOEntry<E>> {
	final long seqNum;
	final E entry;
	static final FIFOEntry<Revision> DONE = new FIFOEntry<>();

	static final Revision FLUSH_REVISION = new RevisionImpl();

	public FIFOEntry(E entry, long seqNum) {
		this.entry = entry;
		this.seqNum = seqNum;
	}

	private FIFOEntry() {
		seqNum = Long.MAX_VALUE;
		entry = null;
	}

	public E getEntry() {
		return entry;
	}

	public long getSeqNum() {
		return seqNum;
	}

	@Override
	public int compareTo(FIFOEntry<E> other) {
		return (seqNum < other.seqNum ? -1 : 1);
	}
}
