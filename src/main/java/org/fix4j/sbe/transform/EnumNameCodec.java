package org.fix4j.sbe.transform;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.fix4j.sbe.core.ValueDecoder;
import org.fix4j.sbe.core.ValueEncoder;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static org.fix4j.sbe.cache.DirectBufferComparator.ASCII;

public class EnumNameCodec<E extends Enum<E>> implements ValueDecoder<E>, ValueEncoder<E> {
    private final E[] enumValues;
    private final Class<E> enumType;
    private final DirectBuffer[] enumNames;
    private final Supplier<? extends E> valueForEmptyString;
    private final Function<? super DirectBuffer, ? extends E> valueForUnknownString;
    private final DirectBuffer view = new UnsafeBuffer(0, 0);

    public EnumNameCodec(final Class<E> enumType) {
        this(enumType, null, (E)null);
    }

    public EnumNameCodec(final Class<E> enumType,
                         final E valueForEmptyString,
                         final E valueForUnknownString) {
        this(enumType, () -> valueForEmptyString, buffer -> valueForUnknownString);
    }

    public EnumNameCodec(final Class<E> enumType,
                         final Supplier<? extends E> valueForEmptyString,
                         final Function<? super DirectBuffer, ? extends E> valueForUnknownString) {
        this.enumValues = enumType.getEnumConstants();
        if (enumValues == null) {
            throw new IllegalArgumentException(enumType.getName() + " is not an enum type");
        }
        this.enumType = enumType;
        this.valueForEmptyString = requireNonNull(valueForEmptyString);
        this.valueForUnknownString = requireNonNull(valueForUnknownString);
        this.enumNames = sortByName(enumValues);
    }

    public Class<E> enumType() {
        return enumType;
    }

    public int enumValueCount() {
        return enumValues.length;
    }

    public int enumNameMaxLength() {
        int max = 0;
        for (final E value : enumValues) {
            max = Math.max(max, value.name().length());
        }
        return max;
    }

    public E enumValueByNameIndex(final int index) {
        return enumValues[index];
    }

    public E valueForEmptyString() {
        return valueForEmptyString.get();
    }

    public E valueForUnknownString(final DirectBuffer unknownString) {
        return valueForUnknownString.apply(unknownString);
    }

    @Override
    public E get(final DirectBuffer buffer, final int offset, final int length) {
        view.wrap(buffer, offset, length);
        try {
            if (length == 0) {
                return valueForEmptyString();
            }
            final int index = Arrays.binarySearch(enumNames, view, ASCII);
            if (index >= 0) {
                return enumValues[index];
            }
            return valueForUnknownString(view);
        } finally {
            view.wrap(0, 0);
        }
    }

    @Override
    public int put(final E value, final IntFunction<? extends MutableDirectBuffer> lengthToBuffer) {
        final String name;
        if (value != null) {
            final Class<?> type = value.getClass();
            if (type != enumType && type.getSuperclass() != enumType) {
                throw new IllegalArgumentException("Invalid enum value " + value + " of type " + type.getName() +
                        " (expected: " + enumType.getName() + ")");
            }
            name = value.name();
        } else {
            name = "";
        }
        final int chars = name.length();
        final MutableDirectBuffer buffer = lengthToBuffer.apply(chars);
        for (int i = 0; i < chars; i++) {
            putAscii(buffer, i, name.charAt(i));
        }
        return chars;
    }

    private static DirectBuffer[] sortByName(final Enum<?>[] values) {
        final int length = values.length;
        int totalLength = 0;

        //1) sort by name (yes, the primitive "slow" way)
        for (int i = 1; i < length; i++) {
            final String name = values[i].name();
            for (int j = i; j > 0 && name.compareTo(values[j - 1].name()) < 0; j--) {
                swap(values, j, j - 1);
            }
            totalLength += name.length();
        }

        //2) allocate name buffers, backed by single continuous byte buffer
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(totalLength);
        final DirectBuffer[] names = new DirectBuffer[length];
        int offset = 0;
        for (int i = 0; i < length; i++) {
            final String name = values[i].name();
            final int chars = name.length();
            final UnsafeBuffer unsafeBuffer = new UnsafeBuffer(byteBuffer, offset, chars);
            for (int j = 0; j < chars; j++) {
                putAscii(unsafeBuffer, j, name.charAt(j));
            }
            names[i] = unsafeBuffer;
            offset += chars;
        }
        return names;
    }

    private static void putAscii(final MutableDirectBuffer buffer, final int index, final char ch) {
        buffer.putByte(index, (byte)(0x7f & ch));
    }

    private static <T> void swap(final T[] array, final int index1, final int index2) {
        final T value1 = array[index1];
        array[index1] = array[index2];
        array[index2] = value1;
    }
}
