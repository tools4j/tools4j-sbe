package org.fix4j.sbe.core;

import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.fix4j.sbe.meta.MetaData;
import org.fix4j.sbe.bytes.ByteReader;
import org.fix4j.sbe.bytes.CharReader;

import java.nio.charset.Charset;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static java.util.Objects.requireNonNull;

abstract public class FlyweightStringEncoder<P> implements StringEncoder<P> {

    private final Supplier<? extends P> payloadSupplier;

    private MutableDirectBuffer buffer;
    private int offset;

    public FlyweightStringEncoder(final Supplier<? extends P> payloadSupplier) {
        this.payloadSupplier = requireNonNull(payloadSupplier);
    }

    protected P payload() {
        return payloadSupplier.get();
    }

    public void wrap(final MutableDirectBuffer buffer, final int offset) {
        this.buffer = buffer;
        this.offset = offset;
    }

    public MutableDirectBuffer buffer() {
        return buffer;
    }

    public int offset() {
        return offset;
    }

    @Override
    public P empty() {
        return put("");
    }

    abstract public int dataOffset();

    abstract protected int length(int length);

    @Override
    public P put(final String s) {
        if ("ASCII".equals(metaData().characterEncoding())) {
            return putAscii(s);
        }
        return putNonAscii(s);
    }

    @Override
    public P put(final CharSequence s) {
        if ("ASCII".equals(metaData().characterEncoding())) {
            return putAscii(s);
        }
        return putNonAscii(s.toString());
    }

    private P putAscii(final CharSequence s) {
        final MutableDirectBuffer buffer = buffer();
        final int length = s.length();
        final int maxlen = length(length);
        final int offset = dataOffset();
        if (length > 0) {
            for (int i = 0; i < length; i++) {
                buffer.putByte(offset + i, (byte) s.charAt(i));
            }
        }
        if (maxlen > length) {
            buffer.setMemory(offset + length, maxlen - length, (byte)0);
        }
        return payload();
    }

    private P putNonAscii(final String s) {
        final byte[] bytes = s.getBytes(Charset.forName(metaData().characterEncoding()));
        return put(bytes, 0, bytes.length);
    }

    @Override
    public <T> P put(final T value, final ValueEncoder<? super T> encoder) {
        final LengthToBuffer lengthToBuffer = lengthToBuffer();
        final int maxlen = lengthToBuffer.view.capacity();
        final int copied = encoder.put(value, lengthToBuffer);
        if (copied < maxlen) {
            lengthToBuffer.view.setMemory(copied, maxlen - copied, (byte)0);
        }
        lengthToBuffer.reset();
        return payload();
    }

    @Override
    public P put(final byte[] src, final int srcOffset, final int length) {
        final MutableDirectBuffer buffer = buffer();
        final int maxlen = length(length);
        final int offset = dataOffset();
        if (length > 0) {
            buffer.putBytes(offset, src, srcOffset, length);
        }
        if (maxlen > length) {
            buffer.setMemory(offset + length, maxlen - length, (byte)0);
        }
        return payload();
    }

    @Override
    public P put(final char[] src, final int srcOffset, final int length) {
        if ("ASCII".equals(metaData().characterEncoding())) {
            final MutableDirectBuffer buffer = buffer();
            final int maxlen = length(length);
            final int offset = dataOffset();
            if (length > 0) {
                for (int i = 0; i < length; i++) {
                    buffer.putByte(offset + i, (byte) src[srcOffset + i]);
                }
            }
            if (maxlen > length) {
                buffer.setMemory(offset + length, maxlen - length, (byte)0);
            }
            return payload();
        }
        return putNonAscii(new String(src, srcOffset, length));
    }

    @Override
    public <S> P put(final S src, final int srcOffset, final ByteReader<? super S> reader, final int length) {
        final MutableDirectBuffer buffer = buffer();
        final int maxlen = length(length);
        final int offset = dataOffset();
        if (length > 0) {
            for (int i = 0; i < length; i++) {
                buffer.putByte(offset + i, reader.read(src, srcOffset + i));
            }
        }
        if (maxlen > length) {
            buffer.setMemory(offset + length, maxlen - length, (byte)0);
        }
        return payload();
    }

    @Override
    public <S> P put(final S src, final int srcOffset, final CharReader<? super S> reader, final int length) {
        if ("ASCII".equals(metaData().characterEncoding())) {
            final MutableDirectBuffer buffer = buffer();
            final int maxlen = length(length);
            final int offset = dataOffset();
            if (length > 0) {
                for (int i = 0; i < length; i++) {
                    buffer.putByte(offset + i, (byte)reader.read(src, srcOffset + i));
                }
            }
            if (maxlen > length) {
                buffer.setMemory(offset + length, maxlen - length, (byte)0);
            }
            return payload();
        }
        final char[] chars = new char[length];
        for (int i = 0; i < length; i++) {
            chars[i] = reader.read(src, srcOffset + i);
        }
        return putNonAscii(new String(chars, 0, length));
    }

    public static class FixedLen<P> extends FlyweightStringEncoder<P> implements StringEncoder.FixedLen<P> {

        private final MetaData.Char metaData;

        public FixedLen(final MetaData.Char metaData, final Supplier<? extends P> payloadSupplier) {
            super(payloadSupplier);
            this.metaData = requireNonNull(metaData);
        }

        @Override
        public MetaData.Char metaData() {
            return metaData;
        }

        public int dataOffset() {
            return offset() + metaData.encodingOffset();
        }

        protected int length(final int length) {
            if (length < 0) {
                throw new IllegalArgumentException("Negative length: " + length);
            }
            final int maxlen = metaData.length();
            if (length > maxlen) {
                throw new IllegalArgumentException("Length too large: " + length + " > " + maxlen);
            }
            return maxlen;
        }
    }

    public static class VarLen<P> extends FlyweightStringEncoder<P> implements StringEncoder.VarLen<P> {

        private final MetaData.VarChar metaData;

        public VarLen(final MetaData.VarChar metaData, final Supplier<? extends P> payloadSupplier) {
            super(payloadSupplier);
            this.metaData = requireNonNull(metaData);
        }

        @Override
        public MetaData.VarChar metaData() {
            return metaData;
        }

        public int dataOffset() {
            return offset() + metaData.headerLength();
        }

        @Override
        protected int length(final int length) {
            if (length < 0) {
                throw new IllegalArgumentException("Negative length: " + length);
            }
            buffer().putInt(offset(), length, LITTLE_ENDIAN);
            return length;
        }
    }

    private class LengthToBuffer implements IntFunction<MutableDirectBuffer> {
        private final MutableDirectBuffer view = new UnsafeBuffer(0, 0);
        @Override
        public MutableDirectBuffer apply(final int length) {
            final int maxlen = length(length);
            view.wrap(buffer(), dataOffset(), maxlen);
            return view;
        }
        LengthToBuffer reset() {
            view.wrap(0, 0);
            return this;
        }
    }
    private LengthToBuffer lengthToBuffer = null;//lazy init
    protected LengthToBuffer lengthToBuffer() {
        if (lengthToBuffer == null) {
            lengthToBuffer = new LengthToBuffer();
        }
        return lengthToBuffer.reset();
    }
}
