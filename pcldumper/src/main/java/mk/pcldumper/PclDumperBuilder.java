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

/**
 * The {@link PclDumperBuilder} is used to build a {@link PclDumper}.
 */
public final class PclDumperBuilder {

    private boolean quiet = false;
    private boolean showOffsets = false;
    private boolean verbose = false;

    /**
     * Constructor of the {@link PclDumperBuilder}.
     */
    public PclDumperBuilder() {
    }

    /**
     * Sets the <i>quiet</i> switch. If <i>quiet</i> is set to true, the output {@link PclDumper} will
     * not contain the header line. The flag is initially set to <b>false</b>.
     *
     * @param value   the value of the <i>quiet</i> switch.
     *
     * @return the {@link PclDumperBuilder}.
     */
    public PclDumperBuilder quiet(final boolean value) {
        this.quiet = value;
        return this;
    }

    /**
     * Sets the <i>showOffsets</i> switch. If <i>showOffsets</i> is set to true, the output {@link PclDumper} will
     * contain the offset of every parsed printer command. The flag is initially set to <b>false</b>.
     *
     * @param value   the value of the <i>showOffsets</i> switch.
     *
     * @return the {@link PclDumperBuilder}.
     */
    public PclDumperBuilder showOffsets(final boolean value) {
        this.showOffsets = value;
        return this;
    }

    /**
     * Sets the <i>verbose</i> switch. If <i>verbose</i> is set to true, the output {@link PclDumper} will
     * contain more details of several commands. The flag is initially set to <b>false</b>.
     *
     * @param value   the value of the <i>verbose</i> switch.
     *
     * @return the {@link PclDumperBuilder}.
     */
    public PclDumperBuilder verbose(final boolean value) {
        this.verbose = value;
        return this;
    }

    /**
     * Builds the {@link PclDumper}.
     *
     * @return the {@link PclDumper}.
     */
    public PclDumper build() {
        return new PclDumper(this.quiet, this.showOffsets, this.verbose);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName());

        sb.append(":");
        sb.append("quiet=");
        sb.append(this.quiet);

        sb.append(",");
        sb.append("showOffsets=");
        sb.append(this.showOffsets);

        sb.append(",");
        sb.append("verbose=");
        sb.append(this.verbose);

        return sb.toString();
    }
}
