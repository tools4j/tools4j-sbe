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
import trading.ExecRptDecoder;
import trading.MessageHeaderEncoder;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;

import static java.util.Objects.requireNonNull;

public class DefaultExecRptEncoder<P> implements ExecRptEncoder<P> {

    private final PayloadAccessProvider<? extends P> payloadAccessProvider;
    private final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
    private final trading.ExecRptEncoder encoder = new trading.ExecRptEncoder();
    private final DefaultLegGroup legGroup = new DefaultLegGroup();
    private final DefaultRejectText rejectText = new DefaultRejectText();

    public DefaultExecRptEncoder(final PayloadAccessProvider<? extends P> payloadAccessProvider) {
        this.payloadAccessProvider = requireNonNull(payloadAccessProvider);
    }

    @Override
    public ExecRptEncoder<P> wrap(final MutableDirectBuffer buffer, final int offset) {
        this.headerEncoder.wrap(null, 0);
        this.encoder.wrap(buffer, offset);
        return cleanp();
    }

    @Override
    public ExecRptEncoder<P> wrapAndApplyHeader(final MutableDirectBuffer buffer, final int offset) {
        this.encoder.wrapAndApplyHeader(buffer, offset, headerEncoder);
        return cleanp();
    }

    private ExecRptEncoder<P> cleanp() {
        legGroup.unwrap();
        rejectText.unwrap();
        return this;
    }

    @Override
    public ExecRptEncoder<P> unwrap() {
        headerEncoder.wrap(null, 0);
        encoder.wrap(null, 0);
        return cleanp();
    }

    @Override
    public MutableDirectBuffer buffer() {
        return encoder.buffer();
    }

    @Override
    public int offset() {
        return encoder.offset();
    }

    @Override
    public int sbeSchemaId() {
        return encoder.sbeSchemaId();
    }

    @Override
    public int sbeSchemaVersion() {
        return encoder.sbeSchemaVersion();
    }

    @Override
    public int sbeTemplateId() {
        return encoder.sbeTemplateId();
    }

    @Override
    public int sbeBlockLength() {
        return encoder.sbeBlockLength();
    }

    @Override
    public String sbeSemanticType() {
        return encoder.sbeSemanticType();
    }

    @Override
    public int encodedLength() {
        return encoder.encodedLength();
    }

    @Override
    public ExecRptEncoder<P> symbol(final String symbol) {
        encoder.symbol(symbol);
        return this;
    }

    @Override
    public Leg<P> legGroupStart(final int count) {
        return legGroup.init(count);
    }

    @Override
    public RejectText<P> legGroupEmpty() {
        return legGroupStart(0).legGroupComplete();
    }

    private class DefaultLegGroup implements Leg<P>, Iterator<Leg<P>> {
        trading.ExecRptEncoder.LegsEncoder legsEncoder;
        int count;
        int index = -1;
        DefaultLegGroup init(final int count) {
            if (legsEncoder != null) {
                throw new IllegalStateException("out of order encoding of legGroup");
            }
            this.legsEncoder = encoder.legsCount(count);
            this.count = count;
            this.index = -1;
            return this;
        }

        void unwrap() {
            legsEncoder = null;
            count = 0;
            index = -1;
        }

        @Override
        public RejectText<P> legGroupComplete() {
            if (index + 1 != count) {
                throw new IllegalStateException("missing legs when encoding legGroup");
            }
            index = count;//make it same as count to prevent further leg access
            return rejectText;
        }

        void validateComplete() {
            if (index != count) {
                throw new IllegalStateException("out of order encoding, legGroup is not complete");
            }
        }

        trading.ExecRptEncoder.LegsEncoder validateIndex() {
            if (index < 0 || index >= count) {
                throw new IllegalStateException("out of order encoding of legGroup");
            }
            return legsEncoder;
        }

        @Override
        public boolean hasNext() {
            return index + 1 < count;
        }

        @Override
        public Leg<P> next() {
            legsEncoder.next();
            index++;
            return this;
        }

        @Override
        public Iterator<Leg<P>> iterator() {
            if (index != -1) {
                throw new IllegalStateException("out of order encoding of legGroup");
            }
            return this;
        }

        @Override
        public Leg<P> settlDate(final String settlDate) {
            validateIndex().settlDate(settlDate);
            return this;
        }

        @Override
        public Leg<P> quantity(final long quantity) {
            validateIndex().quantity(quantity);
            return this;
        }

        @Override
        public Leg<P> price(final double price) {
            validateIndex().price(price);
            return this;
        }
    }

    private class DefaultRejectText implements RejectText<P> {
        boolean written;

        void unwrap() {
            written = false;
        }

        trading.ExecRptEncoder validateWrite() {
            legGroup.validateComplete();
            if (written) {
                throw new IllegalStateException("out of order encoding of legGroup");
            }
            return encoder;
        }

        @Override
        public P rejectText(final String text) {
            validateWrite().rejectText(text);
            written = true;
            return payload();
        }

        @Override
        public P rejectText(final CharSequence text) {
            validateWrite().rejectText(text.toString());
            written = true;
            return payload();
        }

        @Override
        public <S> P rejectText(final S src, final int srcIndex, final CharReader<? super S> reader, final int length) {
            validateWrite();
            if (!"ASCII".equals(ExecRptDecoder.rejectTextCharacterEncoding())) {
                final char[] chars = new char[length];
                for (int i = 0; i < length; i++) {
                    chars[i] = reader.read(src, srcIndex + i);
                }
                final byte[] bytes;
                try {
                    bytes = new String(chars).getBytes(ExecRptDecoder.rejectTextCharacterEncoding());
                } catch (final UnsupportedEncodingException e) {
                    throw new IllegalArgumentException("Unsupported encoding for rejecdtText: " + ExecRptDecoder.rejectTextCharacterEncoding());
                }
                encoder.putRejectText(bytes, 0, bytes.length);
                written = true;
                return payload();
            }
            if (length > 1073741824) {
                throw new IllegalStateException("length > maxValue for type: " + length);
            }
            final int headerLength = 4;
            final int limit = encoder.limit();
            encoder.limit(limit + headerLength + length);
            final MutableDirectBuffer buffer = encoder.buffer();
            buffer.putInt(limit, length, java.nio.ByteOrder.LITTLE_ENDIAN);
            for (int i = 0; i < length; i++) {
                char ch = reader.read(src, srcIndex + i);
                if (ch > 127) {
                    ch = '?';
                }
                encoder.buffer().putByte(limit + headerLength + i, (byte)ch);
            }
            written = true;
            return payload();
        }
    }

    private P payload() {
        return payloadAccessProvider.payload(
                headerEncoder.buffer() != null ? headerEncoder : null, encoder
        );
    }

}
