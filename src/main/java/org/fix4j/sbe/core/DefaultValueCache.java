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
package org.fix4j.sbe.core;

import org.agrona.DirectBuffer;
import org.agrona.ExpandableArrayBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class DefaultValueCache<T> implements ValueCache<T> {

    private final List<DirectBuffer> rawData;
    private final List<T> valueData;
    private final int maxCapacity;
    private final Comparator<? super DirectBuffer> comparator;
    private final ValueDecoder<? extends T> valueFactory;
    private final DirectBuffer view = new UnsafeBuffer(0, 0);

    public DefaultValueCache(final int initialCapacity,
                             final ValueDecoder<? extends T> valueFactory) {
        this(initialCapacity, Integer.MAX_VALUE, valueFactory);
    }

    public DefaultValueCache(final int initialCapacity,
                             final int maxCapacity,
                             final ValueDecoder<? extends T> valueFactory) {
        this(initialCapacity, maxCapacity, DirectBufferComparator.BYTE.comparator(), valueFactory);
    }

    public DefaultValueCache(final int initialCapacity,
                             final int maxCapacity,
                             final Comparator<? super DirectBuffer> comparator,
                             final ValueDecoder<? extends T> valueFactory) {
        if (maxCapacity < 1) {
            throw new IllegalArgumentException("max capacity cannot be zero or negative: " + maxCapacity);
        }
        if (maxCapacity < initialCapacity) {
            throw new IllegalArgumentException("max capacity cannot be less than initial capacity: max=" +
                    maxCapacity + ", initial=" + initialCapacity);
        }
        this.rawData = new ArrayList<>(initialCapacity);
        this.valueData = new ArrayList<>(initialCapacity);
        this.maxCapacity = maxCapacity;
        this.comparator = requireNonNull(comparator);
        this.valueFactory = requireNonNull(valueFactory);
    }

    @Override
    public T get(final DirectBuffer buffer, final int offset, final int length) {
        view.wrap(buffer, offset, length);
        final int index = Collections.binarySearch(rawData, view, comparator);
        if (index >= 0) {
            view.wrap(0, 0);
            return valueData.get(index);
        }
        final int insertIndex = -(index + 1);
        return cache(insertIndex, view);
    }

    @Override
    public int size() {
        return rawData.size();
    }

    @Override
    public void clear() {
        rawData.clear();
        valueData.clear();
    }

    private T cache(final int insertIndex, final DirectBuffer view) {
        final DirectBuffer raw = copyOf(view);
        final T value = valueFactory.get(raw, 0, raw.capacity());
        if (rawData.size() < maxCapacity) {
            rawData.add(insertIndex, raw);
            valueData.add(insertIndex, value);
        } else {
            final int setIndex = Math.min(insertIndex, rawData.size() - 1);
            rawData.set(setIndex, raw);
            valueData.set(setIndex, value);
        }
        view.wrap(0, 0);
        return value;
    }

    private static DirectBuffer copyOf(final DirectBuffer src) {
        final MutableDirectBuffer copy = new ExpandableArrayBuffer(src.capacity());
        copy.putBytes(0, src, 0, src.capacity());
        return copy;
    }
}
