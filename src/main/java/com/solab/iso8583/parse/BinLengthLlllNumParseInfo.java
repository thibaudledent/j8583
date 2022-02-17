package com.solab.iso8583.parse;

import com.solab.iso8583.IsoType;

/**
 * Custom parser for fields of type {@link IsoType#LLLLBINLENGTHNUM}.
 */
public class BinLengthLlllNumParseInfo extends LlllbinParseInfo {

    public BinLengthLlllNumParseInfo() {
        super(IsoType.LLLLBINLENGTHNUM, 0);
    }

}
