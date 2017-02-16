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

import java.util.Collections;
import java.util.List;

/**
 * This class wraps additional information about a printer command.
 */
final class PrinterCommandDetails {

    private final String summary;
    private final List<String> details;

    /**
     * Constructor without detailed multi-line information.
     */
    PrinterCommandDetails(final String summary) {
        this(summary, Collections.<String>emptyList());
    }

    /**
     * Constructor with a short summary and detailed multi-line information.
     */
    PrinterCommandDetails(final String summary, final List<String> details) {
        this.summary = summary == null ? "" : summary;
        this.details = details;
    }

    /**
     * Returns a short summary about the printer command (i. e. "Legal" for the paper
     * type printer command).
     *
     * @return A short summary or an empty string.
     */
    String getSummary() {
        return this.summary;

    }

    /**
     * Returns detailed multi-line information about the printer command (i. e. a completely
     * decoded font header).
     *
     * @return A list containing one or more lines of additional details or an empty list.
     */
    List<String> getDetails() {
        return this.details;
    }
}
