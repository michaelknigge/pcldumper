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

import de.textmode.pclbox.PrinterCommand;

/**
 * The {@link PrinterCommandExecutorMap} contains a map with several
 * {@link PrinterCommandExecutor} that are required to maintain the {@link PclDumperContext}
 * and to return more specific information of a concrete {@link PrinterCommand}.
 */
final class PrinterCommandExecutorMap {

    private static final PrinterCommandExecutor DEFAULT_EXECUTOR = new DefaultCommandExecutor();
    private static final HashMap<String, PrinterCommandExecutor> EXECUTORS = new HashMap<>();

    static {
        EXECUTORS.put("(sT", new TypefaceFamilyCommandExecutor());
        EXECUTORS.put(")sT", new TypefaceFamilyCommandExecutor());
        EXECUTORS.put("&tP", new TextParsingMethodCommandExecutor());
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
        final PrinterCommandExecutor executor = EXECUTORS.get(command.toCommandString());
        if (executor != null) {
            return executor.execute(command, context);
        } else {
            return DEFAULT_EXECUTOR.execute(command, context);
        }
    }
}
