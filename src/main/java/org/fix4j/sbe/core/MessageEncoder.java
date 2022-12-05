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

import org.agrona.MutableDirectBuffer;
import org.agrona.sbe.MessageEncoderFlyweight;

public interface MessageEncoder<E extends MessageEncoder<E>> extends MessageEncoderFlyweight, AutoCloseable {
    /**
     * Wrap a buffer for encoding at a given offset.
     *
     * @param buffer to be wrapped and into which the type will be encoded.
     * @param offset at which the encoded object will be begin.
     * @return the encoder for the block part of the message
     */
    E wrap(MutableDirectBuffer buffer, int offset);
    E wrapAndApplyHeader(MutableDirectBuffer buffer, int offset);
    E unwrap();

    @Override
    default void close() {
        unwrap();
    }
}
