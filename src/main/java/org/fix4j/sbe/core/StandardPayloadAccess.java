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

import org.agrona.MutableDirectBuffer;

public interface StandardPayloadAccess extends AutoCloseable {
    /**
     * The SBE Schema identifier containing the message declaration.
     *
     * @return the SBE Schema identifier containing the message declaration.
     */
    int sbeSchemaId();

    /**
     * The version number of the SBE Schema containing the message.
     *
     * @return the version number of the SBE Schema containing the message.
     */
    int sbeSchemaVersion();

    /**
     * The SBE template identifier for the message.
     *
     * @return the SBE template identifier for the message.
     */
    int sbeTemplateId();

    /**
     * The semantic type of the message which is typically the semantic equivalent in the FIX repository.
     *
     * @return the semantic type of the message which is typically the semantic equivalent in the FIX repository.
     */
    String sbeSemanticType();

    /**
     * The length of the root block in bytes.
     *
     * @return the length of the root block in bytes.
     */
    int sbeBlockLength();

    MutableDirectBuffer buffer();

    int offset();
    int headerLength();
    int messageLength();
    int totalLength();

    @Override
    void close();

    static PayloadAccessProvider<StandardPayloadAccess> provider() {
        return new DefaultStandardPayloadAccess();
    }
}
