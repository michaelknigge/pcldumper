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

import java.util.HashMap;

import de.textmode.pclbox.ParameterizedPclCommand;
import de.textmode.pclbox.PrinterCommand;
import de.textmode.pcldumper.PclDumperContext.TextParsingMethod;

/**
 * The {@link PrinterCommandExecutorMap} contains a map with several
 * {@link PrinterCommandExecutor} that are required to maintain the {@link PclDumperContext}
 * and to return more specific information of a concrete {@link PrinterCommand}.
 */
final class PrinterCommandExecutorMap {

    die Maps können"static"
    weil die
    executoren keinen
    Zustand haben!

    private final HashMap<String, PrinterCommandExecutor> executors = new HashMap<>();
    private final HashMap<String, String> valueToString = new HashMap<>();

    /**
     * Constructor of the {@link PrinterCommandExecutorMap}. Initially fills the map
     * with all required executors.
     */
    PrinterCommandExecutorMap() {

        // TODO check if all commands are handled: http://www.tek-tips.com/faqs.cfm?fid=6911
        // i. e. I can not remember "text color".....

        // Text Parsing Method
        this.executors.put("&tP", new PrinterCommandExecutor() {

            @Override
            String execute(final PrinterCommand command, final PclDumperContext context) {
                final ParameterizedPclCommand cmd = (ParameterizedPclCommand) command;

                switch (Integer.getInteger(cmd.getValue())) {
                case 0:
                case 1:
                    context.setTextParsingMethod(TextParsingMethod.DEFAULT);
                    return "one byte per character";

                case 21:
                    context.setTextParsingMethod(TextParsingMethod.ASIAN_SEVEN_BIT);
                    return "one or two bytes per character, Asian 7 bit";

                case 31:
                    context.setTextParsingMethod(TextParsingMethod.SHIFT_JIS);
                    return "one or two bytes per character, Shift-JIS";

                case 38:
                    context.setTextParsingMethod(TextParsingMethod.ASIAN_EIGHT_BIT);
                    return "one or two bytes per character, Asian 8 bit";

                case 83:
                    context.setTextParsingMethod(TextParsingMethod.UNICODE);
                    return "Unicode";

                case 1008:
                    context.setTextParsingMethod(TextParsingMethod.UTF_8);
                    return "UTF-8";

                default:
                    context.setTextParsingMethod(TextParsingMethod.DEFAULT);
                    return "unknown text parsing method";
                }
            }
        });

        // Page Size
        this.executors.put("&lA", new PrinterCommandExecutor() {

            @Override
            String execute(final PrinterCommand command, final PclDumperContext context) {
                final ParameterizedPclCommand cmd = (ParameterizedPclCommand) command;

                final lookup mit valueToString final Map

                key = "&lA" + "_" + "1" ==> value ="Executive";


                bei "value" ggf. führende nullen abschneiden!!!
                switch (Integer.getInteger(cmd.getValue())) {
                case 1:
                    return "Executive";
                case 2:
                    return "Letter";
                case 3:
                    return "Legal"
                            <Esc>&l#A (#=6)          Page Size: Ledger
                            <Esc>&l#A (#=25)         Page Size: A5
                            <Esc>&l#A (#=26)         Page Size: A4
                            <Esc>&l#A (#=27)         Page Size: A3
                            <Esc>&l#A (#=80)         Page Size: Monarch Envelope
                            <Esc>&l#A (#=81)         Page Size: COM-10 Envelope
                            <Esc>&l#A (#=90)         Page Size: DL Envelope
                            <Esc>&l#A (#=91)         Page Size: C5 Envelope
                            <Esc>&l#A (#=100)        Page Size: B5 Envelope
                            <Esc>&l#A (#=101)        Page Size: Custom                case 0:
                            case 1:
                                context.setTextParsingMethod(TextParsingMethod.DEFAULT);
                                return "one byte per character";

                            case 21:
                                context.setTextParsingMethod(TextParsingMethod.ASIAN_SEVEN_BIT);
                                return "one or two bytes per character, Asian 7 bit";

                            case 31:
                                context.setTextParsingMethod(TextParsingMethod.SHIFT_JIS);
                                return "one or two bytes per character, Shift-JIS";

                            case 38:
                                context.setTextParsingMethod(TextParsingMethod.ASIAN_EIGHT_BIT);
                                return "one or two bytes per character, Asian 8 bit";

                            case 83:
                                context.setTextParsingMethod(TextParsingMethod.UNICODE);
                                return "Unicode";

                            case 1008:
                                context.setTextParsingMethod(TextParsingMethod.UTF_8);
                                return "UTF-8";

                            default:
                                context.setTextParsingMethod(TextParsingMethod.DEFAULT);
                                return "unknown text parsing method";
                }
            }
        });
    }

    /**
     * Invokes the {@link PrinterCommandExecutor} for the given {@link PrinterCommand}. If no
     * {@link PrinterCommandExecutor} exists <code>null</code> is returned.
     *
     * @param command   the {@link PrinterCommand} that is to be "executed".
     * @param context   the current {@link PclDumperContext}.
     *
     * @return a String containing more information about the given {@link PrinterCommand} or <code>null</code>.
     */
    String executeFor(final PrinterCommand command, final PclDumperContext context) {
        final PrinterCommandExecutor executor = this.executors.get(command.toCommandString());
        if (executor != null) {
            return executor.execute(command, context);
        } else {
            return null;
        }
    }
}
