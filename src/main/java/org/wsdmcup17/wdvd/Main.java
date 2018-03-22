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

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.jar.Manifest;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.AsyncAppender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.rewrite.RewriteAppender;
import org.apache.log4j.rewrite.RewritePolicy;
import org.apache.log4j.spi.LoggingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A program connecting to WSDM Cup data servers and extracting features.
 */
public class Main {

	static final Logger logger = LoggerFactory.getLogger(Main.class);

	static String runTime;

	// Configuration
	static String serverAddress;
	static String accessToken;
	static File featureFile;

	private static final String
		CMD_LINE_SYNTAX = "feature-extraction -s SERVER -t TOKEN FEATURES",
		OPT_SERVER = "s",
		OPT_SERVER_LONG = "server",
		OPT_SERVER_DESC = "Data server address",
		OPT_TOKEN = "t",
		OPT_TOKEN_LONG = "token",
		OPT_TOKEN_DESC = "Access token";

	static final Level LOG_LEVEL = Level.INFO;

	private static final String
		LOG_PATTERN = "[%d{yyyy-MM-dd HH:mm:ss}] [%-8p] [Extractor] [%c{1}] %m%n",
		UTF_8 = "UTF-8";

	public static void main(String[] args)
	throws URISyntaxException {
		CommandLine cmd = parseArgs(args);
		serverAddress = cmd.getOptionValue(OPT_SERVER);
		accessToken = cmd.getOptionValue(OPT_TOKEN);
		featureFile = new File(cmd.getArgs()[0]);

		initLogger();
		logConfiguration();

		Client client = new Client(serverAddress, accessToken, featureFile);

		try {
			client.start();
		} finally {
			closeLogger();
		}

		if (ErrorFlagAppender.hasErrorOccured()) {
			System.exit(1);
		}
	}

	private static CommandLine parseArgs(String[] args) {
		Options options = new Options();

		Option input = new Option(
				OPT_SERVER, OPT_SERVER_LONG, true, OPT_SERVER_DESC);
		input.setRequired(true);
		options.addOption(input);

		Option meta = new Option(
				OPT_TOKEN, OPT_TOKEN_LONG, true, OPT_TOKEN_DESC);
		meta.setRequired(true);
		options.addOption(meta);

		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd = null;

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			formatter.printHelp(CMD_LINE_SYNTAX, options);
			System.exit(1);
		}
		return cmd;
	}

	private static void initLogger() {
		// Ignore some log messages from third party libraries
		RewriteAppender rewriteAppender = new RewriteAppender();
		rewriteAppender.setRewritePolicy(new RewritePolicy() {
			@SuppressWarnings("deprecation")
			@Override
			public LoggingEvent rewrite(LoggingEvent source) {
				if (source.getLoggerName().equals(
						"org.wikidata.wdtk.datamodel.helpers.DatamodelConverter")) {
					source.level = Level.DEBUG;
				}
				return source;
			}
		});
		org.apache.log4j.Logger.getRootLogger().addAppender(rewriteAppender);

		// Stores whether an error has occured
		AppenderSkeleton errorFlagAppender = new ErrorFlagAppender();
		errorFlagAppender.setThreshold(Level.ERROR);
		errorFlagAppender.activateOptions();
		rewriteAppender.addAppender(errorFlagAppender);

		ConsoleAppender consoleAppender = new ConsoleAppender();
		consoleAppender.setEncoding(UTF_8);
		consoleAppender.setLayout(new PatternLayout(LOG_PATTERN));
		consoleAppender.setThreshold(LOG_LEVEL);
		consoleAppender.activateOptions();
		AsyncAppender asyncConsoleAppender = new AsyncAppender();
		asyncConsoleAppender.addAppender(consoleAppender);
		asyncConsoleAppender.setBufferSize(1024);
		asyncConsoleAppender.activateOptions();
		rewriteAppender.addAppender(asyncConsoleAppender);

		FileAppender fileAppender = new FileAppender();
		fileAppender.setEncoding(UTF_8);
		fileAppender.setFile(featureFile.getAbsoluteFile() + ".log");
		fileAppender.setLayout(new PatternLayout(LOG_PATTERN));
		fileAppender.setThreshold(LOG_LEVEL);
		fileAppender.setAppend(false);
		fileAppender.activateOptions();
		AsyncAppender asyncFileAppender = new AsyncAppender();
		asyncFileAppender.addAppender(fileAppender);
		asyncFileAppender.setBufferSize(1024);
		asyncFileAppender.activateOptions();
		rewriteAppender.addAppender(asyncFileAppender);
	}

	private static void closeLogger() {
		org.apache.log4j.LogManager.shutdown();
	}


	static class ErrorFlagAppender extends AppenderSkeleton {
		static boolean hasErrorOccured = false;

		public static boolean hasErrorOccured() {
			return hasErrorOccured;
		}

		@Override
		public void close() {
		// not implemented
		}

		@Override
		public boolean requiresLayout() {
			return false;
		}

		@Override
		protected void append(LoggingEvent arg0) {
			hasErrorOccured = true;
		}
	}

	private static void logConfiguration() {
		if (logger.isInfoEnabled()) {
			// Host and operating system
			logger.info(
					"Host name: "
					+ getHostName());
			logger.info(
					"Operating system: "
					+ System.getProperty("os.name"));

			// Java
			logger.info(
					"java.home: "
					+ System.getProperty("java.home"));
			logger.info(
					"java.version: "
					+ System.getProperty("java.version"));
			logger.info(
					"java.runtime.name: "
					+ System.getProperty("java.runtime.name"));
			logger.info(
					"java.runtime.version: "
					+ System.getProperty("java.runtime.version"));
			logger.info(
					"java.vm.name: "
					+ System.getProperty("java.vm.name"));
			logger.info(
					"java.vm.version: "
					+ System.getProperty("java.vm.version"));
			logger.info(
					"java.vm.vendor: "
					+ System.getProperty("java.vm.vendor"));

			// Feature Extraction
			logger.info(
					"Filename of JAR: "
					+ getJarFile());
			logger.info(
					"Implementation version: "
					+ Main.class.getPackage().getImplementationVersion());
			logger.info(
					"Build time: "
					+ getBuildTime());
			logger.info(
					"Run time: "
					+ getRunTime());

			// Configuration
			logger.info(
					"server: "
					+ serverAddress);
			logger.info(
					"accessToken: "
					+ accessToken);
			logger.info(
					"Feature file: "
					+ featureFile.getAbsolutePath());
		}
	}

	private static String getHostName() {
		String result = null;
		try {
			result = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			// not implemented
		}

		return result;
	}

	private static File getJarFile() {
		File result = new java.io.File(Main.class.getProtectionDomain()
					.getCodeSource()
					.getLocation()
					.getPath());

		return result;
	}

	private static String getBuildTime() {
		String result = null;
		try {
			Enumeration<URL> resources;
			resources = Main.class.getClassLoader()
					  .getResources("META-INF/MANIFEST.MF");

			Manifest manifest =
					new Manifest(resources.nextElement().openStream());
			result = manifest.getMainAttributes().getValue("Build-Time");

			} catch (IOException e) {
				// not implemented
			}

		return result;
	}

	private static String getRunTime() {
		if (runTime == null) {
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
			runTime = sdf.format(cal.getTime());
		}

		return runTime;
	}
}
