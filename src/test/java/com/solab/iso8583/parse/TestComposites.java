package com.solab.iso8583.parse;

import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;
import com.solab.iso8583.codecs.CompositeField;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for the CompositeField.
 *
 * @author Enrique Zamudio
 *         Date: 25/11/13 17:43
 */
class TestComposites {

    final String textData = "One  03Two00999X";
    final byte[] binaryData = new byte[]{'O', 'n', 'e', ' ', ' ', 3, 'T', 'w', 'o',
                    0, 9, (byte) 0x99, 'X'};

    @Test
    void testEncodeText() {
        final CompositeField f = new CompositeField();
        f.addValue(new IsoValue<>(IsoType.ALPHA, "One", 5));
        f.getValues().get(0).setCharacterEncoding("UTF-8");
        Assertions.assertEquals("One  ", f.encodeField(f));
        f.addValue("Two", null, IsoType.LLVAR, 0);
        f.getValues().get(1).setCharacterEncoding("UTF-8");
        Assertions.assertEquals("One  03Two", f.encodeField(f));
        f.addValue(999, null, IsoType.NUMERIC, 5);
        f.getValues().get(2).setCharacterEncoding("UTF-8");
        Assertions.assertEquals("One  03Two00999", f.encodeField(f));
        f.addValue("X", null, IsoType.ALPHA, 1);
        Assertions.assertEquals(textData, f.encodeField(f));
    }

    @Test
    void testEncodeBinary() {
        final CompositeField f = new CompositeField()
                .addValue(new IsoValue<>(IsoType.ALPHA, "One", 5));
        Assertions.assertArrayEquals(new byte[]{'O', 'n', 'e', 32, 32}, f.encodeBinaryField(f));
        f.addValue(new IsoValue<>(IsoType.LLVAR, "Two"));
        Assertions.assertArrayEquals(new byte[]{'O', 'n', 'e', ' ', ' ', 3, 'T', 'w', 'o'},
                f.encodeBinaryField(f));
        f.addValue(new IsoValue<>(IsoType.NUMERIC, 999l, 5));
        f.addValue(new IsoValue<>(IsoType.ALPHA, "X", 1));
        Assertions.assertArrayEquals(binaryData, f.encodeBinaryField(f));
    }

    @Test
    void testDecodeText() {
        final CompositeField dec = new CompositeField()
                .addParser(new AlphaParseInfo(5))
                .addParser(new LlvarParseInfo())
                .addParser(new NumericParseInfo(5))
                .addParser(new AlphaParseInfo(1));
        final CompositeField f = dec.decodeField(textData);
        Assertions.assertNotNull(f);
        Assertions.assertEquals(4, f.getValues().size());
        Assertions.assertEquals("One  ", f.getValues().get(0).getValue());
        Assertions.assertEquals("Two", f.getValues().get(1).getValue());
        Assertions.assertEquals("00999", f.getValues().get(2).getValue());
        Assertions.assertEquals("X", f.getValues().get(3).getValue());
    }

    @Test
    void testDecodeBinary() {
        final CompositeField dec = new CompositeField()
                .addParser(new AlphaParseInfo(5))
                .addParser(new LlvarParseInfo())
                .addParser(new NumericParseInfo(5))
                .addParser(new AlphaParseInfo(1));
        final CompositeField f = dec.decodeBinaryField(binaryData, 0, binaryData.length);
        Assertions.assertNotNull(f);
        Assertions.assertEquals(4, f.getValues().size());
        Assertions.assertEquals("One  ", f.getValues().get(0).getValue());
        Assertions.assertEquals("Two", f.getValues().get(1).getValue());
        Assertions.assertEquals(999l, f.getValues().get(2).getValue());
        Assertions.assertEquals("X", f.getValues().get(3).getValue());
    }

    @Test
    void testDecodeBinaryWithOffset() {
        final CompositeField dec = new CompositeField()
                .addParser(new LlvarParseInfo())
                .addParser(new NumericParseInfo(5))
                .addParser(new AlphaParseInfo(1));
        int offset = 5;
        final CompositeField f = dec.decodeBinaryField(binaryData, offset, this.binaryData.length - offset);
        Assertions.assertNotNull(f);
        Assertions.assertEquals(3, f.getValues().size());
        Assertions.assertEquals("Two", f.getValues().get(0).getValue());
        Assertions.assertEquals(999l, f.getValues().get(1).getValue());
        Assertions.assertEquals("X", f.getValues().get(2).getValue());
    }

}
