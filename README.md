# pcldumper [![Build Status](https://travis-ci.org/michaelknigge/pcldumper.svg?branch=master)](https://travis-ci.org/michaelknigge/pcldumper) [![codecov.io](https://codecov.io/github/michaelknigge/pcldumper/coverage.svg?branch=master)](https://codecov.io/github/michaelknigge/pcldumper?branch=master) [![Coverity Status](https://scan.coverity.com/projects/11868/badge.svg)](https://scan.coverity.com/projects/11868) [![Download](https://api.bintray.com/packages/michaelknigge/maven/pcldumper/images/download.svg) ](https://bintray.com/michaelknigge/maven/pcldumper/_latestVersion)

Java command line tool (and library as well) for dumping [PCL](https://en.wikipedia.org/wiki/Printer_Command_Language) printer data streams. [pclbox](https://github.com/michaelknigge/pclbox) is used for parsing the data stream.

# Dependencies
[Apache Commons-IO](http://commons.apache.org/proper/commons-io/) is required in any case. If pcldumper is used as a command line tool then [Apache Commons-CLI](http://commons.apache.org/proper/commons-cli/) is also required.

# Usage (command line tool)
For your convenience pcldumper comes in two flavours - as a regular JAR and as a single runnable JAR (called a "fat JAR") that contains all required dependencies. This runnable JAR is very handy if pcldumper is used as a command line tool because you do not have to specify the Java CLASSPATH with all the required dependencies yourself.

Just invoke the pcldumper from a command line without any additional parameter to get a list of all available options:

```
$ java -jar pcldumper-1.2-all.jar -h
usage: pcldumper [OPTION]... [FILE]

PCL-Dumper reads a PCL printer data stream and dumps every printer
command.

 -f,--file <arg>   output file for the dump
 -h,--help         shows this help
 -o,--offsets      show the offsets of the printer commands
 -q,--quiet        do not show the PCL-Dumper header
 -v,--verbose      show more details

Please report issues at https://github.com/michaelknigge/pcldumper/issues
```

# Usage (Java library)
If you want to use pcldumper in your own code to create PCL dumps, you need to use the `PclDumperBuilder` to build a `PclDumper`. Then invoke the method `dump` of the `PclDumper` to create the dump. That's all.

# Examples
Dump of a PCL file without offsets and without details:

```
$ java -jar pcldumper-1.2-all.jar -q myfile.pcl
PCL      %-12345X        Universal Exit Language
PJL      @PJL            @PJL
PJL      @PJL            @PJL COMMENT THIS IS A TEST CASE
PJL      @PJL            @PJL ENTER LANGUAGE=PCL
PCL      E               Printer Reset
PCL      &l26A           Page Size (A4)
TEXT                     This file contains all types printer commands that are supported
CNTL     0x0D            Carriage Return
CNTL     0x0A            Line Feed
TEXT                     by PclDumper.
PCL      *c72W           User Defined Pattern
PCL      %0B             Enter HP-GL/2 Mode (Position pen at previous HP-GL/2 pen position)
HPGL     IN              Initialize
HPGL     SP              Select Pen
HPGL     CO              Comment
HPGL     SC              Screened Vectors
PCL      %0A             Enter PCL Mode (Position cursor at previous PCL cursor position)
CNTL     0x0D            Carriage Return
CNTL     0x0A            Line Feed
PCL      %-12345X        Universal Exit Language
```

Dump of a PCL file with offsets and with details:

```
$ java -jar pcldumper-1.2-all.jar -qvo myfile.pcl
00000000 : PCL      %-12345X        Universal Exit Language
00000009 : PJL      @PJL            @PJL
0000000F : PJL      @PJL            @PJL COMMENT THIS IS A TEST CASE
0000005C : PJL      @PJL            @PJL ENTER LANGUAGE=PCL
00000075 : PCL      E               Printer Reset
00000077 : PCL      &l26A           Page Size (A4)
0000007D : TEXT                     Printable text
                                    >>> Length (Bytes) : 64
                                    >>> Parsing Method : Default, 1 byte per character
                                    >>> Hexadecimal    : 546869732066696C6520636F6E7461696E7320616C6C207479706573207072696E74657220636F6D6D616E647320746861742061726520737570706F72746564
                                    >>> Decoded        : This file contains all types printer commands that are supported
000000BD : CNTL     0x0D            Carriage Return
000000BE : CNTL     0x0A            Line Feed
000000BF : TEXT                     Printable text
                                    >>> Length (Bytes) : 13
                                    >>> Parsing Method : Default, 1 byte per character
                                    >>> Hexadecimal    : 62792050636C44756D7065722E
                                    >>> Decoded        : by PclDumper.
000000CC : PCL      *c72W           User Defined Pattern
                                    >>> 00000000 00 00 01 00 00 10 00 20 FF FF FF FF 7F FF FF FE ....... ........
                                    >>> 00000010 3F FF FF FC 1F FF FF F8 0F FF FF F0 07 FF FF E0 ?...............
                                    >>> 00000020 03 FF FF C0 01 FF FF 80 00 FF FF 00 00 7F FE 00 ................
                                    >>> 00000030 00 3F FC 00 00 1F F8 00 00 0F F0 00 00 07 E0 00 .?..............
                                    >>> 00000040 00 03 C0 00 00 01 80 00                         ........
0000011A : PCL      %0B             Enter HP-GL/2 Mode (Position pen at previous HP-GL/2 pen position)
0000011E : HPGL     IN              Initialize
                                    >>> IN
00000121 : HPGL     SP              Select Pen
                                    >>> SP1
00000125 : HPGL     CO              Comment
                                    >>> CO"This includes HP/GL-2 comments in double quotes"
00000159 : HPGL     SC              Screened Vectors
                                    >>> SC0,100,0,100
0000019D : PCL      %0A             Enter PCL Mode (Position cursor at previous PCL cursor position)
000001A1 : CNTL     0x0D            Carriage Return
000001A2 : CNTL     0x0A            Line Feed
000001A3 : CNTL     0x0D            Carriage Return
000001A4 : CNTL     0x0A            Line Feed
000001A5 : PCL      &p48X           Transparent Print Data
                                    >>> 00000000 41 6E 64 20 74 65 78 74 20 69 6E 20 50 43 4C 2D And text in PCL-
                                    >>> 00000010 43 6F 6D 6D 61 6E 64 20 22 54 72 61 6E 73 70 61 Command "Transpa
                                    >>> 00000020 72 65 6E 74 20 50 72 69 6E 74 20 44 61 74 61 22 rent Print Data"
000001DB : CNTL     0x0D            Carriage Return
000001DC : CNTL     0x0A            Line Feed
000001FE : PCL      &t1008P         Text Parsing Method (UTF-8)
00000206 : PCL      (18N            Primary Symbol Set (Unicode)
0000020B : PCL      (s0P            Primary Spacing (Fixed spacing)
00000210 : PCL      (s6H            Primary Pitch (Characters Per Inch)
00000212 : PCL      (s0S            Primary Style (Upright, solid)
00000214 : PCL      (s0B            Primary Stroke Weight (Medium, Book, or Text)
00000216 : PCL      (s4099T         Primary Typeface (Courier from AGFA)
0000021B : TEXT                     Printable text
                                    >>> Length (Bytes) : 5
                                    >>> Parsing Method : UTF-8
                                    >>> Hexadecimal    : C2A3E282AC
                                    >>> Decoded        : £€
00000220 : PCL      %-12345X        Universal Exit Language
```
