package com.ergh99.util.octgn;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;

@lombok.extern.slf4j.XSlf4j
public class OCTGNToolApp {

    public static final String FRESH_INSTALL_OPT = "f";
    public static final String OCTGN_HOME_OPT = "h";
    public static final String DO_INSTALL_OPT = "i";
    public static final String GET_IMAGES_OPT = "g";

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
                OCTGNDirectory dir = new OCTGNDirectory();
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
                    throw log.throwing(new Error(e));
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
            return url.substring(url.lastIndexOf('/', jarOffset) + 1, jarOffset) + "jar";
        } else {
            return null;
        }
    }

    protected static void processArguments(String[] args) {
        log.entry((Object[]) args);
        CommandLineParser p = new DefaultParser();
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
        log.exit();
    }

    private static Options getOptions() {
        Options opts = new Options();

        opts.addOption(Option.builder(OCTGN_HOME_OPT)
                .desc("Set OCTGN home directory")
                .longOpt("octgn-home")
                .hasArg(true)
                .argName("path")
                .build());
        OptionGroup modeGroup = new OptionGroup();
        Option freshInstallOpt = Option.builder(FRESH_INSTALL_OPT)
                .desc("Only for a fresh install of OCTGN.  Sets up Android=Netrunner")
                .longOpt("fresh-install")
                .build();
        modeGroup.addOption(freshInstallOpt);
        modeGroup.addOption(
                Option.builder(DO_INSTALL_OPT)
                        .desc("Install the specified game from the OCTGN Game Directory")
                        .longOpt("install=game")
                        .hasArg(true)
                        .argName("game-name")
                        .build());
        modeGroup.addOption(
                Option.builder(GET_IMAGES_OPT)
                        .desc("Install images for specified game (Only Android-Netrunner is currently supported)")
                        .longOpt("get-images")
                        .hasArg(true)
                        .argName("game-name")
                        .build());
        modeGroup.setRequired(true);
        opts.addOptionGroup(modeGroup);

        return opts;
    }
}
