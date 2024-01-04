package com.solab.iso8583;

import com.solab.iso8583.util.HexCodec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;

class TestHexadecimalLengthDecoding {

    @Test
    void shouldParseLengthWithBcdDecoding() throws IOException, ParseException {
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
        Assertions.assertNotNull(m);
        Assertions.assertEquals("666666666", m.getField(2).toString());
        Assertions.assertEquals("01234567890123456789012345", m.getField(3).toString());
        Assertions.assertEquals("C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0", m.getField(4).toString());
        Assertions.assertEquals(repeat("5", 112), m.getField(5).toString());
        Assertions.assertEquals(repeat("C1", 112), m.getField(6).toString());
        Assertions.assertEquals(repeat("6", 1112), m.getField(7).toString());
        Assertions.assertEquals(repeat("C2", 1112), m.getField(8).toString());
        Assertions.assertEquals("88888888", m.getField(9).toString());
    }

    @Test
    void shouldWriteLengthWithBcdDecoding() throws IOException {
        // Given
        Object[][] inputs = {
                {IsoType.LLBCDBIN, "26", "01234567890123456789012345"},
                {IsoType.LLBIN, "26", "C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0"},
                {IsoType.LLLBCDBIN, "0126", repeat("7", 126)},
                {IsoType.LLLBIN, "0126", repeat("C1", 126)},
                {IsoType.LLLLBCDBIN, "1126", repeat("7", 1126)},
                {IsoType.LLLLBIN, "1126", repeat("C1", 1126)},
        };

        for (Object[] input : inputs) {
            // When
            IsoType isoType = (IsoType) input[0];
            String len = (String) input[1];
            String value = (String) input[2];

            IsoValue<byte[]> isoValue = new IsoValue<>(isoType, HexCodec.hexDecode(value), Integer.parseInt(len));

            String result = getResultAsString(isoValue);
            // Then
            Assertions.assertEquals(len + value, result);
        }
    }

    private String getResultAsString(IsoValue<?> isoValue) throws IOException {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        isoValue.write(bout, true, false);
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
