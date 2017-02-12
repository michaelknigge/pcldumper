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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.HexDump;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

import de.textmode.pclbox.ParameterizedPclCommand;
import de.textmode.pclbox.PrinterCommand;

/**
 * This is the default {@link PrinterCommandExecutor} that knows almost
 * every {@link PrinterCommand}.
 */
final class DefaultCommandExecutor extends PrinterCommandExecutor {

    private static final HashMap<String, String> LOOKUP_MAP = new HashMap<>();

    static {
        LOOKUP_MAP.put("&l0S", "Simplex");
        LOOKUP_MAP.put("&l1S", "Duplex, Long-Edge Binding");
        LOOKUP_MAP.put("&l2S", "Duplex, Short-Edge Binding");

        LOOKUP_MAP.put("&a0G", "Select next side");
        LOOKUP_MAP.put("&a1G", "Select front side");
        LOOKUP_MAP.put("&a2G", "Select back side");

        LOOKUP_MAP.put("&l1G", "Upper Output Bin");
        LOOKUP_MAP.put("&l2G", "Lower (Rear) Output Bin");

        LOOKUP_MAP.put("&l1A", "Executive");
        LOOKUP_MAP.put("&l2A", "Letter");
        LOOKUP_MAP.put("&l3A", "Legal");
        LOOKUP_MAP.put("&l6A", "Ledger");
        LOOKUP_MAP.put("&l10A", "JISExec");
        LOOKUP_MAP.put("&l25A", "A5");
        LOOKUP_MAP.put("&l26A", "A4");
        LOOKUP_MAP.put("&l27A", "A3");
        LOOKUP_MAP.put("&l45A", "JIS B5 Paper");
        LOOKUP_MAP.put("&l46A", "JIS B4 Paper");
        LOOKUP_MAP.put("&l71A", "Hagaki Postcard");
        LOOKUP_MAP.put("&l72A", "Oufuku-Hagaki Postcard");
        LOOKUP_MAP.put("&l80A", "Monarch");
        LOOKUP_MAP.put("&l81A", "Com-10");
        LOOKUP_MAP.put("&l90A", "International DL");
        LOOKUP_MAP.put("&l91A", "International C5");
        LOOKUP_MAP.put("&l100A", "International B5");

        LOOKUP_MAP.put("&l0H", "Print the current page, paper source remains unchanged");
        LOOKUP_MAP.put("&l1H", "Feed paper from the a printer-specific tray");
        LOOKUP_MAP.put("&l2H", "Feed paper from manual input");
        LOOKUP_MAP.put("&l3H", "Feed envelope from manual input");
        LOOKUP_MAP.put("&l4H", "Feed paper from lower tray");
        LOOKUP_MAP.put("&l5H", "Feed from optional paper source");
        LOOKUP_MAP.put("&l6H", "Feed envelope from optional envelope feeder");

        LOOKUP_MAP.put("&l0O", "Portrait");
        LOOKUP_MAP.put("&l1O", "Landscape");
        LOOKUP_MAP.put("&l2O", "Reverse Portrait");
        LOOKUP_MAP.put("&l3O", "Reverse Landscape");

        LOOKUP_MAP.put("&l0L", "Disable");
        LOOKUP_MAP.put("&l1L", "Enable");

        LOOKUP_MAP.put("&k0G", "CR=CR; LF=LF; FF=FF");
        LOOKUP_MAP.put("&k1G", "CR=CR-LF; LF=LF; FF=FF");
        LOOKUP_MAP.put("&k2G", "CR=CR; LF=CR-LF; FF=CR-FF");
        LOOKUP_MAP.put("&k3G", "CR=CR-LF; LF=CR-LF; FF=CR-FF");

        LOOKUP_MAP.put("&f0S", "Push - Store cursor position");
        LOOKUP_MAP.put("&f1S", "Pop - Recall a cursor position");

        final String primaryOrSecondary = "()";
        for (int i = 0; i < 2; ++i) {
            final String firstByte = primaryOrSecondary.substring(i, i + 1);

            LOOKUP_MAP.put(firstByte + "s0P", "Fixed spacing");
            LOOKUP_MAP.put(firstByte + "s1P", "Proportional spacing");

            LOOKUP_MAP.put(firstByte + "s0S", "Upright, solid");
            LOOKUP_MAP.put(firstByte + "s1S", "Italic");
            LOOKUP_MAP.put(firstByte + "s4S", "Condensed");
            LOOKUP_MAP.put(firstByte + "s5S", "Condensed italic");
            LOOKUP_MAP.put(firstByte + "s8S", "Compressed, or extra condensed");
            LOOKUP_MAP.put(firstByte + "s24S", "Expanded");
            LOOKUP_MAP.put(firstByte + "s32S", "Outline");
            LOOKUP_MAP.put(firstByte + "s64S", "Iinline");
            LOOKUP_MAP.put(firstByte + "s128S", "Shadowed");
            LOOKUP_MAP.put(firstByte + "s160S", "Outline shadowed");

            LOOKUP_MAP.put(firstByte + "s-7B", "Ultra Thin");
            LOOKUP_MAP.put(firstByte + "s-6B", "Extra Thin");
            LOOKUP_MAP.put(firstByte + "s-5B", "Thin");
            LOOKUP_MAP.put(firstByte + "s-4B", "Extra Light");
            LOOKUP_MAP.put(firstByte + "s-3B", "Light");
            LOOKUP_MAP.put(firstByte + "s-2B", "Demi Light");
            LOOKUP_MAP.put(firstByte + "s-1B", "Semi Light");
            LOOKUP_MAP.put(firstByte + "s0B", "Medium, Book, or Text");
            LOOKUP_MAP.put(firstByte + "s1B", "Semi Bold");
            LOOKUP_MAP.put(firstByte + "s2B", "Demi Bold");
            LOOKUP_MAP.put(firstByte + "s3B", "Bold");
            LOOKUP_MAP.put(firstByte + "s4B", "Extra Bold");
            LOOKUP_MAP.put(firstByte + "s5B", "Black");
            LOOKUP_MAP.put(firstByte + "s6B", "Extra Black");
            LOOKUP_MAP.put(firstByte + "s7B", "Ultra Black");

            LOOKUP_MAP.put(firstByte + "18C", "GW-3212");
            LOOKUP_MAP.put(firstByte + "0D", "ISO 60: Danish/Norwegian");
            LOOKUP_MAP.put(firstByte + "2D", "Devanagari");
            LOOKUP_MAP.put(firstByte + "1E", "ISO 4: United Kingdom");
            LOOKUP_MAP.put(firstByte + "9E", "Windows 3.1 Latin 2");
            LOOKUP_MAP.put(firstByte + "1F", "ISO 69: French");
            LOOKUP_MAP.put(firstByte + "1G", "ISO 21: German");
            LOOKUP_MAP.put(firstByte + "8G", "Greek-8");
            LOOKUP_MAP.put(firstByte + "9G", "Windows 3.1 Latin/Greek");
            LOOKUP_MAP.put(firstByte + "10G", "PC-851 Latin/Greek");
            LOOKUP_MAP.put(firstByte + "12G", "PC-8 Latin/Greek");
            LOOKUP_MAP.put(firstByte + "0H", "Hebrew-7");
            LOOKUP_MAP.put(firstByte + "7H", "ISO 8859/8 Latin/Hebrew");
            LOOKUP_MAP.put(firstByte + "8H", "Hebrew-8");
            LOOKUP_MAP.put(firstByte + "15H", "PC-862 Latin/Hebrew");
            LOOKUP_MAP.put(firstByte + "0I", "ISO 15: Italian");
            LOOKUP_MAP.put(firstByte + "6J", "Microsoft Publishing");
            LOOKUP_MAP.put(firstByte + "7J", "DeskTop");
            LOOKUP_MAP.put(firstByte + "8J", "Document");
            LOOKUP_MAP.put(firstByte + "9J", "PC-1004");
            LOOKUP_MAP.put(firstByte + "10J", "PS Text");
            LOOKUP_MAP.put(firstByte + "11J", "PS ISO Latin1");
            LOOKUP_MAP.put(firstByte + "12J", "MC Text");
            LOOKUP_MAP.put(firstByte + "13J", "Ventura International");
            LOOKUP_MAP.put(firstByte + "14J", "Ventura US");
            LOOKUP_MAP.put(firstByte + "16J", "Swash Characters");
            LOOKUP_MAP.put(firstByte + "17J", "Small Caps & Old Style Figures");
            LOOKUP_MAP.put(firstByte + "18J", "Old Style Figures");
            LOOKUP_MAP.put(firstByte + "19J", "Fractions");
            LOOKUP_MAP.put(firstByte + "21J", "Lining Figures");
            LOOKUP_MAP.put(firstByte + "22J", "Small Caps and Lining Figures");
            LOOKUP_MAP.put(firstByte + "23J", "Alternate Caps");
            LOOKUP_MAP.put(firstByte + "8K", "Kana-8 - JIS 210");
            LOOKUP_MAP.put(firstByte + "9K", "Korean-8");
            LOOKUP_MAP.put(firstByte + "0L", "Line Draw-7");
            LOOKUP_MAP.put(firstByte + "1L", "HP Block Characters");
            LOOKUP_MAP.put(firstByte + "2L", "Tax Line Draw");
            LOOKUP_MAP.put(firstByte + "8L", "Line Draw-8");
            LOOKUP_MAP.put(firstByte + "9L", "Ventura ITC Zapf Dingbats");
            LOOKUP_MAP.put(firstByte + "10L", "PS ITC Zapf Dingbats");
            LOOKUP_MAP.put(firstByte + "11L", "ITC Zapf Dingbats Series 100");
            LOOKUP_MAP.put(firstByte + "12L", "ITC Zapf Dingbats Series 200");
            LOOKUP_MAP.put(firstByte + "13L", "ITC Zapf Dingbats Series 300");
            LOOKUP_MAP.put(firstByte + "14L", "ITC Zapf Dingbats MS");
            LOOKUP_MAP.put(firstByte + "19L", "Windows Baltic");
            LOOKUP_MAP.put(firstByte + "20L", "Carta");
            LOOKUP_MAP.put(firstByte + "21L", "Ornaments");
            LOOKUP_MAP.put(firstByte + "22L", "Universal News & Commercial Pi");
            LOOKUP_MAP.put(firstByte + "23L", "Chess");
            LOOKUP_MAP.put(firstByte + "24L", "Astrology 1");
            LOOKUP_MAP.put(firstByte + "31L", "Pi Set #1");
            LOOKUP_MAP.put(firstByte + "32L", "Pi Set #2");
            LOOKUP_MAP.put(firstByte + "33L", "Pi Set #3");
            LOOKUP_MAP.put(firstByte + "34L", "Pi Set #4");
            LOOKUP_MAP.put(firstByte + "35L", "Pi Set #5");
            LOOKUP_MAP.put(firstByte + "36L", "Pi Set #6");
            LOOKUP_MAP.put(firstByte + "579L", "Wingdings");
            LOOKUP_MAP.put(firstByte + "0M", "Math-7");
            LOOKUP_MAP.put(firstByte + "1M", "Tech-7");
            LOOKUP_MAP.put(firstByte + "5M", "PS Math");
            LOOKUP_MAP.put(firstByte + "6M", "Ventura Math");
            LOOKUP_MAP.put(firstByte + "8M", "Math-8");
            LOOKUP_MAP.put(firstByte + "10M", "Universal Greek & Math Pi");
            LOOKUP_MAP.put(firstByte + "11M", "TeX Math Extension");
            LOOKUP_MAP.put(firstByte + "12M", "TeX Math Symbol");
            LOOKUP_MAP.put(firstByte + "13M", "TeX Math Italic");
            LOOKUP_MAP.put(firstByte + "19M", "Symbol");
            LOOKUP_MAP.put(firstByte + "0N", "ISO 8859/1 Latin 1");
            LOOKUP_MAP.put(firstByte + "2N", "ISO 8859/2 Latin 2");
            LOOKUP_MAP.put(firstByte + "3N", "ISO 8859/3 Latin 3");
            LOOKUP_MAP.put(firstByte + "4N", "ISO 8859/4 Latin 4");
            LOOKUP_MAP.put(firstByte + "5N", "ISO 8859/9 Latin 5");
            LOOKUP_MAP.put(firstByte + "6N", "ISO 8859/10 Latin 6");
            LOOKUP_MAP.put(firstByte + "9N", "ISO 8859/15 Latin 9");
            LOOKUP_MAP.put(firstByte + "10N", "ISO 8859/5 Latin/Cyrillic");
            LOOKUP_MAP.put(firstByte + "11N", "ISO 8859/6 Latin/Arabic");
            LOOKUP_MAP.put(firstByte + "12N", "ISO 8859/7 Latin/Greek");
            LOOKUP_MAP.put(firstByte + "18N", "Unicode");
            LOOKUP_MAP.put(firstByte + "0O", "OCR-A");
            LOOKUP_MAP.put(firstByte + "1O", "OCR-B");
            LOOKUP_MAP.put(firstByte + "2O", "OCR-M");
            LOOKUP_MAP.put(firstByte + "10O", "MICR (E13B)");
            LOOKUP_MAP.put(firstByte + "0P", "Typewriter Paired APL");
            LOOKUP_MAP.put(firstByte + "1P", "Bit Paired APL");
            LOOKUP_MAP.put(firstByte + "10P", "Expert");
            LOOKUP_MAP.put(firstByte + "11P", "Alternate");
            LOOKUP_MAP.put(firstByte + "12P", "Fraktur");
            LOOKUP_MAP.put(firstByte + "xQ", "Reserved for Specials");
            LOOKUP_MAP.put(firstByte + "0R", "Cyrillic ASCII");
            LOOKUP_MAP.put(firstByte + "1R", "Cyrillic");
            LOOKUP_MAP.put(firstByte + "3R", "PC Cyrillic");
            LOOKUP_MAP.put(firstByte + "9R", "Windows 3.1 Latin/Cyrillic");
            LOOKUP_MAP.put(firstByte + "0S", "ISO 11: Swedish");
            LOOKUP_MAP.put(firstByte + "2S", "ISO 17: Spanish");
            LOOKUP_MAP.put(firstByte + "7S", "HP European Spanish");
            LOOKUP_MAP.put(firstByte + "8S", "HP Latin Spanish");
            LOOKUP_MAP.put(firstByte + "16S", "HP-GL Download");
            LOOKUP_MAP.put(firstByte + "17S", "HP-GL Drafting");
            LOOKUP_MAP.put(firstByte + "18S", "HP-GL Special Symbols");
            LOOKUP_MAP.put(firstByte + "20S", "Sonata");
            LOOKUP_MAP.put(firstByte + "0T", "Thai-8");
            LOOKUP_MAP.put(firstByte + "1T", "TISI 620-2533");
            LOOKUP_MAP.put(firstByte + "5T", "Windows 3.1 Latin 5");
            LOOKUP_MAP.put(firstByte + "8T", "Turkish-8");
            LOOKUP_MAP.put(firstByte + "9T", "PC-8 Turkish");
            LOOKUP_MAP.put(firstByte + "10T", "Teletex");
            LOOKUP_MAP.put(firstByte + "0U", "ISO 6: ASCII");
            LOOKUP_MAP.put(firstByte + "1U", "Legal");
            LOOKUP_MAP.put(firstByte + "4U", "Roman-9");
            LOOKUP_MAP.put(firstByte + "5U", "HPL");
            LOOKUP_MAP.put(firstByte + "7U", "OEM-1");
            LOOKUP_MAP.put(firstByte + "8U", "Roman-8");
            LOOKUP_MAP.put(firstByte + "9U", "Windows 3.0 Latin 1");
            LOOKUP_MAP.put(firstByte + "10U", "PC-8, Code Page 437");
            LOOKUP_MAP.put(firstByte + "11U", "PC-8 D/N, Danish/Norwegian");
            LOOKUP_MAP.put(firstByte + "12U", "PC-850, Multilingual");
            LOOKUP_MAP.put(firstByte + "13U", "PC-858");
            LOOKUP_MAP.put(firstByte + "15U", "Pi Font");
            LOOKUP_MAP.put(firstByte + "16U", "PC-857");
            LOOKUP_MAP.put(firstByte + "17U", "PC-852, Latin 2");
            LOOKUP_MAP.put(firstByte + "19U", "Windows 3.1 Latin 1");
            LOOKUP_MAP.put(firstByte + "20U", "PC-860 Portugal");
            LOOKUP_MAP.put(firstByte + "21U", "PC-861 Iceland");
            LOOKUP_MAP.put(firstByte + "23U", "PC-863 Canada-French");
            LOOKUP_MAP.put(firstByte + "25U", "PC-865 Norway");
            LOOKUP_MAP.put(firstByte + "26U", "PC-775");
            LOOKUP_MAP.put(firstByte + "8V", "Arabic-8");
            LOOKUP_MAP.put(firstByte + "9V", "Windows 3.1 Latin/Arabic");
            LOOKUP_MAP.put(firstByte + "10V", "Code Page 864 Latin/Arabic");
            LOOKUP_MAP.put(firstByte + "0Y", "3 of 9 Barcode");
            LOOKUP_MAP.put(firstByte + "1Y", "Industrial 2 of 5 Barcode");
            LOOKUP_MAP.put(firstByte + "2Y", "Matrix 2 of 5 Barcode");
            LOOKUP_MAP.put(firstByte + "4Y", "Interleaved 2 of 5 Barcode");
            LOOKUP_MAP.put(firstByte + "5Y", "CODABAR Barcode");
            LOOKUP_MAP.put(firstByte + "6Y", "MSI/Plessey Barcode");
            LOOKUP_MAP.put(firstByte + "7Y", "Code 11 Barcode");
            LOOKUP_MAP.put(firstByte + "8Y", "UPC/EAN Barcode");
            LOOKUP_MAP.put(firstByte + "14Y", "MICR");
            LOOKUP_MAP.put(firstByte + "15Y", "USPS ZIP");
            LOOKUP_MAP.put(firstByte + "0A", "Math-7");
            LOOKUP_MAP.put(firstByte + "0B", "Line Draw-7");
            LOOKUP_MAP.put(firstByte + "0C", "HP Large Characters");
            LOOKUP_MAP.put(firstByte + "1D", "ISO 61: Norwegian Version 2");
            LOOKUP_MAP.put(firstByte + "0E", "Roman Extension");
            LOOKUP_MAP.put(firstByte + "0F", "ISO 25: French");
            LOOKUP_MAP.put(firstByte + "0G", "HP German");
            LOOKUP_MAP.put(firstByte + "0K", "ISO 14: JIS ASCII");
            LOOKUP_MAP.put(firstByte + "1K", "ISO 13: Katakana");
            LOOKUP_MAP.put(firstByte + "2K", "ISO 57: Chinese");
            LOOKUP_MAP.put(firstByte + "1S", "HP Spanish");
            LOOKUP_MAP.put(firstByte + "3S", "ISO 10: Swedish");
            LOOKUP_MAP.put(firstByte + "4S", "ISO 16: Portuguese");
            LOOKUP_MAP.put(firstByte + "5S", "ISO 84: Portuguese");
            LOOKUP_MAP.put(firstByte + "6S", "ISO 85: Spanish");
            LOOKUP_MAP.put(firstByte + "2U", "ISO 2: International Reference");
            LOOKUP_MAP.put(firstByte + "0V", "Arabic");

        }

        LOOKUP_MAP.put("(3@", "Default primary font characteristics");
        LOOKUP_MAP.put(")3@", "Default secondary font characteristics");

        LOOKUP_MAP.put("&d0D", "Fixed position");
        LOOKUP_MAP.put("&d3D", "Floating position");

        LOOKUP_MAP.put("*c0F", "Delete all soft fonts");
        LOOKUP_MAP.put("*c1F", "Delete all temporary soft fonts");
        LOOKUP_MAP.put("*c2F", "Delete soft font");
        LOOKUP_MAP.put("*c3F", "Delete Character Code");
        LOOKUP_MAP.put("*c4F", "Make soft font temporary");
        LOOKUP_MAP.put("*c5F", "Make soft font permanent");
        LOOKUP_MAP.put("*c6F", "Copy/Assign current invoked font as temporary");

        LOOKUP_MAP.put("*c0S", "Delete all temporary and permanent user-defined symbol sets");
        LOOKUP_MAP.put("*c1S", "Delete all temporary user-defined symbol sets");
        LOOKUP_MAP.put("*c2S", "Delete current user-defined symbol set");
        LOOKUP_MAP.put("*c4S", "Make current user-defined symbol set temporary");
        LOOKUP_MAP.put("*c5S", "Make current user-defined symbol set permanent");

        LOOKUP_MAP.put("&f0X", "Start macro definition");
        LOOKUP_MAP.put("&f1X", "Stop macro definition");
        LOOKUP_MAP.put("&f2X", "Execute macro");
        LOOKUP_MAP.put("&f3X", "Call macro");
        LOOKUP_MAP.put("&f4X", "Enable macro for automatic overlay");
        LOOKUP_MAP.put("&f5X", "Disable automatic overlay");
        LOOKUP_MAP.put("&f6X", "Delete all macros");
        LOOKUP_MAP.put("&f7X", "Delete all temporary macros");
        LOOKUP_MAP.put("&f8X", "Delete macro");
        LOOKUP_MAP.put("&f9X", "Make macro temporary");
        LOOKUP_MAP.put("&f10X", "Make macro permanent");

        LOOKUP_MAP.put("*v0N", "Transparent");
        LOOKUP_MAP.put("*v1N", "Opaque");

        LOOKUP_MAP.put("*v0O", "Transparent");
        LOOKUP_MAP.put("*v1O", "Opaque");

        LOOKUP_MAP.put("*v0T", "Solid black");
        LOOKUP_MAP.put("*v1T", "Solid white");
        LOOKUP_MAP.put("*v2T", "Shading pattern");
        LOOKUP_MAP.put("*v3T", "Cross-hatch pattern");
        LOOKUP_MAP.put("*v4T", "User-defined pattern");

        LOOKUP_MAP.put("*p0R", "Rotate patterns with print direction");
        LOOKUP_MAP.put("*p1R", "Keep patterns fixed");

        LOOKUP_MAP.put("*c0Q", "Delete all patterns");
        LOOKUP_MAP.put("*c1Q", "Delete all temporary patterns");
        LOOKUP_MAP.put("*c2Q", "Delete pattern");
        LOOKUP_MAP.put("*c3Q", "Reserved");
        LOOKUP_MAP.put("*c4Q", "Make pattern temporary");
        LOOKUP_MAP.put("*c5Q", "Make pattern permanent");

        LOOKUP_MAP.put("*c0P", "Black fill");
        LOOKUP_MAP.put("*c1P", "Erase (white) fill");
        LOOKUP_MAP.put("*c2P", "Shaded fill");
        LOOKUP_MAP.put("*c3P", "Cross-hatch fill");
        LOOKUP_MAP.put("*c4P", "User-defined pattern fill");
        LOOKUP_MAP.put("*c5P", "Current pattern fill");

        LOOKUP_MAP.put("*r0F", "Raster image prints in orientation of logical page");
        LOOKUP_MAP.put("*r3F", "Raster image prints along the width of the physical page");

        LOOKUP_MAP.put("*r0A", "Start graphics at default left graphics margin");
        LOOKUP_MAP.put("*r1A", "Start graphics at current cursor position");

        LOOKUP_MAP.put("*b0M", "Unencoded");
        LOOKUP_MAP.put("*b1M", "Run-length encoding");
        LOOKUP_MAP.put("*b2M", "Tagged Imaged File Format");
        LOOKUP_MAP.put("*b3M", "Delta row compression");
        LOOKUP_MAP.put("*b4M", "Reserved");
        LOOKUP_MAP.put("*b5M", "Adaptive compression");
        LOOKUP_MAP.put("*b9M", "Replacement Delta Row");

        LOOKUP_MAP.put("*s0T", "Invalid location");
        LOOKUP_MAP.put("*s1T", "Currently selected");
        LOOKUP_MAP.put("*s2T", "All Locations");
        LOOKUP_MAP.put("*s3T", "Internal");
        LOOKUP_MAP.put("*s4T", "Downloaded Entity");
        LOOKUP_MAP.put("*s5T", "Cartridge");
        LOOKUP_MAP.put("*s7T", "User-installable ROM device");

        LOOKUP_MAP.put("*s0I", "Font");
        LOOKUP_MAP.put("*s1I", "Macro");
        LOOKUP_MAP.put("*s2I", "User-defined pattern");
        LOOKUP_MAP.put("*s3I", "Symbol Set");
        LOOKUP_MAP.put("*s4I", "Font Extended");

        LOOKUP_MAP.put("&r0F", "Flush all complete pages");
        LOOKUP_MAP.put("&r1F", "Flush all pages");

        LOOKUP_MAP.put("%0B", "Position pen at previous HP-GL/2 pen position");
        LOOKUP_MAP.put("%1B", "Position pen at current PCL cursor position");

        LOOKUP_MAP.put("%0A", "Position cursor at previous PCL cursor position");
        LOOKUP_MAP.put("%1A", "Position cursor at current HP-GL/2 pen position");

        LOOKUP_MAP.put("&l0S", "Simplex");
    }

    @Override
    PrinterCommandDetails execute(final PrinterCommand command, final PclDumperContext context) {
        final String summary = LOOKUP_MAP.get(command.toDisplayString());

        if (command instanceof ParameterizedPclCommand) {
            final ParameterizedPclCommand cmd = (ParameterizedPclCommand) command;
            final byte[] data = cmd.getDataSection();

            if (data != null) {
                return new PrinterCommandDetails(summary, createHexDump(data));
            }
        }

        return new PrinterCommandDetails(summary);
    }

    private static List<String> createHexDump(byte[] data) {
        // Let's be lazy here.... Let all the work be done by Apache Commons... This is not really
        // a high-performance way to create the hex dump for us, but note that PCL-Dumper is not
        // required to be a high-performance tool... So I guess it is "ok" here to be lazy...
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            HexDump.dump(data, 0, baos, 0);
        } catch (final IOException e) {
            // An IOException should never ever been throws because we write to memory...
            return Collections.<String>emptyList();
        }

        final byte[] hex = baos.toByteArray();
        IOUtils.closeQuietly(baos);

        final ByteArrayInputStream bais = new ByteArrayInputStream(hex);
        final InputStreamReader reader = new InputStreamReader(bais, Charset.defaultCharset());
        final LineIterator iter = new LineIterator(reader);

        final List<String> result = new ArrayList<>();
        while (iter.hasNext()) {
            result.add(iter.next());
        }

        iter.close();

        return result;
    }
}
