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
package org.fix4j.sbe.cache;

import org.agrona.DirectBuffer;
import org.fix4j.sbe.core.ValueDecoder;

import java.nio.charset.Charset;

import static java.util.Objects.requireNonNull;

public class StringCache implements ValueCache<String> {

    private final ValueCache<? extends String> cache;

    public StringCache(final int initialCapacity) {
        this(initialCapacity, Integer.MAX_VALUE);
    }

    public StringCache(final int initialCapacity, final int maxCapacity) {
        this(initialCapacity, maxCapacity, StringFactory.ASCII);
    }

    public StringCache(final int initialCapacity, final Charset charset) {
        this(initialCapacity, Integer.MAX_VALUE, charset);
    }

    public StringCache(final int initialCapacity, final int maxCapacity, final Charset charset) {
        this(initialCapacity, maxCapacity, StringFactory.stringDecoder(charset));
    }

    public StringCache(final int initialCapacity,
                       final int maxCapacity,
                       final ValueDecoder<? extends String> valueFactory) {
        this(new DefaultValueCache<>(initialCapacity, maxCapacity, valueFactory));
    }

    public StringCache(final ValueCache<? extends String> valueCache) {
        this.cache = requireNonNull(valueCache);
    }

    @Override
    public String get(final DirectBuffer buffer, final int offset, final int length) {
        return cache.get(buffer, offset, length);
    }

    @Override
    public int size() {
        return cache.size();
    }

    @Override
    public void clear() {
        cache.clear();
    }
}
