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

public interface ExecRptDecoder extends MessageDecoder<ExecRptDecoder> {
    static ExecRptDecoder create() {
        return new DefaultExecRptDecoder();
    }

    String symbol();

    LegGroup legGroup();

    int rejectTextLength();
    String rejectText();
    <D> int rejectText(D dst, int dstOffset, CharWriter<? super D> writer, int length);

    interface LegGroup extends Iterable<Leg> {
        int count();
        Leg next();
    }
    interface Leg extends LegGroup {
        String settlDate();
        long quantity();
        double price();
    }
}