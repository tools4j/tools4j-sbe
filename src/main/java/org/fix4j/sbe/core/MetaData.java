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

public interface MetaData {
    String name();

    int id();

    int sinceVersion();

    Presence presence();

    String epoch();

    String timeUnit();

    String semanticType();

    String description();

    enum Presence {
        REQUIRED,
        OPTIONAL,
        CONSTANT
    }

    interface FixedLength extends MetaData {
        int length();
        int encodingOffset();
        int encodingLength();
    }

    interface CharEncoded extends MetaData {
        String characterEncoding();
    }

    interface Char extends CharEncoded, FixedLength {
        byte nullValue();
        byte minValue();
        byte maxValue();
    }

    interface Uint8 extends FixedLength {
        int MASK = 0xff;
        default int mask() {return MASK;}
        byte nullValue();
        byte minValue();
        byte maxValue();
    }

    interface Uint16 extends FixedLength {
        int MASK = 0xffff;
        default int mask() {return MASK;}
        short nullValue();
        short minValue();
        short maxValue();
    }

    interface Uint32 extends FixedLength {
        long MASK = 0xffffffffL;
        default long mask() {return MASK;}
        long nullValue();
        long minValue();
        long maxValue();
    }

    interface Uint64 extends FixedLength {
        long MASK = 0xffffffffffffffffL;
        default long mask() {return MASK;}
        long nullValue();
        long minValue();
        long maxValue();
    }

    interface Int8 extends FixedLength {
        byte nullValue();
        byte minValue();
        byte maxValue();
    }

    interface Int16 extends FixedLength {
        short nullValue();
        short minValue();
        short maxValue();
    }

    interface Int32 extends FixedLength {
        int nullValue();
        int minValue();
        int maxValue();
    }

    interface Int64 extends FixedLength {
        long nullValue();
        long minValue();
        long maxValue();
    }

    interface Float extends FixedLength {
        float nullValue();
        float minValue();
        float maxValue();
    }

    interface Double extends FixedLength {
        double nullValue();
        double minValue();
        double maxValue();
    }

    interface VarData extends MetaData {
        int headerLength();
    }

    interface VarChar extends CharEncoded, VarData {}
}
