package mk.pcldumper;

/*
 * Copyright 2017 Michael Knigge
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import mk.pclbox.PclException;

/**
 * This is the Main-Class of PCL-Dumper. It is just used to parse the command line arguments. This is the
 * only class that has an external dependency. If you want to add the {@link PclDumper} functionality
 * to your own applications but do not want more external dependencies - just use the {@link PclDumperBuilder}
 * directly.
 */
public final class Main {

    private static final String HELP_HEADER =
            "\nPCL-Dumper reads a PCL printer data stream and dumps every printer command.\n\n";

    private static final String HELP_FOOTER =
            "\nPlease report issues at https://github.com/michaelknigge/pcldumper/issues";

    private static final String HELP_USAGE = "pcldumper [OPTION]... [FILE]";

    /**
     * This is the entry point of PCL-Dumper.
     *
     * @param args   the command line arguments.
     */
    public static void main(final String[] args) {

        final CommandLineParser parser = new DefaultParser();

        final Options options = new Options();
        options.addOption("q", "quiet", false, "do not show the PCL-Dumper header");
        options.addOption("v", "verbose", false, "show more details");
        options.addOption("o", "offsets", false, "show the offsets of the printer commands");
        options.addOption("h", "help", false, "shows this help");
        options.addOption("f", "file", true, "output file for the dump");

        try {
            final CommandLine line = parser.parse(options, args);
            final String[] fileNames = line.getArgs();

            if (line.hasOption("help") || fileNames.length != 1) {
                showHelpAndExit(options);
            }

            final PclDumperBuilder builder = new PclDumperBuilder();
            builder.quiet(line.hasOption("quiet"));
            builder.showOffsets(line.hasOption("offsets"));
            builder.verbose(line.hasOption("verbose"));

            try (final InputStream in = new FileInputStream(fileNames[0])) {
                if (line.hasOption("file")) {
                    dumpToFile(builder.build(), in, line.getOptionValue("file"));
                } else {
                    dumpToStandardOutput(builder.build(), in);
                }
            } catch (final FileNotFoundException e) {
                showError(e.getMessage());
            } catch (final IOException e) {
                showError(e.getMessage());
            }
        } catch (final ParseException e) {
            System.err.println(e.getMessage());
            System.err.println();
            showHelpAndExit(options);
        }
    }

    /**
     * Dumps the PCL data stream to System.out.
     *
     * @param dumper   a ready to use (configured) {@link PclDumper}.
     * @param in   the {@link InputStream} to read from
     */
    private static void dumpToStandardOutput(PclDumper dumper, InputStream in) {
        try {
            dumper.dump(in, System.out);
        } catch (IOException | PclException e) {
            showError(e.getMessage());
        }
    }

    /**
     * Dumps the PCL data stream to a file.
     *
     * @param dumper   a ready to use (configured) {@link PclDumper}.
     * @param in   the {@link InputStream} to read from
     * @param fileName   the name of the output file.
     */
    private static void dumpToFile(PclDumper dumper, InputStream in, String fileName) {
        try (final PrintStream out = new PrintStream(fileName)) {
            dumper.dump(in, out);
        } catch (final PclException | IOException e) {
            showError(e.getMessage());
        }
    }

    /**
     * Shows the online help of PCL-Dumper and exits the JVM with RC=1.
     *
     * @param options   all valid command line options.
     */
    private static final void showHelpAndExit(final Options options) {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(HELP_USAGE, HELP_HEADER, options, HELP_FOOTER);
        System.exit(1);
    }

    /**
     * Shows an error message.
     *
     * @param message   the message to show.
     */
    private static final void showError(final String message) {
        System.err.println();
        System.err.println("**********************************************************************");
        System.err.println();
        System.err.println(message);
        System.err.println();
        System.err.println("**********************************************************************");
        System.err.println();
    }
}
