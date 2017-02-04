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

import de.textmode.pclbox.ParameterizedPclCommand;
import de.textmode.pclbox.PrinterCommand;
import de.textmode.pcldumper.PclDumperContext.TextParsingMethod;

/**
 * This {@link PrinterCommandExecutor} handles the {@link PrinterCommand}
 * "Text Parsing Method".
 */
final class TextParsingMethodCommandExecutor extends PrinterCommandExecutor {

    @Override
    String execute(PrinterCommand command, PclDumperContext context) {
        final ParameterizedPclCommand cmd = (ParameterizedPclCommand) command;

        switch (Integer.parseInt(cmd.getValue())) {
        case 0:
        case 1:
            context.setTextParsingMethod(TextParsingMethod.DEFAULT);
            return "One byte per character";

        case 21:
            context.setTextParsingMethod(TextParsingMethod.ASIAN_SEVEN_BIT);
            return "One or two bytes per character, Asian 7 bit";

        case 31:
            context.setTextParsingMethod(TextParsingMethod.SHIFT_JIS);
            return "One or two bytes per character, Shift-JIS";

        case 38:
            context.setTextParsingMethod(TextParsingMethod.ASIAN_EIGHT_BIT);
            return "One or two bytes per character, Asian 8 bit";

        case 83:
            context.setTextParsingMethod(TextParsingMethod.UNICODE);
            return "Unicode";

        case 1008:
            context.setTextParsingMethod(TextParsingMethod.UTF_8);
            return "UTF-8";

        default:
            context.setTextParsingMethod(TextParsingMethod.DEFAULT);
            return "Unknown text parsing method";
        }
    }
}
