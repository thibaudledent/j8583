package com.solab.iso8583.parse;

import com.solab.iso8583.IsoValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;

/**
 * Test parsing of data with different encodings.
 *
 * @author Enrique Zamudio
 *         Date: 05/03/14 16:55
 */
class TestEncoding {

    @Test
    void windowsToUtf8() throws UnsupportedEncodingException, ParseException {
        final String data = "05ácido";
        final byte[] buf = data.getBytes("ISO-8859-1");
        final LlvarParseInfo parser = new LlvarParseInfo();
        parser.setCharacterEncoding("UTF-8");
        IsoValue<?> field = parser.parse(1, buf, 0, null);
        Assertions.assertNotEquals(field.getValue(), data.substring(2));
        parser.setCharacterEncoding("ISO-8859-1");
        field = parser.parse(1, buf, 0, null);
        Assertions.assertEquals(data.substring(2), field.getValue());
    }

}
