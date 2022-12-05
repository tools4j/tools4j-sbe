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
package org.fix4j.sbe.meta;

import static java.util.Objects.requireNonNull;

public class DefaultMetaData implements MetaData {

    private final String name;
    private final int id;

    public DefaultMetaData(final String name, final int id) {
        this.name = requireNonNull(name);
        this.id = id;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public int id() {
        return id;
    }

    @Override
    public int sinceVersion() {
        return 0;//FIXME
    }

    @Override
    public Presence presence() {
        return Presence.REQUIRED;//FIXME
    }

    @Override
    public String epoch() {
        return "";//FIXME
    }

    @Override
    public String timeUnit() {
        return "";//FIXME
    }

    @Override
    public String semanticType() {
        return "";//FIXME
    }

    @Override
    public String description() {
        return "";//FIXME
    }

    public static class DefaultChar extends DefaultMetaData implements Char {
        private final int offset;
        private final int length;
        private final String charEncoding;
        public DefaultChar(final String name, final int id,
                           final int offset, final int length,
                           final String charEncoding) {
            super(name, id);
            this.offset = offset;
            this.length = length;
            this.charEncoding = requireNonNull(charEncoding);
        }

        @Override
        public int length() {
            return length;
        }

        @Override
        public int encodingOffset() {
            return offset;
        }

        @Override
        public int encodingLength() {
            return length;
        }

        @Override
        public String characterEncoding() {
            return charEncoding;
        }

        @Override
        public byte nullValue() {
            return '\0';//FIXME
        }

        @Override
        public byte minValue() {
            return 32;
        }

        @Override
        public byte maxValue() {
            return 127;
        }
    }
}
