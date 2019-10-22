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
import org.agrona.sbe.EncoderFlyweight;
import org.agrona.sbe.MessageEncoderFlyweight;

public interface EnvelopeEncoder<P> extends MessageEncoder<EnvelopeEncoder<P>> {

//    static EnvelopeEncoder<StandardPayloadAccess> create() {
//        return create(new DefaultStandardPayloadAccess());
//    }
//
//    static <P> EnvelopeEncoder<P> create(PayloadAccessProvider<? extends P> payloadAccessProvider) {
//        return new DefaultEnvelopeEncoder<>(payloadAccessProvider);
//    }

    EnvelopeEncoder<P> time(long time);
    EnvelopeEncoder<P> seqNo(long seqNo);

    P data(byte[] src, int srcOffset, int length);
    P data(DirectBuffer src, int srcOffset, int length);
    <S> P data(S src, int srcOffset, ByteReader<? super S> reader, int length);
    Data<P> dataStart();
    P dataEmpty();

    interface Data<P> {
        <E extends EncoderFlyweight> E wrap(E encoder);
        <E extends EncoderFlyweight> E wrap(E encoder, int offset);
        <E extends MessageEncoder<?>> E wrapAndApplyHeader(E encoder);
        P dataComplete();
    }
}
