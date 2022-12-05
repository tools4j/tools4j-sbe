/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019-2022 fix4j-sbe, Marco Terzer, Anton Anufriev
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

import org.fix4j.sbe.meta.MetaData;
import org.fix4j.sbe.bytes.ByteReader;
import org.fix4j.sbe.bytes.CharReader;

public interface StringEncoder<P> {
    MetaData.CharEncoded metaData();
    P empty();
    P put(String s);
    P put(CharSequence s);
    <T> P put(T value, ValueEncoder<? super T> encoder);
    P put(byte[] src, int srcOffset, int length);
    P put(char[] src, int srcOffset, int length);
    <S> P put(S src, int srcOffset, ByteReader<? super S> reader, int length);
    <S> P put(S src, int srcOffset, CharReader<? super S> reader, int length);

    interface FixedLen<P> extends StringEncoder<P> {
        @Override
        MetaData.Char metaData();
    }

    interface VarLen<P> extends StringEncoder<P> {
        @Override
        MetaData.VarChar metaData();
    }
}
