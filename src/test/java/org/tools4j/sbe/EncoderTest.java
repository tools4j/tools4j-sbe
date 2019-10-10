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
package org.tools4j.sbe;

import org.agrona.DirectBuffer;
import org.agrona.ExpandableArrayBuffer;
import org.agrona.MutableDirectBuffer;
import org.junit.Ignore;
import org.junit.Test;
import org.tools4j.sbe.core.EncoderSupplier;

import java.util.Objects;

public class EncoderTest {

    @Ignore
    @Test
    public void encode() {
        final EncoderSupplier.Router router = new EncoderSupplier.Router() {
            private DirectBuffer buffer;
            private int offset;
            private int length;
            @Override
            public void route() {
                System.out.println(buffer);
            }

            @Override
            public void wrap(final DirectBuffer buffer, final int offset, final int length) {
                this.buffer = Objects.requireNonNull(buffer);
                this.offset = offset;
                this.length = length;
            }
        };

        final MutableDirectBuffer buffer = new ExpandableArrayBuffer();
        final EncoderSupplier<EncoderSupplier.Router> encoders = EncoderSupplier.supplier(buffer, 0, router);

        encoders.execRpt()
                .symbol("AUDUSD")
                .legStart(2)
                    .next().quantity(100000).price(1.23).settlDate("20191010")
                    .next().quantity(120000).price(1.34).settlDate("20191020")
                .legComplete()
                .rejectText("")
                .route();
    }
}
