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

import org.agrona.MutableDirectBuffer;

public interface ExecRptEncoder<P> extends AutoCloseable {
    Block<P> wrap(MutableDirectBuffer buffer, int offset);
    Block<P> wrapAndApplyHeader(MutableDirectBuffer buffer, int offset);
    void unwrap();

    default void close() {
        unwrap();
    }

    static ExecRptEncoder<StandardPayloadAccess> create() {
        return create(new DefaultStandardPayloadAccess());
    }

    static <P> ExecRptEncoder<P> create(PayloadAccessProvider<? extends P> payloadAccessProvider) {
        return new DefaultExecRptEncoder<>(payloadAccessProvider);
    }

    interface Block<P> {
        Block<P> symbol(String symbol);
        LegGroup<P> legGroupStart(int count);
        RejectText<P> legGroupEmpty();
    }
    interface LegGroup<P> {
        Leg<P> next();
        RejectText<P> legGroupComplete();
    }
    interface Leg<P> extends LegGroup<P> {
        Leg<P> settlDate(String settlDate);
        Leg<P> quantity(long quantity);
        Leg<P> price(double price);
    }
    interface RejectText<P> {
        P rejectText(String text);
        P rejectText(CharSequence text);
    }
}
