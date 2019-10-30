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

import java.nio.charset.Charset;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static org.tools4j.sbe.core.StringFactories.ASCII_STRING_FACTORY;
import static org.tools4j.sbe.core.StringFactories.stringFactory;

public class StringCache implements ValueCache<String> {

    private final ValueCache<? extends String> cache;

    public StringCache(final int initialCapacity) {
        this(initialCapacity, Integer.MAX_VALUE);
    }

    public StringCache(final int initialCapacity, final int maxCapacity) {
        this(initialCapacity, maxCapacity, ASCII_STRING_FACTORY);
    }

    public StringCache(final int initialCapacity, final Charset charset) {
        this(initialCapacity, Integer.MAX_VALUE, charset);
    }

    public StringCache(final int initialCapacity, final int maxCapacity, final Charset charset) {
        this(initialCapacity, maxCapacity, stringFactory(charset));
    }

    public StringCache(final int initialCapacity,
                       final int maxCapacity,
                       final Function<? super DirectBuffer, ? extends String> valueFactory) {
        this(new DefaultValueCache<>(initialCapacity, maxCapacity, valueFactory));
    }

    public StringCache(final ValueCache<? extends String> valueCache) {
        this.cache = requireNonNull(valueCache);
    }

    @Override
    public String lookup(final DirectBuffer buffer, final int offset, final int length) {
        return cache.lookup(buffer, offset, length);
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
