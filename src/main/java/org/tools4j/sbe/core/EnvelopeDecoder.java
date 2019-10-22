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

import org.agrona.MutableDirectBuffer;
import org.agrona.sbe.CompositeDecoderFlyweight;
import org.agrona.sbe.MessageDecoderFlyweight;

public interface EnvelopeDecoder extends MessageDecoder<EnvelopeDecoder> {
//    static EnvelopeDecoder create() {
//        return new DefaultEnvelopeDecoder();
//    }

    long time();
    long seqNo();

    int dataLength();
    <D> int data(byte[] dst, int dstOffset, int length);
    <D> int data(MutableDirectBuffer dst, int dstOffset, int length);
    <D> int data(D dst, int dstOffset, ByteWriter<? super D> writer, int length);
    Data data();

    interface Data {
        int length();
        <D extends CompositeDecoderFlyweight> D wrap(D decoder);
        <D extends MessageDecoderFlyweight> D data(D decoder);
        <D extends MessageDecoderFlyweight> D data(D decoder, int offset, int actingBlockLength, int actingVersion);
    }
}
