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

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.sbe.CompositeDecoderFlyweight;
import org.agrona.sbe.MessageDecoderFlyweight;
import org.fix4j.sbe.meta.MetaData;
import org.fix4j.sbe.bytes.ByteWriter;

public interface DataDecoder {
    MetaData.VarData metaData();
    int length();
    int get(byte[] dst, int dstOffset, int length);
    int get(MutableDirectBuffer dst, int dstOffset, int length);
    <T> T get(ValueDecoder<T> decoder);
    <D> int get(D dst, int dstOffset, ByteWriter<? super D> writer, int length);

    DirectBuffer wrap(DirectBuffer decoder);
    <D extends DirectView> D wrap(D decoder);
    <D extends CompositeDecoderFlyweight> D wrap(D decoder);
    <D extends MessageDecoderFlyweight> D wrap(D decoder);
    <D extends MessageDecoderFlyweight> D wrap(D decoder, int offset, int actingBlockLength, int actingVersion);
}
