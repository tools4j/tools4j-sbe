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
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Function;

public enum StringFactories {
    ;
    public static Function<DirectBuffer, String> ASCII_STRING_FACTORY = stringFactory(StandardCharsets.US_ASCII);
    public static Function<DirectBuffer, String> UTF8_STRING_FACTORY = stringFactory(StandardCharsets.UTF_8);
    public static Function<DirectBuffer, String> ISO_8859_1_STRING_FACTORY = stringFactory(StandardCharsets.ISO_8859_1);

    public static Function<DirectBuffer, String> stringFactory(final Charset charset) {
        Objects.requireNonNull(charset);
        return buffer -> {
            byte[] bytes = buffer.byteArray();
            if (bytes == null) {
                bytes = new byte[buffer.capacity()];
                buffer.getBytes(0, bytes);
            }
            return new String(bytes, charset);
        };
    };

    public static Function<DirectBuffer, String> CHAR_STRING_FACTORY = buffer -> {
        final int len = buffer.capacity();
        final int charLen = len / 2;
        final char[] chars = new char[charLen];
        for (int i = 0; i < charLen; i++) {
            chars[i] = buffer.getChar(i * 2);
        }
        return new String(chars);
    };
}
