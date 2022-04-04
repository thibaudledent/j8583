package com.solab.iso8583.parse;

import com.solab.iso8583.IsoType;

/**
 * Custom parser for fields of type {@link IsoType#LLBCDLENGTHALPHANUM}.
 */
public class BcdLengthLlAlphaNumParseInfo extends LlvarParseInfo {

    /**
     * Instantiates a new Bcd length ll alpha num parse info.
     */
    public BcdLengthLlAlphaNumParseInfo() {
        super(IsoType.LLBCDLENGTHALPHANUM, 0);
    }
}
