package com.ergh99.util.octgn;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OCTGNToolApp {

	public static final String FEED_URL =
			"https://www.myget.org/f/octgngamedirectory";
	public static final String FRESH_INSTALL_OPT = "f";
	public static final String OCTGN_HOME_OPT = "h";
	public static final String DO_INSTALL_OPT = "i";
	public static final String GET_IMAGES_OPT = "g";

	private static final Logger log =
			LoggerFactory.getLogger(OCTGNToolApp.class);

	private static boolean FRESH_INSTALL;
	private static boolean DO_INSTALL;
	private static boolean GET_IMAGES;

	private static Path octgnPath;
	private static String gameName;

	public static void main(String[] args) {
		if (args.length == 0) {
			printUsage();
			System.exit(0);
		}
		try {
			processArguments(args);
			verifyOCTGN();
			OCTGN o = new OCTGN(octgnPath);
			if (FRESH_INSTALL || DO_INSTALL) {
				OCTGNDirectory dir = new OCTGNDirectory(new URL(FEED_URL));
				OCTGNEntry entry = dir.getEntryForName(gameName);
				if (entry != null) {
					o = new OCTGN(octgnPath);
					OCTGNGame game = o.installGameFromEntry(entry);
					log.info("Successfully installed: {}", game.getTitle());
				}
			}

			if (FRESH_INSTALL || GET_IMAGES) {
				if (gameName.equals(ANRConstants.ANR_TITLE) == false) {
					System.err.println("Only Android-Netrunner is supported at this time");
					System.exit(-1);
				}
				OCTGNGame game = o.getGameByTitle(gameName);
				Set<Path> cardPaths = game.getCardPaths();
				System.out.println("Processing " + game.getSetCount() + " sets");
				System.out.println("Processing " + game.getCardCount() + " cards");
				try {
					ANRImageDownloader.downloadCards(cardPaths);
				} catch (InterruptedException e) {
					log.error(e.getLocalizedMessage());
					throw new Error(e);
				}
				System.out.println("Done!");
			}
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private static void verifyOCTGN() {
		if (Files.notExists(octgnPath.resolve("GameDatabase"))) {
			System.err.println("No OCTGN installation found at " + octgnPath);
			printUsage();
			System.exit(1);
		}
	}

	protected static void printUsage() {
		HelpFormatter f = new HelpFormatter();
		f.printHelp("java -jar " + getJarName(), getOptions());
	}

	protected static String getJarName() {
		String classPath = "/" + OCTGNToolApp.class.getName().replace('.', '/') + ".class";
		URL location = OCTGNToolApp.class.getResource(classPath);
		return getJarFromUrl(location);
	}

	protected static String getJarFromUrl(URL location) {
		if (location.getProtocol().equals("jar")) {
			String url = location.toExternalForm();
			int jarOffset = url.lastIndexOf("jar!");
			return url.substring(url.lastIndexOf('/', jarOffset) +1, jarOffset) + "jar";
		} else {
			return null;
		}
	}

	protected static void processArguments(String[] args) {
		CommandLineParser p = new BasicParser();
		CommandLine cl = null;
		try {
			cl = p.parse(getOptions(), args);
		} catch (UnrecognizedOptionException e) {
			printUsage();
			System.exit(-2);
		} catch (ParseException e) {
			log.debug("Error parsing arguments: {}", Arrays.toString(args));
			e.printStackTrace();
			System.exit(-1);
		}

		FRESH_INSTALL = cl.hasOption(FRESH_INSTALL_OPT);
		if (FRESH_INSTALL || args.length == 0) {
			gameName = ANRConstants.ANR_TITLE;
			octgnPath = Paths.get(".");
		} else {
			octgnPath = Paths.get((cl.hasOption(OCTGN_HOME_OPT))
					? cl.getOptionValue(OCTGN_HOME_OPT)
					: ".");

			if (cl.hasOption(DO_INSTALL_OPT)) {
				gameName = cl.getOptionValue(DO_INSTALL_OPT);
				DO_INSTALL = true;
			}

			if (cl.hasOption(GET_IMAGES_OPT)) {
				gameName = cl.getOptionValue(GET_IMAGES_OPT);
				GET_IMAGES = true;
			}

			if (DO_INSTALL && GET_IMAGES) {
				System.err.println("install-game cannot be combined with get-images");
				printUsage();
				System.exit(-1);
			}
		}
	}

	@SuppressWarnings("static-access")
	private static Options getOptions() {
		Options opts = new Options();

		opts.addOption(OptionBuilder
				.withDescription("Set OCTGN home directory")
				.withLongOpt("octgn-home")
				.hasArg(true)
				.withArgName("path")
				.create(OCTGN_HOME_OPT));
		OptionGroup modeGroup = new OptionGroup();
		Option freshInstallOpt = OptionBuilder
				.withDescription("Only for a fresh install of OCTGN.  Sets up Android=Netrunner")
				.withLongOpt("fresh-install")
				.create(FRESH_INSTALL_OPT);
		modeGroup.addOption(freshInstallOpt);
		modeGroup.addOption(
				OptionBuilder
				.withDescription("Install the specified game from the OCTGN Game Directory")
				.withLongOpt("install=game")
				.hasArg(true)
				.withArgName("game-name")
				.create(DO_INSTALL_OPT));
		modeGroup.addOption(
				OptionBuilder
				.withDescription("Install images for specified game (Only Android-Netrunner is currently supported)")
				.withLongOpt("get-images")
				.hasArg(true)
				.withArgName("game-name")
				.create(GET_IMAGES_OPT));
		modeGroup.isRequired();
		opts.addOptionGroup(modeGroup);

		return opts;
	}

	public static final DirectoryStream.Filter<Path> DIRS_ONLY = Files::isDirectory;
}
