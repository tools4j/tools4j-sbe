package org.fix4j.sbe.core;

import org.agrona.DirectBuffer;

import java.io.IOException;
import java.nio.charset.Charset;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static java.util.Objects.requireNonNull;

abstract public class FlyweightStringDecoder implements StringDecoder {

    private DirectBuffer buffer;
    private int offset;

    public static class FixedLen extends FlyweightStringDecoder implements StringDecoder.FixedLen {

        private final MetaData.Char metaData;

        public FixedLen(final MetaData.Char metaData) {
            this.metaData = requireNonNull(metaData);
        }

        @Override
        public MetaData.Char metaData() {
            return metaData;
        }

        @Override
        public int dataOffset() {
            return offset() + metaData.encodingOffset();
        }

        @Override
        public int length() {
            return metaData.length();
        }
    }

    public static class VarLen extends FlyweightStringDecoder implements StringDecoder.VarLen {

        private final MetaData.VarChar metaData;

        public VarLen(final MetaData.VarChar metaData) {
            this.metaData = requireNonNull(metaData);
        }

        @Override
        public MetaData.VarChar metaData() {
            return metaData;
        }

        @Override
        public int dataOffset() {
            return offset() + metaData.headerLength();
        }

        @Override
        public int length() {
            return buffer().getInt(offset(), LITTLE_ENDIAN);
        }
    }

    public void wrap(final DirectBuffer buffer, final int offset) {
        this.buffer = buffer;
        this.offset = offset;
    }

    public DirectBuffer buffer() {
        return buffer;
    }

    public int offset() {
        return offset;
    }

    abstract public int dataOffset();

    @Override
    public String get() {
        final int off = dataOffset();
        final int len = length();
        final byte[] bytes = new byte[len];
        buffer.getBytes(off, bytes, 0, len);
        int end = 0;
        while (end < len && bytes[end] != 0) end++;
        return new String(bytes, 0, end, Charset.forName(metaData().characterEncoding()));
    }

    @Override
    public <T> T get(final ValueDecoder<T> decoder) {
        return decoder.get(buffer, dataOffset(), length());
    }

    @Override
    public <D> int get(final D dst, final int dstOffset, final ByteWriter<? super D> writer, final int length) {
        final DirectBuffer buffer = buffer();
        final int dataOff = dataOffset();
        final int copyLen = Math.min(length(), length);
        for (int i = 0; i < copyLen; i++) {
            final byte b = buffer.getByte(dataOff + i);
            writer.write(dst, dstOffset + i, dstOffset + copyLen, b);
        }
        return copyLen;
    }

    @Override
    public <D> int get(final D dst, final int dstOffset, final CharWriter<? super D> writer, final int length) {
        if ("ASCII".equals(metaData().characterEncoding())) {
            final DirectBuffer buffer = buffer();
            final int dataOff = dataOffset();
            final int copyLen = Math.min(length(), length);
            for (int i = 0; i < copyLen; i++) {
                final byte b = buffer.getByte(dataOff + i);
                writer.write(dst, dstOffset + i, dstOffset + copyLen, (char)b);
            }
            return copyLen;
        }
        final String s = get();
        final int slen = s.length();
        for (int i = 0; i < slen; i++) {
            writer.write(dst, dstOffset + i, dstOffset + slen, s.charAt(i));
        }
        return slen;

    }

    @Override
    public int appendTo(final Appendable appendable) {
        if ("ASCII".equals(metaData().characterEncoding())) {
            final DirectBuffer buffer = buffer();
            final int off = dataOffset();
            final int len = length();
            for (int i = 0; i < len; i++) {
                final byte b = buffer.getByte(off + i);
                try {
                    appendable.append((char) b);
                } catch (final IOException e) {
                    throw new RuntimeException("Appendable exception: e=" + e, e);
                }
            }
            return len;
        }
        final String s = get();
        try {
            appendable.append(s);
        } catch (final IOException e) {
            throw new RuntimeException("Appendable exception: e=" + e, e);
        }
        return s.length();
    }

    @Override
    public int appendTo(final StringBuilder stringBuilder) {
        if ("ASCII".equals(metaData().characterEncoding())) {
            final DirectBuffer buffer = buffer();
            final int off = dataOffset();
            final int len = length();
            for (int i = 0; i < len; i++) {
                final byte b = buffer.getByte(off + i);
                stringBuilder.append((char) b);
            }
            return len;
        }
        final String s = get();
        stringBuilder.append(s);
        return s.length();
    }

    @Override
    public String toString() {
        return buffer == null ? metaData().name() + "(?)" : get();
    }
}
