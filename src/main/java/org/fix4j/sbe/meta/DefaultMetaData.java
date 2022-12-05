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
