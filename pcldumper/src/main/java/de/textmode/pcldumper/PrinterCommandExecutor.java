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

import de.textmode.pclbox.PrinterCommand;

/**
 * A {@link PrinterCommandExecutor} "executes" a {@link PrinterCommand}. A {@link PrinterCommandExecutor}
 * is responsible to maintain the {@link PclDumperContext} and/or to return more specific information
 * of a concrete {@link PrinterCommand}.
 */
abstract class PrinterCommandExecutor {

    /**
     * Returns more specific information of the given {@link PrinterCommand}.
     */
    abstract String execute(final PrinterCommand command, final PclDumperContext context);
}
