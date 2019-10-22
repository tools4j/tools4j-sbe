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
import org.agrona.concurrent.UnsafeBuffer;

import java.util.Iterator;

public class DefaultExecRptDecoder implements ExecRptDecoder {

    private final trading.ExecRptDecoder decoder = new trading.ExecRptDecoder();
    private final DefaultLegGroup legGroup = new DefaultLegGroup();
    private final DefaultRejectText rejectText = new DefaultRejectText();
    private final DirectBuffer nullBuffer = new UnsafeBuffer(0, 0);

    private void cleanup() {
        legGroup.unwrap();
        rejectText.unwrap();
    }

    @Override
    public ExecRptDecoder wrap(final DirectBuffer buffer, final int offset, final int actingBlockLength, final int actingVersion) {
        decoder.wrap(buffer, offset, actingBlockLength, actingVersion);
        cleanup();
        return this;
    }

    @Override
    public ExecRptDecoder unwrap() {
        decoder.wrap(null, 0, 0, 0);
        cleanup();
        return this;
    }

    @Override
    public ExecRptDecoder reset() {
        decoder.limit(offset() + sbeBlockLength());
        cleanup();
        return this;
    }

    @Override
    public DirectBuffer buffer() {
        return decoder.buffer();
    }

    @Override
    public int offset() {
        return decoder.offset();
    }

    @Override
    public int sbeSchemaId() {
        return decoder.sbeSchemaId();
    }

    @Override
    public int sbeSchemaVersion() {
        return decoder.sbeSchemaVersion();
    }

    @Override
    public int sbeTemplateId() {
        return decoder.sbeTemplateId();
    }

    @Override
    public int sbeBlockLength() {
        return decoder.sbeBlockLength();
    }

    @Override
    public String sbeSemanticType() {
        return decoder.sbeSemanticType();
    }

    @Override
    public int encodedLength() {
        return decoder.encodedLength();
    }

    @Override
    public String symbol() {
        return decoder.symbol();
    }

    @Override
    public LegGroup legs() {
        return legGroup.init();
    }

    @Override
    public int rejectTextLength() {
        return rejectText.init().rejectTextLength();
    }

    @Override
    public String rejectText() {
        return rejectText.init().rejectText();
    }

    @Override
    public <D> int rejectText(final D dst, final int dstOffset, final ByteWriter<? super D> writer, final int length) {
        return rejectText.init().rejectText(dst, dstOffset, writer, length);
    }

    @Override
    public <D> int rejectText(final D dst, final int dstOffset, final CharWriter<? super D> writer, final int length) {
        return rejectText.init().rejectText(dst, dstOffset, writer, length);
    }

    private class DefaultLegGroup implements Leg, Iterator<Leg> {
        trading.ExecRptDecoder.LegsDecoder legsDecoder;
        int index = -1;
        DefaultLegGroup init() {
            if (legsDecoder != null) {
                throw new IllegalStateException("out of order decoding of legGroup");
            }
            legsDecoder = decoder.legs();
            index = -1;
            return this;
        }

        void skip() {
            if (legsDecoder == null) {
                init();
            }
            while (index + 1 < count()) {
                next();
            }
            index = count();//make it same as count to prevent further leg access
        }

        void unwrap() {
            legsDecoder = null;
            index = -1;
        }

        @Override
        public int count() {
            return legsDecoder.count();
        }

        @Override
        public Iterator<Leg> iterator() {
            if (index != -1) {
                throw new IllegalStateException("out of order decoding of legGroup");
            }
            return this;
        }

        @Override
        public boolean hasNext() {
            return legsDecoder.hasNext();
        }

        trading.ExecRptDecoder.LegsDecoder validateIndex() {
            if (index < 0 || index >= count()) {
                throw new IllegalStateException("out of order decoding of legGroup");
            }
            return legsDecoder;
        }

        @Override
        public Leg next() {
            legsDecoder.next();
            index++;
            return this;
        }

        @Override
        public String settlDate() {
            return validateIndex().settlDate();
        }

        @Override
        public long quantity() {
            return validateIndex().quantity();
        }

        @Override
        public double price() {
            return validateIndex().price();
        }
    }

    private class DefaultRejectText {
        boolean read;
        DefaultRejectText init() {
            validateNotRead();
            legGroup.skip();
            return this;
        }
        void skip() {
            if (!read) {
                legGroup.skip();
                decoder.wrapRejectText(nullBuffer);
                read = true;
            }
        }
        void unwrap() {
            this.read = false;
        }
        void validateNotRead() {
            if (read) {
                throw new IllegalStateException("out of order decoding of rejectTextLength");
            }
        }
        int rejectTextLength() {
            validateNotRead();
            return decoder.rejectTextLength();
        }
        String rejectText() {
            validateNotRead();
            final String result = decoder.rejectText();
            read = true;
            return result;
        }

        <D> int rejectText(final D dst, final int dstOffset, final ByteWriter<? super D> writer, final int length) {
            validateNotRead();
            final int headerLength = 4;
            final int limit = decoder.limit();
            final DirectBuffer buffer = decoder.buffer();
            final int dataLength = (int)(buffer.getInt(limit, java.nio.ByteOrder.LITTLE_ENDIAN) & 0xFFFF_FFFFL);
            final int bytesCopied = Math.min(length, dataLength);
            decoder.limit(limit + headerLength + dataLength);
            read = true;
            for (int i = 0; i < bytesCopied; i++) {
                writer.write(dst, dstOffset + i, dstOffset + bytesCopied, buffer.getByte(i));
            }
            return bytesCopied;
        }

        <D> int rejectText(final D dst, final int dstOffset, final CharWriter<? super D> writer, final int length) {
            validateNotRead();
            if (!"ASCII".equals(trading.ExecRptDecoder.rejectTextCharacterEncoding())) {
                final String s = decoder.rejectText();
                read = true;
                final int len = Math.min(s.length(), length);
                for (int i = 0; i < len; i++) {
                    writer.write(dst, dstOffset + i, dstOffset + len, s.charAt(i));
                }
                return len;
            }
            final int headerLength = 4;
            final int limit = decoder.limit();
            final DirectBuffer buffer = decoder.buffer();
            final int dataLength = (int)(buffer.getInt(limit, java.nio.ByteOrder.LITTLE_ENDIAN) & 0xFFFF_FFFFL);
            final int bytesCopied = Math.min(length, dataLength);
            decoder.limit(limit + headerLength + dataLength);
            read = true;
            for (int i = 0; i < bytesCopied; i++) {
                writer.write(dst, dstOffset + i, dstOffset + bytesCopied, (char)buffer.getByte(i));
            }
            return bytesCopied;
        }
    }

}
