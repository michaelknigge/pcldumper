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

final class PclDumperContext {

    enum TextParsingMethod {
        /**
         * 0 and 1 (one bye per character).
         */
        DEFAULT,

        /**
         * 21 (one or two bytes bye per character).
         */
        ASIAN_SEVEN_BIT,

        /**
         * 31 (one or two bytes bye per character).
         */
        SHIFT_JIS,

        /**
         * 38 (one or two bytes bye per character).
         */
        ASIAN_EIGHT_BIT,

        /**
         * 83 (according to http://www.tek-tips.com/faqs.cfm?fid=6911)
         */
        UNICODE,

        /**
         * 1008 (according to http://www.tek-tips.com/faqs.cfm?fid=6911)
         */
        UTF_8;
    }

    private TextParsingMethod textParsingMethod;

    /**
     * Constructor of the {@link PclDumperContext}.
     */
    PclDumperContext() {
        this.textParsingMethod = TextParsingMethod.DEFAULT;
    }

    /**
     * Returns the current text parsing method.
     *
     * @return the current text parsing method
     */
    TextParsingMethod getTextParsingMethod() {
        return this.textParsingMethod;
    }

    /**
     * Sets the current text parsing method.
     */
    void setTextParsingMethod(final TextParsingMethod value) {
        this.textParsingMethod = value;
    }

}
