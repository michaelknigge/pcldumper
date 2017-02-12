package de.textmode.pcldumper;

import java.nio.charset.Charset;

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
        DEFAULT("Default, 1 byte per character", Charset.forName("iso-8859-1")),

        /**
         * 21 (one or two bytes bye per character).
         */
        ASIAN_SEVEN_BIT("Asian seven bit, i. e. JIS X0208, GB 2312-80 or KS C 5601-1992", Charset.forName("EUC-JP")),

        /**
         * 31 (one or two bytes bye per character).
         */
        SHIFT_JIS("Shift-JIS", Charset.forName("Shift_JIS")),

        /**
         * 38 (one or two bytes bye per character).
         */
        ASIAN_EIGHT_BIT("Asian eight bit, i. e. Big Five, TCA, KS C 5601-1992 or GB 2312-80", Charset.forName("Big5")),

        /**
         * 83 (according to http://www.tek-tips.com/faqs.cfm?fid=6911)
         */
        UNICODE("Unicode", Charset.forName("utf-8")),

        /**
         * 1008 (according to http://www.tek-tips.com/faqs.cfm?fid=6911)
         */
        UTF_8("UTF-8", Charset.forName("utf-8"));

        private final String description;
        private final Charset charset;

        private TextParsingMethod(final String description, final Charset charset) {
            this.description = description;
            this.charset = charset;
        }

        /**
         * Returns the {@link Charset} that can be used for decoding.
         *
         * @return A {@link Charset} that can be used to decode data.
         */
        public Charset getCharset() {
            return this.charset;
        }

        @Override
        public String toString() {
            return this.description;
        }
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
