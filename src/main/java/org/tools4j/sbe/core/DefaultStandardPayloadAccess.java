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
import org.agrona.sbe.CompositeEncoderFlyweight;
import org.agrona.sbe.MessageEncoderFlyweight;

public class DefaultStandardPayloadAccess implements StandardPayloadAccess, PayloadAccessProvider<StandardPayloadAccess> {
    private MutableDirectBuffer buffer;
    private int offset;
    private int headerLength;
    private int messageLength;

    @Override
    public StandardPayloadAccess payload(final CompositeEncoderFlyweight header, final MessageEncoderFlyweight message) {
        if (header != null) {
            offset = header.offset();
            headerLength = header.encodedLength();
            if (message != null) {
                buffer = message.buffer();
                messageLength = message.encodedLength();
                return this;
            }
            buffer = header.buffer();
            messageLength = 0;
            return this;
        }
        if (message != null) {
            buffer = message.buffer();
            offset = message.offset();
            headerLength = 0;
            messageLength = message.encodedLength();
            return this;
        }
        close();
        return this;
    }

    @Override
    public MutableDirectBuffer buffer() {
        return buffer;
    }

    @Override
    public int offset() {
        return offset;
    }

    @Override
    public int headerLength() {
        return headerLength;
    }

    @Override
    public int messageLength() {
        return messageLength;
    }

    @Override
    public int totalLength() {
        return headerLength + messageLength;
    }

    @Override
    public void close() {
        buffer = null;
        offset = 0;
        headerLength = 0;
        messageLength = 0;
    }
}
