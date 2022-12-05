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
package org.fix4j.sbe.sample;

import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.fix4j.sbe.core.FlyweightStringDecoder.FixedLen;
import org.fix4j.sbe.core.FlyweightStringDecoder.VarLen;
import org.fix4j.sbe.core.StringDecoder;
import org.fix4j.sbe.meta.DefaultMetaData;
import org.fix4j.sbe.meta.MetaData;
import trading.ExecRptDecoder.LegsDecoder;

import java.util.Iterator;

public class DefaultExecRptDecoder implements ExecRptDecoder {

    private final trading.ExecRptDecoder decoder = new trading.ExecRptDecoder();
    private final DefaultLegGroup legGroup = new DefaultLegGroup();
    private final DefaultRejectText rejectText = new DefaultRejectText();
    private final DirectBuffer nullBuffer = new UnsafeBuffer(0, 0);

    private void cleanup() {
        legGroup.unwrap();
        rejectText.unwrap();
    }

    @Override
    public ExecRptDecoder wrap(final DirectBuffer buffer, final int offset, final int actingBlockLength, final int actingVersion) {
        decoder.wrap(buffer, offset, actingBlockLength, actingVersion);
        cleanup();
        return this;
    }

    @Override
    public ExecRptDecoder unwrap() {
        decoder.wrap(null, 0, 0, 0);
        cleanup();
        return this;
    }

    @Override
    public ExecRptDecoder reset() {
        decoder.limit(offset() + sbeBlockLength());
        cleanup();
        return this;
    }

    @Override
    public DirectBuffer buffer() {
        return decoder.buffer();
    }

    @Override
    public int offset() {
        return decoder.offset();
    }

    @Override
    public int sbeSchemaId() {
        return decoder.sbeSchemaId();
    }

    @Override
    public int sbeSchemaVersion() {
        return decoder.sbeSchemaVersion();
    }

    @Override
    public int sbeTemplateId() {
        return decoder.sbeTemplateId();
    }

    @Override
    public int sbeBlockLength() {
        return decoder.sbeBlockLength();
    }

    @Override
    public String sbeSemanticType() {
        return decoder.sbeSemanticType();
    }

    @Override
    public int encodedLength() {
        return decoder.encodedLength();
    }

    @Override
    public String symbol() {
        return decoder.symbol();
    }

    @Override
    public LegGroup legs() {
        return legGroup.init();
    }

    @Override
    public StringDecoder rejectText() {
        return rejectText.init();
    }

    private class DefaultLegGroup implements Leg, Iterator<Leg> {
        final FixedLen settlDate = new FixedLen(
                new DefaultMetaData.DefaultChar("settlDate", LegsDecoder.settlDateId(),
                        LegsDecoder.settlDateEncodingOffset(), LegsDecoder.settlDateLength(),
                        LegsDecoder.settlDateCharacterEncoding()));
        LegsDecoder legsDecoder;
        int index = -1;
        DefaultLegGroup init() {
            if (legsDecoder != null) {
                throw new IllegalStateException("out of order decoding of legGroup");
            }
            legsDecoder = decoder.legs();
            index = -1;
            return this;
        }

        void skip() {
            if (legsDecoder == null) {
                init();
            }
            while (index + 1 < count()) {
                next();
            }
            index = count();//make it same as count to prevent further leg access
        }

        void unwrap() {
            legsDecoder = null;
            settlDate.wrap(null, 0);
            index = -1;
        }

        @Override
        public int count() {
            return legsDecoder.count();
        }

        @Override
        public Iterator<Leg> iterator() {
            if (index != -1) {
                throw new IllegalStateException("out of order decoding of legGroup");
            }
            return this;
        }

        @Override
        public boolean hasNext() {
            return legsDecoder.hasNext();
        }

        trading.ExecRptDecoder.LegsDecoder validateIndex() {
            if (index < 0 || index >= count()) {
                throw new IllegalStateException("out of order decoding of legGroup");
            }
            return legsDecoder;
        }

        @Override
        public Leg next() {
            legsDecoder.next();
            index++;
            return this;
        }

        @Override
        public StringDecoder settlDate() {
            validateIndex();
            settlDate.wrap(buffer(), decoder.limit() - legsDecoder.actingBlockLength());
            return settlDate;
        }

        @Override
        public long quantity() {
            return validateIndex().quantity();
        }

        @Override
        public double price() {
            return validateIndex().price();
        }
    }

    private class DefaultRejectText implements MetaData.VarChar {
        final VarLen str = new VarLen(this);
        StringDecoder.VarLen init() {
            if (str.buffer() == null) {
                legGroup.skip();
                str.wrap(buffer(), decoder.limit());
                decoder.limit(decoder.limit() + str.metaData().headerLength());
            }
            return str;
        }
        void unwrap() {
            str.wrap(null, 0);
        }

        @Override
        public int headerLength() {
            return trading.ExecRptDecoder.rejectTextHeaderLength();
        }

        @Override
        public String characterEncoding() {
            return trading.ExecRptDecoder.rejectTextCharacterEncoding();
        }

        @Override
        public String name() {
            return "rejectText";
        }

        @Override
        public int id() {
            return trading.ExecRptDecoder.rejectTextId();
        }

        @Override
        public int sinceVersion() {
            return 0;
        }

        @Override
        public Presence presence() {
            return null;
        }

        @Override
        public String epoch() {
            return null;
        }

        @Override
        public String timeUnit() {
            return null;
        }

        @Override
        public String semanticType() {
            return null;
        }

        @Override
        public String description() {
            return null;
        }
    }

}
