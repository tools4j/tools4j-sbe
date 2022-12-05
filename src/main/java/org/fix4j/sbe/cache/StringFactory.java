/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 fix4j-sbe, Marco Terzer, Anton Anufriev
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
package org.fix4j.sbe.cache;

import org.agrona.DirectBuffer;
import org.fix4j.sbe.core.ValueDecoder;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static java.util.Objects.requireNonNull;

public enum StringFactory implements ValueDecoder<String> {
    ASCII(StandardCharsets.US_ASCII),
    UTF8(StandardCharsets.UTF_8),
    ISO_8859_1(StandardCharsets.ISO_8859_1),
    CHAR(stringDecoder());

    private final ValueDecoder<String> decoder;

    StringFactory(final Charset charset) {
        this(stringDecoder(charset));
    }

    StringFactory(final ValueDecoder<String> decoder) {
        this.decoder = requireNonNull(decoder);
    }

    @Override
    public String get(final DirectBuffer buffer, final int offset, final int length) {
        return decoder.get(buffer, offset, length);
    }

    public ValueDecoder<String> valueDecoder() {
        return decoder;
    }

    public static ValueDecoder<String> stringDecoder(final Charset charset) {
        requireNonNull(charset);
        return (buffer, offset, length) -> {
            byte[] bytes = buffer.byteArray();
            if (bytes != null) {
                return new String(bytes, offset, length, charset);
            }
            final ByteBuffer bbuf = buffer.byteBuffer();
            if (bbuf != null && bbuf.hasArray()) {
                bytes = bbuf.array();
                return new String(bytes, offset + bbuf.arrayOffset(), length, charset);
            }
            bytes = new byte[length];
            buffer.getBytes(offset, bytes, 0, length);
            return new String(bytes, charset);
        };
    };

    private static ValueDecoder<String> stringDecoder() {
        return (buffer, offset, length) -> {
            final int charLen = length / Character.BYTES;
            final char[] chars = new char[charLen];
            for (int i = 0; i < charLen; i++) {
                chars[i] = buffer.getChar(offset + i * Character.BYTES);
            }
            return new String(chars);
        };
    }
}
