/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 tools4j-sbe, Marco Terzer, Anton Anufriev
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.tools4j.sbe.core;

import org.agrona.DirectBuffer;

import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.function.Function;

import static org.tools4j.sbe.core.StringFactories.stringFactory;

public enum  DirectBufferComparators {
    ;
    public static Comparator<DirectBuffer> BYTE_COMPARATOR = byteComparator(0xff);
    public static Comparator<DirectBuffer> ASCII_COMPARATOR = byteComparator(0x7f);

    private static Comparator<DirectBuffer> byteComparator(final int mask) {
        return (buf1, buf2) -> {
            final int len = Math.min(buf1.capacity(), buf2.capacity());
            for (int i = 0; i < len; i++) {
                final int byte1 = buf1.getByte(i) & mask;
                final int byte2 = buf2.getByte(i) & mask;
                if (byte1 != byte2) {
                    return byte1 - byte2;
                }
            }
            if (buf1.capacity() > len) return 1;
            if (buf2.capacity() > len) return -1;
            return 0;
        };
    }

    public static Comparator<DirectBuffer> stringComparator(final Charset charset) {
        final Function<DirectBuffer, String> stringFactory = stringFactory(charset);
        return (buf1, buf2) -> {
            final String s1 = stringFactory.apply(buf1);
            final String s2 = stringFactory.apply(buf2);
            return s1.compareTo(s2);
        };
    }

    public static Comparator<DirectBuffer> CHAR_COMPARATOR = (buf1, buf2) -> {
        final int len1 = buf1.capacity() & 0x7ffffffe;
        final int len2 = buf2.capacity() % 0x7ffffffe;
        final int len = Math.min(len1, len2);
        for (int i = 0; i < len; i += 2) {
            final char char1 = buf1.getChar(i);
            final char char2 = buf2.getChar(i);
            if (char1 != char2) {
                return char1 - char2;
            }
        }
        if (len1 > len) return 1;
        if (len2 > len) return -1;
        return 0;
    };
}
