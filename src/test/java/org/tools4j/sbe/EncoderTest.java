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
import org.agrona.sbe.CompositeEncoderFlyweight;
import org.agrona.sbe.MessageEncoderFlyweight;
import org.junit.Test;
import org.tools4j.sbe.core.*;

public class EncoderTest {

    interface Router {
        void route();
    }

    final class Printer implements Router, PayloadAccessProvider<Router> {
        private CompositeEncoderFlyweight header;
        private MessageEncoderFlyweight message;

        @Override
        public void route() {
            System.out.printf("header=%s, message=%s\n", header, message);
            header = null;
            message = null;
        }

        @Override
        public Router payload(final CompositeEncoderFlyweight header, final MessageEncoderFlyweight message) {
            this.header = header;
            this.message = message;
            return this;
        }
    }

    @Test
    public void encodeStandardPayload() {
        final MutableDirectBuffer buffer = new ExpandableArrayBuffer();
        final EncoderSupplier<StandardPayloadAccess> encoders = EncoderSupplier.supplier(buffer, 0);

        StandardPayloadAccess payload = encoders.execRpt()
                .symbol("AUDUSD")
                .legGroupStart(2)
                    .next().quantity(100000).price(1.23).settlDate("20191010")
                    .next().quantity(120000).price(1.34).settlDate("20191020")
                .legGroupComplete()
                .rejectText().empty();
        System.out.printf("header-length=%d, message-length=%d, total-length=%d, offset=%d, buffer=%s\n",
                payload.headerLength(),
                payload.messageLength(),
                payload.totalLength(),
                payload.offset(),
                payload.buffer()
        );
    }

    @Test
    public void encodeAndRoute() {
        final MutableDirectBuffer buffer = new ExpandableArrayBuffer();
        final EncoderSupplier<Router> routers = EncoderSupplier.supplier(buffer, 0, new Printer());

        routers.execRpt()
                .symbol("AUDUSD")
                .legGroupStart(2)
                    .next().quantity(100000).price(1.23).settlDate("20191010")
                    .next().quantity(120000).price(1.34).settlDate("20191020")
                .legGroupComplete()
                .rejectText().put("Hello World", 6, CharReader.STRING_READER, 5)
                .route();
    }

    @Test
    public void encodeWithGroupIterator() {
        final MutableDirectBuffer buffer = new ExpandableArrayBuffer();
        final EncoderSupplier<Router> routers = EncoderSupplier.supplier(buffer, 0, new Printer());
        final ExecRptEncoder<Router> execRpt = routers.execRpt();

        execRpt.symbol("AUDUSD");

        int i = 0;
        final ExecRptEncoder.LegGroup<Router> legGroup = execRpt.legGroupStart(2);
        for (final ExecRptEncoder.Leg<?> leg : legGroup) {
            leg.quantity(i * 100000).price(1.23 + i).settlDate("2019101" + i);
            i++;
        }
        legGroup.legGroupComplete().rejectText().put("bla").route();
    }

    @Test(expected = IllegalStateException.class)
    public void legGroupIncomplete() {
        final MutableDirectBuffer buffer = new ExpandableArrayBuffer();
        final EncoderSupplier<StandardPayloadAccess> encoders = EncoderSupplier.supplier(buffer, 0);

        encoders.execRpt()
                .symbol("AUDUSD")
                .legGroupStart(2)
                .legGroupComplete();
    }

    @Test(expected = IllegalStateException.class)
    public void legGroupStartAferComplete() {
        final MutableDirectBuffer buffer = new ExpandableArrayBuffer();
        final EncoderSupplier<StandardPayloadAccess> encoders = EncoderSupplier.supplier(buffer, 0);

        final ExecRptEncoder<?> rpt = encoders.execRpt();
        rpt
                .symbol("AUDUSD")
                .legGroupEmpty()
                .rejectText().put("end of story");
        rpt
                .symbol("AUDUSD")
                .legGroupStart(2);
    }

    @Test(expected = IllegalStateException.class)
    public void rejecctTextBeforeLegGroupComplete() {
        final MutableDirectBuffer buffer = new ExpandableArrayBuffer();
        final EncoderSupplier<StandardPayloadAccess> encoders = EncoderSupplier.supplier(buffer, 0);

        final ExecRptEncoder.RejectText<?> rejectText = encoders.execRpt()
                .symbol("AUDUSD")
                .legGroupEmpty();
        encoders.execRpt()
                .symbol("AUDUSD")
                .legGroupStart(2)
                    .next().quantity(100000).price(1.23).settlDate("20191010");
        rejectText.rejectText().put("group not finished");
    }
}
