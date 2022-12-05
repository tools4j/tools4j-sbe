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
package org.fix4j.sbe.sample;

import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.fix4j.sbe.core.FlyweightStringEncoder.FixedLen;
import org.fix4j.sbe.core.StringEncoder;
import org.fix4j.sbe.core.ValueEncoder;
import org.fix4j.sbe.meta.DefaultMetaData;
import org.fix4j.sbe.meta.MetaData;
import org.fix4j.sbe.payload.PayloadViewProvider;
import org.fix4j.sbe.bytes.ByteReader;
import org.fix4j.sbe.bytes.CharReader;
import trading.ExecRptDecoder;
import trading.ExecRptEncoder.LegsEncoder;
import trading.MessageHeaderEncoder;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;

import static java.util.Objects.requireNonNull;

public class DefaultExecRptEncoder<P> implements ExecRptEncoder<P> {

    private final PayloadViewProvider<? extends P> payloadAccessProvider;
    private final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
    private final trading.ExecRptEncoder encoder = new trading.ExecRptEncoder();
    private final DefaultLegGroup legGroup = new DefaultLegGroup();
    private final DefaultRejectText rejectText = new DefaultRejectText();

    public DefaultExecRptEncoder(final PayloadViewProvider<? extends P> payloadAccessProvider) {
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
    public int limit() {
        return encoder.limit();
    }

    @Override
    public void limit(final int limit) {
        encoder.limit(limit);
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

    @Override
    public String toString() {
        try (org.fix4j.sbe.sample.ExecRptDecoder decoder = org.fix4j.sbe.sample.ExecRptDecoder.create()) {
            return decoder.wrap(buffer(), offset(), sbeBlockLength(), sbeSchemaVersion()).toString();
        }
    }

    private class DefaultLegGroup implements Leg<P>, Iterator<Leg<P>> {
        final FixedLen<Leg<P>> settlDate = new FixedLen<Leg<P>>(
                new DefaultMetaData.DefaultChar("settlDate", LegsEncoder.settlDateId(),
                        LegsEncoder.settlDateEncodingOffset(), LegsEncoder.settlDateLength(),
                        LegsEncoder.settlDateCharacterEncoding()),
                () -> DefaultLegGroup.this
        );
        LegsEncoder legsEncoder;
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
            settlDate.wrap(null, 0);
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
        public StringEncoder<Leg<P>> settlDate() {
            //noinspection ResultOfMethodCallIgnored
            validateIndex();
            settlDate.wrap(buffer(), encoder.limit() - LegsEncoder.sbeBlockLength());
            return settlDate;
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

    private class DefaultRejectText implements RejectText<P>, StringEncoder<P>, MetaData.VarChar {
        private final MutableDirectBuffer bufferView = new UnsafeBuffer(0, 0);
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
        public StringEncoder<P> rejectText() {
            return this;
        }

        @Override
        public P empty() {
            return put("");
        }

        @Override
        public P put(final String text) {
            validateWrite().rejectText(text);
            written = true;
            return payload();
        }

        @Override
        public P put(final CharSequence text) {
            return put(text, 0, CharReader.CHAR_SEQUENCE_READER, text.length());
        }

        @Override
        public <T> P put(final T value, final ValueEncoder<? super T> valueEncoder) {
            validateWrite();
            valueEncoder.put(value, length -> {
                validateWrite();
                if (length > 1073741824)
                {
                    throw new IllegalStateException("length > maxValue for type: " + length);
                }
                final int headerLength = 4;
                final int limit = encoder.limit();
                encoder.buffer().putInt(limit, length, java.nio.ByteOrder.LITTLE_ENDIAN);
                encoder.limit(limit + headerLength + length);
                written = true;
                bufferView.wrap(encoder.buffer(), limit + headerLength, length);
                return bufferView;
            });
            bufferView.wrap(0, 0);
            return payload();
        }

        @Override
        public P put(final byte[] src, final int srcOffset, final int length) {
            return put(src, srcOffset, ByteReader.BYTE_ARRAY_READER, length);
        }

        @Override
        public P put(final char[] src, final int srcOffset, final int length) {
            return put(src, srcOffset, CharReader.CHAR_ARRAY_READER, length);
        }

        @Override
        public <S> P put(final S src, final int srcOffset, final ByteReader<? super S> reader, final int length) {
            validateWrite();
            if (length > 1073741824) {
                throw new IllegalStateException("length > maxValue for type: " + length);
            }
            final int headerLength = 4;
            final int limit = encoder.limit();
            encoder.limit(limit + headerLength + length);
            final MutableDirectBuffer buffer = encoder.buffer();
            buffer.putInt(limit, length, java.nio.ByteOrder.LITTLE_ENDIAN);
            for (int i = 0; i < length; i++) {
                int b = reader.read(src, srcOffset + i) & 0xff;
                if (b > 127) {
                    b = '?';
                }
                encoder.buffer().putByte(limit + headerLength + i, (byte)b);
            }
            written = true;
            return payload();
        }

        @Override
        public <S> P put(final S src, final int srcOffset, final CharReader<? super S> reader, final int length) {
            validateWrite();
            if (!"ASCII".equals(ExecRptDecoder.rejectTextCharacterEncoding())) {
                final char[] chars = new char[length];
                for (int i = 0; i < length; i++) {
                    chars[i] = reader.read(src, srcOffset + i);
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
                char ch = reader.read(src, srcOffset + i);
                if (ch > 127) {
                    ch = '?';
                }
                encoder.buffer().putByte(limit + headerLength + i, (byte)ch);
            }
            written = true;
            return payload();
        }

        @Override
        public VarChar metaData() {
            return this;
        }

        @Override
        public int headerLength() {
            return trading.ExecRptDecoder.rejectTextHeaderLength();
        }

        @Override
        public String characterEncoding() {
            return trading.ExecRptDecoder.rejectTextCharacterEncoding();
        }

        @Override
        public String name() {
            return null;
        }

        @Override
        public int id() {
            return 0;
        }

        @Override
        public int sinceVersion() {
            return 0;
        }

        @Override
        public MetaData.Presence presence() {
            return null;
        }

        @Override
        public String epoch() {
            return null;
        }

        @Override
        public String timeUnit() {
            return null;
        }

        @Override
        public String semanticType() {
            return null;
        }

        @Override
        public String description() {
            return null;
        }
    }

    private P payload() {
        return payloadAccessProvider.payload(
                headerEncoder.buffer() != null ? headerEncoder : null, encoder
        );
    }

}
