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

import org.agrona.ExpandableArrayBuffer;
import org.agrona.MutableDirectBuffer;
import org.junit.Before;
import org.junit.Test;
import org.tools4j.sbe.core.DecoderSupplier;
import org.tools4j.sbe.core.EncoderSupplier;
import org.tools4j.sbe.core.ExecRptDecoder;
import org.tools4j.sbe.core.StandardPayloadAccess;

import static org.junit.Assert.fail;
import static org.tools4j.sbe.core.CharWriter.STRING_BUILDER_WRITER;

public class DecoderTest {

    private StandardPayloadAccess payload;

    @Before
    public void encode() {
        final MutableDirectBuffer buffer = new ExpandableArrayBuffer();
        final EncoderSupplier<StandardPayloadAccess> encoders = EncoderSupplier.supplier(buffer, 0);

        payload = encoders.execRpt()
                .symbol("AUDUSD")
                .legGroupStart(2)
                    .next().quantity(100000).price(1.23).settlDate("20191010")
                    .next().quantity(120000).price(1.34).settlDate("20191020")
                .legGroupComplete()
                .rejectText().put("bla");
    }

    @Test
    public void decode() {
        final DecoderSupplier decoders = DecoderSupplier.supplier();
        final ExecRptDecoder decoder = decoders.execRpt(payload);

        System.out.printf("symbol=%s\n", decoder.symbol());
        for (final ExecRptDecoder.Leg leg : decoder.legs()) {
            System.out.printf("  leg{quantity=%d, price=%f, settlDate=%s}\n",
                    leg.quantity(), leg.price(), leg.settlDate());
        }
        System.out.printf("rejectTextLength=%d, rejectText=%s\n",
                decoder.rejectText().length(), decoder.rejectText());

        decoder.reset();

        System.out.printf("symbol=%s, legs=%d, rejectText=%s\n",
                decoder.symbol(), decoder.legs().count(), decoder.rejectText());

        decoder.reset();

        final StringBuilder stringBuilder = new StringBuilder("rejectText=");
        decoder.rejectText().get(stringBuilder, stringBuilder.length(), STRING_BUILDER_WRITER, Integer.MAX_VALUE);
        System.out.println(stringBuilder);
    }

    @Test(expected = IllegalStateException.class)
    public void decodeVarTwiceWithoutReset() {
        final DecoderSupplier decoders = DecoderSupplier.supplier();
        final ExecRptDecoder decoder = decoders.execRpt(payload);
        System.out.printf("first rejectText: %s\n", decoder.rejectText().get());
        System.out.printf("second rejectText: %s\n", decoder.rejectText().get());
        fail("should cause an exception");
    }
}
