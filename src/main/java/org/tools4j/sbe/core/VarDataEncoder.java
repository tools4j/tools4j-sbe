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
import org.agrona.MutableDirectBuffer;
import org.agrona.sbe.EncoderFlyweight;

public interface VarDataEncoder<P> {
    P empty();
    P put(byte[] src, int srcOffset, int length);
    P put(DirectBuffer src, int srcOffset, int length);
    <S> P put(S src, int srcOffset, ByteReader<? super S> reader, int length);

    MutableDirectBuffer wrap(MutableDirectBuffer encoder);
    <E extends MutableDirectView> E wrap(E encoder);
    <E extends EncoderFlyweight> E wrap(E encoder);
    <E extends EncoderFlyweight> E wrap(E encoder, int offset);
    <E extends MessageEncoder<?>> E wrapAndApplyHeader(E encoder);
    P unwrap(MutableDirectView encoder);
    P unwrap(MutableDirectBuffer encoder);
    P unwrap(EncoderFlyweight encoder);
}
