package de.textmode.pcldumper;

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
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;

import javax.xml.bind.DatatypeConverter;

import de.textmode.pclbox.ControlCharacterCommand;
import de.textmode.pclbox.HpglCommand;
import de.textmode.pclbox.ParameterizedPclCommand;
import de.textmode.pclbox.PclException;
import de.textmode.pclbox.PclParser;
import de.textmode.pclbox.PjlCommand;
import de.textmode.pclbox.PrinterCommand;
import de.textmode.pclbox.PrinterCommandHandler;
import de.textmode.pclbox.PrinterCommandVisitor;
import de.textmode.pclbox.TextCommand;
import de.textmode.pclbox.TwoBytePclCommand;

/**
 * The {@link PclDumper} parses a PCL file and prints all printer commands.
 */
public final class PclDumper implements PrinterCommandHandler, PrinterCommandVisitor {

    private static final PrinterCommandExecutorMap EXECUTORS = new PrinterCommandExecutorMap();

    private static final String PCLBOX_VERSION = "1.0";
    private static final String PCLBOX_URL = "https://github.com/michaelknigge/pcldumper";

    private static final String INDENTION_WITH_OFFSETS = "                                    ";
    private static final String INDENTION_WITHOUT_OFFSETS = "                         ";

    private final boolean quiet;
    private final boolean verbose;
    private final boolean showOffsets;

    private PclDumperContext context;
    private PrintStream outputStream;

    /**
     * Constructor that internally creates a {@link FileInputStream} for reading and seeking
     * within the PCL data stream.
     *
     * @param quiet   true if no header line should be printed.
     * @param showOffsets   true if offsets should be printed.
     * @param showHpGl   true if HP/GL commands should be printed.
     * @param verbose   true if more details should be printed.
     */
    PclDumper(final boolean quiet, final boolean showOffsets, boolean verbose) {
        this.quiet = quiet;
        this.showOffsets = showOffsets;
        this.verbose = verbose;
    }

    /**
     * Performs the parsing and dumping of the PCL printer data stream.
     *
     * @param in   the input PCL data stream to be parsed.
     * @param out   the output stream to which the dump will be written.
     *
     * @throws PclException if the parsed PCL data stream contains an error
     * @throws IOException if an I/O error occurs
     */
    public void dump(final InputStream in, final PrintStream out) throws IOException, PclException {

        if (!this.quiet) {
            out.println("PCL-Dumper " + PCLBOX_VERSION + " - " + PCLBOX_URL);
            out.println("-----------------------------------------------------------------------------");
            out.println(" ");
        }

        this.outputStream = out;
        this.context = new PclDumperContext();

        try (final PclParser parser = new PclParser(in, this)) {
            parser.parse();
        }
    }

    @Override
    public void handlePrinterCommand(PrinterCommand command) {
        if (this.showOffsets) {
            this.outputStream.print(String.format("%08X : ", command.getOffset()));
        }

        command.accept(this);
    }

    @Override
    public void handle(TextCommand command) {
        // TODO  prettyPrint (for stuff < ASCII 32)
        final Charset charset = this.context.getTextParsingMethod().getCharset();
        if (this.verbose) {
            this.printPrinterCommandLine(command, "TEXT", "", command.getTextualDescription());

            this.printIndentedLine("Length (Bytes) : " + command.getText().length);
            this.printIndentedLine("Parsing Method : " + this.context.getTextParsingMethod());
            this.printIndentedLine("Hexadecimal    : " + DatatypeConverter.printHexBinary(command.getText()));

            this.printIndentedLine("Decoded        : " + new String(command.getText(), charset));
        } else {
            this.printPrinterCommandLine(command, "TEXT", "", new String(command.getText(), charset));
        }
    }

    @Override
    public void handle(ControlCharacterCommand command) {
        this.printPrinterCommandLine(command, "CNTL", command.toDisplayString(), command.getTextualDescription());
    }

    @Override
    public void handle(TwoBytePclCommand command) {
        this.printPrinterCommandLine(command, "PCL", command.toDisplayString(), command.getTextualDescription());
    }

    @Override
    public void handle(ParameterizedPclCommand command) {
        this.printPrinterCommandLine(command, "PCL", command.toDisplayString(), command.getTextualDescription());
    }

    @Override
    public void handle(PjlCommand command) {
        this.printPrinterCommandLine(command, "PJL", "@PJL", command.toDisplayString());
    }

    @Override
    public void handle(HpglCommand command) {
        this.printPrinterCommandLine(command, "HPGL", command.toCommandString(), command.getTextualDescription());
        if (this.verbose) {
            this.printIndentedLine(command.toDisplayString());
        }
    }

    private void printPrinterCommandLine(
            PrinterCommand cmd,
            final String type,
            final String command,
            final String description) {

        final PrinterCommandDetails details = EXECUTORS.executeFor(cmd, this.context);
        final String toPrint;
        if (details.getSummary().length() > 0) {
            toPrint = description + " (" + details.getSummary() + ")";
        } else {
            toPrint = description;
        }

        this.outputStream.println(String.format("%1$-8s %2$-15s %3$s", type, command, toPrint));

        if (this.verbose) {
            for (final String s : details.getDetails()) {
                this.printIndentedLine(s);
            }
        }
    }

    private void printIndentedLine(final String text) {
        if (this.showOffsets) {
            this.outputStream.print(INDENTION_WITH_OFFSETS);
        } else {
            this.outputStream.print(INDENTION_WITHOUT_OFFSETS);
        }

        this.outputStream.println(">>> " + text);
    }
}
