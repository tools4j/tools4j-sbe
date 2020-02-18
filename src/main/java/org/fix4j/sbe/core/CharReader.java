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
package org.fix4j.sbe.core;

import org.agrona.DirectBuffer;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

@FunctionalInterface
public interface CharReader<S> {
    char read(S source, int index);

    CharReader<DirectBuffer> DIRECT_BUFFER_READER = DirectBuffer::getChar;
    CharReader<DirectBuffer> DIRECT_BUFFER_READER_ASCII = (source, index) -> (char) source.getByte(index);
    CharReader<ByteBuffer> BYTE_BUFFER_READER = ByteBuffer::getChar;
    CharReader<ByteBuffer> BYTE_BUFFER_READER_ASCII = (source, index) -> (char)source.get(index);
    CharReader<CharBuffer> CHAR_BUFFER_READER = CharBuffer::get;
    CharReader<CharSequence> CHAR_SEQUENCE_READER = CharSequence::charAt;
    CharReader<String> STRING_READER = String::charAt;
    CharReader<byte[]> BYTE_ARRAY_ASCII_READER = (source, index) -> (char)source[index];
    CharReader<char[]> CHAR_ARRAY_READER = (source, index) -> source[index];
}
