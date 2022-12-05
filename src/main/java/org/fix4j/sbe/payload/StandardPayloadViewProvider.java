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
package org.fix4j.sbe.payload;

import org.agrona.MutableDirectBuffer;
import org.agrona.sbe.CompositeEncoderFlyweight;
import org.agrona.sbe.MessageEncoderFlyweight;

public class StandardPayloadViewProvider implements PayloadViewProvider<StandardPayloadView> {

    private final View view = new View();

    @Override
    public StandardPayloadView payload(final CompositeEncoderFlyweight header, final MessageEncoderFlyweight message) {
        return view.init(header, message);
    }

    private static final class View implements StandardPayloadView {
        private CompositeEncoderFlyweight header;
        private MessageEncoderFlyweight message;

        View init(final CompositeEncoderFlyweight header, final MessageEncoderFlyweight message) {
            if (header != null && message != null && header.buffer() != message.buffer()) {
                throw new IllegalArgumentException("header and message must use the same buffer instance");
            }
            this.header = header;
            this.message = message;
            return this;
        }

        @Override
        public int sbeSchemaId() {
            return message.sbeSchemaId();
        }

        @Override
        public int sbeSchemaVersion() {
            return message.sbeSchemaVersion();
        }

        @Override
        public int sbeTemplateId() {
            return message.sbeTemplateId();
        }

        @Override
        public String sbeSemanticType() {
            return message.sbeSemanticType();
        }

        @Override
        public int sbeBlockLength() {
            return message.sbeBlockLength();
        }

        @Override
        public MutableDirectBuffer buffer() {
            return message.buffer();
        }

        @Override
        public int offset() {
            return header != null ? header.offset() : message != null ? message.offset() : 0;
        }

        @Override
        public int headerLength() {
            return header != null ? header.encodedLength() : 0;
        }

        @Override
        public int messageLength() {
            return message != null ? message.encodedLength() : 0;
        }

        @Override
        public int totalLength() {
            return headerLength() + messageLength();
        }

        @Override
        public void close() {
            header = null;
            message = null;
        }
    }
}
