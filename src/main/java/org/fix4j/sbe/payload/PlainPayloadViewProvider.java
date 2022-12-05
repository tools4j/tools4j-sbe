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
package org.fix4j.sbe.payload;

import org.agrona.sbe.CompositeEncoderFlyweight;
import org.agrona.sbe.MessageEncoderFlyweight;

public class PlainPayloadViewProvider implements PayloadViewProvider<PlainPayloadView> {

    private final View view = new View();

    @Override
    public PlainPayloadView payload(final CompositeEncoderFlyweight header, final MessageEncoderFlyweight message) {
        return view.init(header, message);
    }

    private static final class View implements PlainPayloadView {
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
        public CompositeEncoderFlyweight header() {
            return header;
        }

        @Override
        public MessageEncoderFlyweight message() {
            return message;
        }

        @Override
        public void close() {
            header = null;
            message = null;
        }
    }
}
