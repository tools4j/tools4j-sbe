package org.fix4j.sbe.core;

public interface MessageMeta {
    Block block();
    Iterable<Group> groups();
    Iterable<Data> datas();

    interface Element {
        Element next();
    }

    interface Field extends Element {
        MetaData.FixedLength meta();
    }

    interface Block extends Element {
        Iterable<Field> fields();
        Iterable<Group> groups();
        Iterable<Data> datas();
    }

    interface Group extends Element {
        Iterable<Field> fields();
        Iterable<Group> groups();
        Iterable<Data> datas();
    }

    interface Data extends Element {
        MetaData.VarData meta();
    }
}
