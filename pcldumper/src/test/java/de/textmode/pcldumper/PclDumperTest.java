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

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;

import de.textmode.pclbox.PclException;
import junit.framework.TestCase;

public final class PclDumperTest extends TestCase {

    // Set this to true to overwrite the "expected files". Note that the JAR needs to be rebuilt
    // because overwriting the files will not update the files bundled with the JAR...
    private static final boolean OVERWRITE_EXTECTED_FILES = false;

    private static final Charset ISO_8859_1 = Charset.forName("iso-8859-1");

    private static final String PCL_SUFFIX = ".pcl";
    private static final String EXPECTED_STANDARD_SUFFIX = "_expected_s.txt";
    private static final String EXPECTED_STANDARD_OFFSETS_SUFFIX = "_expected_so.txt";
    private static final String EXPECTED_VERBOSE_SUFFIX = "_expected_v.txt";
    private static final String EXPECTED_VERBOSE_OFFSETS_SUFFIX = "_expected_vo.txt";

    /**
     * Returns an {@link InputStream} from an embedded resource.
     *
     * @param name   Name of the resource
     *
     * @return an {@link InputStream} from an embedded resource.
     */
    private InputStream getResourceAsStream(final String name) {
        return this.getClass().getClassLoader().getResourceAsStream(name);
    }

    /**
     * Dumps the PCL data from the given {@link InputStream} using the given {@link PclDumper}.
     *
     * @param dumper   The {@link PclDumper} to be used for dumping
     * @param input   The {@link InputStream} to read from
     *
     * @return the created dump
     *
     * @throws IOException If an I/O error occurs
     * @throws PclException If there are Problems with the PCL data stream
     */
    private byte[] dump(final PclDumper dumper, final InputStream input)
            throws IOException, PclException {

        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            dumper.dump(input, new PrintStream(baos, true, "utf-8"));
            return baos.toByteArray();
        }
    }

    /**
     * Dumps the PCL data from the given {@link InputStream} using the given {@link PclDumper}.
     *
     * @param actual   The dump that has been built
     * @param expectedStreamName   The name of the embedded resource that contains the expected dump
     *
     * @throws IOException If an I/O error occurs
     * @throws PclException If there are Problems with the PCL data stream
     */
    private void compare(final byte[] actual, final String expectedStreamName)
            throws IOException, PclException {

        if (OVERWRITE_EXTECTED_FILES) {

            final Path path = Paths.get(
                    new java.io.File(".").getCanonicalPath(),
                    "src",
                    "test",
                    "resources",
                    expectedStreamName);

            System.out.println("*** Updating file " + path);

            try (final FileOutputStream out = new FileOutputStream(path.toString())) {
                out.write(actual);
            }

            return;
        }

        try (final InputStream expected = this.getResourceAsStream(expectedStreamName)) {
            final byte[] expectedBytes = IOUtils.toByteArray(expected);
            if (!Arrays.equals(actual, expectedBytes)) {
                assertEquals(
                        ISO_8859_1.decode(ByteBuffer.wrap(expectedBytes)),
                        ISO_8859_1.decode(ByteBuffer.wrap(actual)));
            }
        }
    }

    /**
     * Dumps the PCL data from the given {@link InputStream}.
     *
     * @param name   The name of the embedded resource that contains the PCL data stream
     *     to be dumped (without the .PCL suffix)
     *
     * @throws IOException If an I/O error occurs
     * @throws PclException If there are Problems with the PCL data stream
     */
    private void performStandardTest(final String name) throws IOException, PclException {
        try (final InputStream input = this.getResourceAsStream(name + PCL_SUFFIX)) {
            this.compare(
                    this.dump(new PclDumperBuilder().build(), input),
                    name + EXPECTED_STANDARD_SUFFIX);
        }
    }

    /**
     * Dumps the PCL data (with offsets) from the given {@link InputStream}.
     *
     * @param name   The name of the embedded resource that contains the PCL data stream
     *     to be dumped (without the .PCL suffix)
     *
     * @throws IOException If an I/O error occurs
     * @throws PclException If there are Problems with the PCL data stream
     */
    private void performStandardTestWithOffsets(final String name) throws IOException, PclException {
        try (final InputStream input = this.getResourceAsStream(name + PCL_SUFFIX)) {
            this.compare(
                    this.dump(new PclDumperBuilder().showOffsets(true).build(), input),
                    name + EXPECTED_STANDARD_OFFSETS_SUFFIX);
        }
    }

    /**
     * Dumps the PCL data (verbose dump) from the given {@link InputStream}.
     *
     * @param name   The name of the embedded resource that contains the PCL data stream
     *     to be dumped (without the .PCL suffix)
     *
     * @throws IOException If an I/O error occurs
     * @throws PclException If there are Problems with the PCL data stream
     */
    private void performVerboseTest(final String name) throws IOException, PclException {
        try (final InputStream input = this.getResourceAsStream(name + PCL_SUFFIX)) {
            this.compare(
                    this.dump(new PclDumperBuilder().verbose(true).build(), input),
                    name + EXPECTED_VERBOSE_SUFFIX);
        }
    }

    /**
     * Dumps the PCL data (verbose dump with offsets) from the given {@link InputStream}.
     *
     * @param name   The name of the embedded resource that contains the PCL data stream
     *     to be dumped (without the .PCL suffix)
     *
     * @throws IOException If an I/O error occurs
     * @throws PclException If there are Problems with the PCL data stream
     */
    private void performVerboseTestWithOffsets(final String name) throws IOException, PclException {
        try (final InputStream input = this.getResourceAsStream(name + PCL_SUFFIX)) {
            this.compare(
                    this.dump(new PclDumperBuilder().verbose(true).showOffsets(true).build(), input),
                    name + EXPECTED_VERBOSE_OFFSETS_SUFFIX);
        }
    }

    /**
     * Performs four different dumps and checks the results.
     *
     * @param name   The name of the embedded resource that contains the PCL data stream
     *     to be dumped (without the .PCL suffix)
     *
     * @throws IOException If an I/O error occurs
     * @throws PclException If there are Problems with the PCL data stream
     */
    private void performTest(final String name) throws IOException, PclException {
        this.performStandardTest(name);
        this.performStandardTestWithOffsets(name);

        this.performVerboseTest(name);
        this.performVerboseTestWithOffsets(name);
    }

    /**
     * Performs a test with a PCL data stream that contains all supported printer commands.
     *
     * @throws IOException If an I/O error occurs
     * @throws PclException If there are Problems with the PCL data stream
     */
    public void testAllTypesOfPrinterCommands() throws IOException, PclException {
        this.performTest("generic_test");
    }
}
