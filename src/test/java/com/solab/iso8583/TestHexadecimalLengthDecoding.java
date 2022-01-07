package com.solab.iso8583;

import com.solab.iso8583.util.HexCodec;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;

public class TestHexadecimalLengthDecoding {

    @Test
    public void shouldParseLengthWithBcdDecoding() throws IOException, ParseException {
        // Given
        MessageFactory<IsoMessage> mf = createMessageFactory();
        String input = "0100" +                                 //MTI
                "7F80000000000000" +                            // bitmap (with fields 2,3,4,5,6,7,8,9)
                "09" + "0666666666" +                           // F2(LLBCDBIN) length (09 = 9) + BCD value
                "26" + "01234567890123456789012345" +           // F3(LLBCDBIN) length (26 = 26) + BCD value
                "18" + "C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0" + // F4(LLBIN) length (18 = 18) + EBCDIC value
                "0112" + repeat("5", 112) +         // F5(LLLBCDBIN) length (0112 = 112) + BCD value
                "0112" + repeat("C1", 112) +        // F6(LLLBIN) length (0112 = 112) + EBCDIC value
                "1112" + repeat("6", 1112) +        // F7(LLLLBCDBIN) length (1112 = 1112) + BCD value
                "1112" + repeat("C2", 1112) +       // F8(LLLLBIN) length (1112 = 1112) + EBCDIC value
                "88888888";                                     // F9(BINARY)

        // When
        final IsoMessage m = mf.parseMessage(HexCodec.hexDecode(input), 0);

        // Then
        Assert.assertNotNull(m);
        Assert.assertEquals("666666666", m.getField(2).toString());
        Assert.assertEquals("01234567890123456789012345", m.getField(3).toString());
        Assert.assertEquals("C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0", m.getField(4).toString());
        Assert.assertEquals(repeat("5", 112), m.getField(5).toString());
        Assert.assertEquals(repeat("C1", 112), m.getField(6).toString());
        Assert.assertEquals(repeat("6", 1112), m.getField(7).toString());
        Assert.assertEquals(repeat("C2", 1112), m.getField(8).toString());
        Assert.assertEquals("88888888", m.getField(9).toString());
    }

    @Test
    public void shouldParseLengthWithHexadecimalDecoding() throws IOException, ParseException {
        // Given
        MessageFactory<IsoMessage> mf = createMessageFactory();
        mf.setVariableLengthFieldsInHex(true);
        String input = "0100" +                                 //MTI
                "7FFC000000000000" +                            // bitmap (with fields 2,3,4,5,6,7,8,9,10,11,12,13,14)
                "09" + "0666666666" +                           // F2(LLBCDBIN) length (09 = 9) + BCD value
                "1A" + "01234567890123456789012345" +           // F3(LLBCDBIN) length (1A = 26) + BCD value
                "12" + "C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0" + // F4(LLBIN) length (12 = 18) + EBCDIC value
                "0112" + repeat("5", 274) +         // F5(LLLBCDBIN) length (0112 = 274) + BCD value
                "0112" + repeat("C1", 274) +        // F6(LLLBIN) length (0112 = 274) + EBCDIC value
                "1112" + repeat("6", 4370) +        // F7(LLLLBCDBIN) length (1112 = 4370) + BCD value
                "1112" + repeat("C2", 4370) +       // F8(LLLLBIN) length (1112 = 4370) + EBCDIC value
                "88888888" +                                    // F9(BINARY)
                "FF" + repeat("C0", 255) +           // F10(LLBIN) length (FF = 255) + EBCDIC value
                "0FFE" + repeat("5", 4094) +         // F11(LLLBCDBIN) length (0FFE = 4094) + BCD value
                "0FFF" + repeat("C1", 4095) +        // F12(LLLBIN) length (0FFF = 4095) + EBCDIC value
                "FABC" + repeat("6", 64188) +        // F13(LLLLBCDBIN) length (FABC = 64188) + BCD value
                "FFFF" + repeat("C2", 65535);        // F14(LLLLBIN) length (FFFF = 65535) + EBCDIC value

        // When
        final IsoMessage m = mf.parseMessage(HexCodec.hexDecode(input), 0);

        // Then
        Assert.assertNotNull(m);
        Assert.assertEquals("666666666", m.getField(2).toString());
        Assert.assertEquals("01234567890123456789012345", m.getField(3).toString());
        Assert.assertEquals("C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0", m.getField(4).toString());
        Assert.assertEquals(repeat("5", 274), m.getField(5).toString());
        Assert.assertEquals(repeat("C1", 274), m.getField(6).toString());
        Assert.assertEquals(repeat("6", 4370), m.getField(7).toString());
        Assert.assertEquals(repeat("C2", 4370), m.getField(8).toString());
        Assert.assertEquals("88888888", m.getField(9).toString());
        Assert.assertEquals(repeat("C0", 255), m.getField(10).toString());
        Assert.assertEquals(repeat("5", 4094), m.getField(11).toString());
        Assert.assertEquals(repeat("C1", 4095), m.getField(12).toString());
        Assert.assertEquals(repeat("6", 64188), m.getField(13).toString());
        Assert.assertEquals(repeat("C2", 65535), m.getField(14).toString());
    }

    @Test
    public void shouldWriteLengthWithBcdDecoding() throws IOException {
        boolean hexa = false;

        Object[][] inputs = {
                {IsoType.LLBCDBIN, "26", "01234567890123456789012345"},
                {IsoType.LLBIN, "26", "C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0"},
                {IsoType.LLLBCDBIN, "0126", repeat("7", 126)},
                {IsoType.LLLBIN, "0126", repeat("C1", 126)},
                {IsoType.LLLLBCDBIN, "1126", repeat("7", 1126)},
                {IsoType.LLLLBIN, "1126", repeat("C1", 1126)},
        };

        for (Object[] input : inputs) {
            IsoType isoType = (IsoType) input[0];
            String len = (String) input[1];
            String value = (String) input[2];

            IsoValue<byte[]> isoValue = new IsoValue<>(isoType, HexCodec.hexDecode(value), Integer.parseInt(len), hexa);

            String result = getResultAsString(isoValue, hexa);
            Assert.assertEquals(len + value, result);
        }
    }

    @Test
    public void shouldWriteLengthWithHexadecimalDecoding() throws IOException {
        boolean hexa = true;

        Object[][] inputs = {
                {IsoType.LLBCDBIN,"1A", "01234567890123456789012345"},
                {IsoType.LLBIN, "1A", "C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0"},
                {IsoType.LLLBCDBIN, "012A", repeat("7", 298)},
                {IsoType.LLLBIN, "012A", repeat("C1", 298)},
                {IsoType.LLLLBCDBIN, "112A", repeat("7", 4394)},
                {IsoType.LLLLBIN, "112A", repeat("C1", 4394)},
                {IsoType.LLBIN, "FF", repeat("C0", 255)},
                {IsoType.LLLBCDBIN, "0FFE", repeat("7", 4094)},
                {IsoType.LLLBIN, "0FFF", repeat("C1", 4095)},
                {IsoType.LLLLBCDBIN, "FFFE", repeat("7", 65534)},
                {IsoType.LLLLBIN, "FABC", repeat("C1", 64188)},
        };

        for (Object[] input : inputs) {
            IsoType isoType = (IsoType) input[0];
            String hexaLen = (String) input[1];
            String value = (String) input[2];

            IsoValue<byte[]> isoValue = new IsoValue<>(isoType, HexCodec.hexDecode(value), 0, hexa);

            String result = getResultAsString(isoValue, hexa);
            Assert.assertEquals(hexaLen + value, result);
        }
    }

    @Test
    public void shouldWriteCustomBinFieldLengthWithHexadecimalDecoding() throws IOException, ParseException  {
        // Given
        String input = "0100" +                  // MTI
                "1500000000000000" +             // bitmap (with fields 4,6,8)
                "FF" + repeat("00", 255) +       // F2(LLBIN) max length (255 bytes) + value
                "0FFF" + repeat("11", 4095) +    // F2(LLLBIN) max length (4095 bytes) + value
                "FFFF" + repeat("22", 65535);    // F2(LLLLBIN) max length (65535 bytes) + value
        // And
        MessageFactory<IsoMessage> mf = createMessageFactoryWithCustomFields();
        mf.setVariableLengthFieldsInHex(true);
        IsoMessage m = mf.parseMessage(HexCodec.hexDecode(input), 0);

        // When
        final byte[] serializedMessage = m.writeData();

        // Then
        Assert.assertEquals(input, HexCodec.hexEncode(serializedMessage, 0, serializedMessage.length));
    }

    private String getResultAsString(IsoValue<?> isoValue, boolean hexa) throws IOException {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        isoValue.write(bout, true, false, hexa);
        byte[] writtenBytes = bout.toByteArray();
        return HexCodec.hexEncode(writtenBytes, 0, writtenBytes.length);
    }

    private String repeat(String value, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(value);
        }
        return sb.toString();
    }

    private MessageFactory<IsoMessage> createMessageFactory() throws IOException {
        MessageFactory<IsoMessage> mf = new MessageFactory<>();
        mf.setConfigPath("hexadecimal.xml");
        mf.setBinaryHeader(true);
        mf.setBinaryFields(true);
        return mf;
    }

    private MessageFactory<IsoMessage> createMessageFactoryWithCustomFields() throws IOException {
        MessageFactory<IsoMessage> mf = createMessageFactory();
        mf.setCustomField(4, new TestCustomField());
        mf.setCustomField(6, new TestCustomField());
        mf.setCustomField(8, new TestCustomField());
        return mf;
    }

    private static class TestCustomFieldDto {
        private String value;

        public String getValue() {
            return value;
        }

        public TestCustomFieldDto setValue(String value) {
            this.value = value;
            return this;
        }
    }

    private static class TestCustomField implements CustomBinaryField<TestCustomFieldDto> {

        @Override
        public TestCustomFieldDto decodeBinaryField(byte[] value, int offset, int length) {
            return new TestCustomFieldDto().setValue(HexCodec.hexEncode(value, offset, length));
        }

        @Override
        public byte[] encodeBinaryField(TestCustomFieldDto value) {
            return HexCodec.hexDecode(value.getValue());
        }

        @Override
        public TestCustomFieldDto decodeField(String value) {
            return decodeBinaryField(value.getBytes(), 0, value.length());
        }

        @Override
        public String encodeField(TestCustomFieldDto value) {
            byte[] byteValue = encodeBinaryField(value);
            return HexCodec.hexEncode(byteValue, 0, byteValue.length);
        }
    }

}
