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
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;

import javax.xml.bind.DatatypeConverter;

import mk.pclbox.ControlCharacterCommand;
import mk.pclbox.HpglCommand;
import mk.pclbox.ParameterizedPclCommand;
import mk.pclbox.PclException;
import mk.pclbox.PclParser;
import mk.pclbox.PjlCommand;
import mk.pclbox.PrinterCommand;
import mk.pclbox.PrinterCommandHandler;
import mk.pclbox.PrinterCommandVisitor;
import mk.pclbox.TextCommand;
import mk.pclbox.TwoBytePclCommand;

/**
 * The {@link PclDumper} parses a PCL file and prints all printer commands.
 */
final class PclDumper implements PrinterCommandHandler, PrinterCommandVisitor {

    private final static Charset ISO_8869_1 = Charset.forName("iso-8859-1");

    private final boolean quiet;
    private final boolean verbose;
    private final boolean showOffsets;

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
     * @throws PclException
     * @throws IOException
     */
    void dump(final InputStream in, final PrintStream out) throws IOException, PclException {

        if (!this.quiet) {
            out.println("PCL-Dumper 1.0 - see https://github.com/michaelknigge/pcldumper");
            out.println("----------------------------------------------------------------------");
            out.println(" ");
        }

        this.outputStream = out;

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
        // TODO  check "Text Parsing Method" -> see https://docs.oracle.com/javase/8/docs/technotes/guides/intl/encoding.doc.html
        // TODO  also do binary display?
        // TODO  display in next line?
        // TODO  prettyPrint (for stuff < ASCII 32
        if (this.verbose) {
            this.printLine("TEXT", "", command.getTextualDescription());

            this.printLine("Length (Bytes) : " + command.getText().length);
            this.printLine("Parsing Method : " + "currently unknown");
            this.printLine("Hexadecimal    : " + DatatypeConverter.printHexBinary(command.getText()));

            this.printLine("Decoded        : " + new String(command.getText(), ISO_8869_1));
        } else {
            this.printLine("TEXT", "", new String(command.getText(), ISO_8869_1));
        }
    }

    @Override
    public void handle(ControlCharacterCommand command) {
        this.printLine("CNTL", command.toDisplayString(), command.getTextualDescription());
    }

    @Override
    public void handle(TwoBytePclCommand command) {
        this.printLine("PCL", command.toDisplayString(), command.getTextualDescription());
    }

    @Override
    public void handle(ParameterizedPclCommand command) {
        // TODO  show (appended to getTextualDescription) a textual description of the value if flag is set to "verbose"
        this.printLine("PCL", command.toDisplayString(), command.getTextualDescription());
    }

    @Override
    public void handle(PjlCommand command) {
        this.printLine("PJL", "@PJL", command.toDisplayString());
    }

    @Override
    public void handle(HpglCommand command) {
        // TODO  show the whole HPGL command in the next line if "verbose"
        this.printLine("HPGL", command.toCommandString(), command.getTextualDescription());
    }

    private void printLine(final String type, final String command, final String description) {
        this.outputStream.println(String.format("%1$-8s %2$-15s %3$s", type, command, description));
    }

    private void printLine(final String text) {
        if (this.showOffsets) {
            this.outputStream.print("           ");
        } else {
            this.outputStream.print("         ");
        }
        this.outputStream.println(text);
    }
}
