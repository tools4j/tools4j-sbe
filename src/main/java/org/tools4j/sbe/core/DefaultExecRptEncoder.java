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
import trading.MessageHeaderEncoder;

import static java.util.Objects.requireNonNull;

public class DefaultExecRptEncoder<P> implements ExecRptEncoder<P> {

    private final PayloadAccessProvider<? extends P> payloadAccessProvider;
    private final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
    private final trading.ExecRptEncoder encoder = new trading.ExecRptEncoder();
    private final DefaultBlock blcok = new DefaultBlock();
    private final DefaultLegGroup legGroup = new DefaultLegGroup();
    private final DefaultRejectText rejectText = new DefaultRejectText();

    public DefaultExecRptEncoder(final PayloadAccessProvider<? extends P> payloadAccessProvider) {
        this.payloadAccessProvider = requireNonNull(payloadAccessProvider);
    }

    @Override
    public Block<P> wrap(final MutableDirectBuffer buffer, final int offset) {
        this.headerEncoder.wrap(null, 0);
        this.encoder.wrap(buffer, offset);
        return blcok;
    }

    @Override
    public Block<P> wrapAndApplyHeader(final MutableDirectBuffer buffer, final int offset) {
        this.encoder.wrapAndApplyHeader(buffer, offset, headerEncoder);
        return blcok;
    }

    @Override
    public void unwrap() {
        headerEncoder.wrap(null, 0);
        encoder.wrap(null, 0);
        legGroup.unwrap();
        rejectText.unwrap();
    }

    private class DefaultBlock implements Block<P> {
        @Override
        public Block<P> symbol(final String symbol) {
            encoder.symbol(symbol);
            return this;
        }

        @Override
        public Leg<P> legGroupStart(final int count) {
            return legGroup.init(count);
        }

        @Override
        public RejectText<P> legGroupEmpty() {
            return legGroupStart(0).legGroupComplete();
        }
    }

    private class DefaultLegGroup implements Leg<P> {
        trading.ExecRptEncoder.LegsEncoder legsEncoder;
        DefaultLegGroup init(final int count) {
            legsEncoder = encoder.legsCount(count);
            return this;
        }

        void unwrap() {
            legsEncoder = null;
        }

        @Override
        public RejectText<P> legGroupComplete() {
            legsEncoder = null;
            return rejectText;
        }

        @Override
        public Leg<P> next() {
            legsEncoder.next();
            return this;
        }

        @Override
        public Leg<P> settlDate(final String settlDate) {
            legsEncoder.settlDate(settlDate);
            return this;
        }

        @Override
        public Leg<P> quantity(final long quantity) {
            legsEncoder.quantity(quantity);
            return this;
        }

        @Override
        public Leg<P> price(final double price) {
            legsEncoder.price(price);
            return this;
        }
    }

    private class DefaultRejectText implements RejectText<P> {
        @Override
        public P rejectText(final String text) {
            encoder.rejectText(text);
            return payload();
        }

        @Override
        public P rejectText(final CharSequence text) {
            encoder.rejectText(text.toString());
            return payload();
        }
        void unwrap() {
            //nothing to do
        }
    }

    private P payload() {
        return payloadAccessProvider.payload(
                headerEncoder.buffer() != null ? headerEncoder : null, encoder
        );
    }

}
