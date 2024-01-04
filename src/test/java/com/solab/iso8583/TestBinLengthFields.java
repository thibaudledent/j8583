package com.solab.iso8583;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.solab.iso8583.util.HexCodec;

class TestBinLengthFields {

    @Test
    void testLLBINLENGTHALPHANUM() throws IOException, ParseException {
        final String fieldValue = "33041910961431721875800124A00812345678";

        MessageFactory messageFactory = new MessageFactory();
        messageFactory.setConfigPath("config.xml");
        messageFactory.setUseBinaryMessages(true);

        IsoMessage iso1 = messageFactory.newMessage(0x285);
        iso1.setField(3, new IsoValue<>(IsoType.LLBINLENGTHALPHANUM, fieldValue));
        byte[] binaryMessage = iso1.writeData();

        Assertions.assertEquals("02852000000000000000263333303431393130393631343331373231383735383030313234413030383132333435363738", HexCodec.hexEncode(binaryMessage, 0, binaryMessage.length));

        IsoMessage iso2 = messageFactory.parseMessage(binaryMessage, 0);
        String decodedValue = iso2.getField(3).toString();
        Assertions.assertEquals(fieldValue, decodedValue);
    }

    @Test
    void testLLLLBINLENGTHALPHANUM() throws IOException, ParseException {
        final String fieldValue = "0C006000004";

        MessageFactory messageFactory = new MessageFactory();
        messageFactory.setConfigPath("config.xml");
        messageFactory.setUseBinaryMessages(true);

        IsoMessage iso1 = messageFactory.newMessage(0x290);
        iso1.setField(3, new IsoValue<>(IsoType.LLLLBINLENGTHALPHANUM, fieldValue));
        byte[] binaryMessage = iso1.writeData();

        Assertions.assertEquals("02902000000000000000000B3043303036303030303034", HexCodec.hexEncode(binaryMessage, 0, binaryMessage.length));

        IsoMessage iso2 = messageFactory.parseMessage(binaryMessage, 0);
        String decodedValue = iso2.getField(3).toString();
        Assertions.assertEquals(fieldValue, decodedValue);
    }

    @Test
    void testLLBINLENGTHNUM() throws IOException, ParseException {
        final String fieldValue = "4977834199990006";
        MessageFactory messageFactory = new MessageFactory();
        messageFactory.setConfigPath("config.xml");
        messageFactory.setUseBinaryMessages(true);

        IsoMessage iso1 = messageFactory.newMessage(0x284);
        iso1.setField(3, new IsoValue<>(IsoType.LLBINLENGTHNUM, fieldValue));
        byte[] binaryMessage = iso1.writeData();

        Assertions.assertEquals("02842000000000000000104977834199990006", HexCodec.hexEncode(binaryMessage, 0, binaryMessage.length));
        IsoMessage iso2 = messageFactory.parseMessage(binaryMessage, 0);
        String decodeValue = iso2.getField(3).toString();
        Assertions.assertEquals(fieldValue, decodeValue);
    }

    @Test
    void testLLLLBINLENGTHNUM() throws IOException, ParseException {
        final String fieldValue = "4977834199990006";
        MessageFactory messageFactory = new MessageFactory();
        messageFactory.setConfigPath("config.xml");
        messageFactory.setUseBinaryMessages(true);

        IsoMessage iso1 = messageFactory.newMessage(0x287);
        iso1.setField(3, new IsoValue<>(IsoType.LLLLBINLENGTHNUM, fieldValue));
        byte[] binaryMessage = iso1.writeData();

        Assertions.assertEquals("0287200000000000000000104977834199990006", HexCodec.hexEncode(binaryMessage, 0, binaryMessage.length));

        IsoMessage iso2 = messageFactory.parseMessage(binaryMessage, 0);
        String decodeValue = iso2.getField(3).toString();
        Assertions.assertEquals(fieldValue, decodeValue);
    }

    @Test
    void testLLLLBINLENGTHNUMLargeValue() throws IOException, ParseException {
        final String fieldValue = "4977834199990006".repeat(200);
        MessageFactory messageFactory = new MessageFactory();
        messageFactory.setConfigPath("config.xml");
        messageFactory.setUseBinaryMessages(true);

        IsoMessage iso1 = messageFactory.newMessage(0x287);
        iso1.setField(3, new IsoValue<>(IsoType.LLLLBINLENGTHNUM, fieldValue));
        byte[] binaryMessage = iso1.writeData();

        Assertions.assertEquals("028720000000000000000C8049778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006497783419999000649778341999900064977834199990006", HexCodec.hexEncode(binaryMessage, 0, binaryMessage.length));

        IsoMessage iso2 = messageFactory.parseMessage(binaryMessage, 0);
        String decodeValue = iso2.getField(3).toString();
        Assertions.assertEquals(fieldValue, decodeValue);
    }

    @Test
    void testLLBINLENGTHBIN() throws IOException, ParseException {
        final String fieldValue = "000201000003010000100F3139322E3136392E3233342E31322000110101002201320023253139363338306232632D623238372D343731612D386163362D34626461343638343563623500232532314143533036303358585858585858585858585858585858585858585858585858585820002801010036284361727265666F75722020202020202020202020202020202020202020202020202020202020202000370720210126074156003806000000000112";
        MessageFactory messageFactory = new MessageFactory();
        messageFactory.setConfigPath("config.xml");
        messageFactory.setUseBinaryMessages(true);

        IsoMessage iso1 = messageFactory.newMessage(0x286);
        iso1.setField(3, new IsoValue<>(IsoType.LLBINLENGTHBIN, fieldValue));
        byte[] binaryMessage = iso1.writeData();

        Assertions.assertEquals("02862000000000000000B4" + fieldValue, HexCodec.hexEncode(binaryMessage, 0, binaryMessage.length));

        IsoMessage iso2 = messageFactory.parseMessage(binaryMessage, 0);
        String decodeValue = iso2.getField(3).toString();
        Assertions.assertEquals(fieldValue, decodeValue);
    }

    @Test
    void testLLLLBINLENGTHBIN() throws IOException, ParseException {
        final String fieldValue = "0123";
        MessageFactory messageFactory = new MessageFactory();
        messageFactory.setConfigPath("config.xml");
        messageFactory.setUseBinaryMessages(true);

        IsoMessage iso1 = messageFactory.newMessage(0x288);
        iso1.setField(3, new IsoValue<>(IsoType.LLLLBINLENGTHBIN, fieldValue));
        byte[] binaryMessage = iso1.writeData();

        Assertions.assertEquals("028820000000000000000002" + fieldValue, HexCodec.hexEncode(binaryMessage, 0, binaryMessage.length));

        IsoMessage iso2 = messageFactory.parseMessage(binaryMessage, 0);
        String decodeValue = iso2.getField(3).toString();
        Assertions.assertEquals(fieldValue, decodeValue);
    }

    @Test
    void testLLLLBINLENGTHBIN_MAXVALUE() throws IOException, ParseException {
        final String fieldValue = "49".repeat(65535);
        MessageFactory messageFactory = new MessageFactory();
        messageFactory.setConfigPath("config.xml");
        messageFactory.setUseBinaryMessages(true);

        IsoMessage iso1 = messageFactory.newMessage(0x288);
        iso1.setField(3, new IsoValue<>(IsoType.LLLLBINLENGTHBIN, fieldValue));
        byte[] binaryMessage = iso1.writeData();

        Assertions.assertEquals("02882000000000000000FFFF" + fieldValue, HexCodec.hexEncode(binaryMessage, 0, binaryMessage.length));

        IsoMessage iso2 = messageFactory.parseMessage(binaryMessage, 0);
        String decodeValue = iso2.getField(3).toString();
        Assertions.assertEquals(fieldValue, decodeValue);
    }
}
