package com.solab.iso8583;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;

import org.junit.Assert;
import org.junit.Test;

import com.solab.iso8583.util.HexCodec;

public class TestBinLengthFields {

    @Test
    public void testLLBINLENGTHALPHANUM() throws IOException, ParseException {
        final String fieldValue = "33041910961431721875800124A00812345678";

        MessageFactory messageFactory = new MessageFactory();
        messageFactory.setConfigPath("config.xml");
        messageFactory.setUseBinaryMessages(true);

        IsoMessage iso1 = messageFactory.newMessage(0x285);
        iso1.setField(3, new IsoValue<>(IsoType.LLBINLENGTHALPHANUM, fieldValue));
        byte[] binaryMessage = iso1.writeData();

        Assert.assertEquals("02852000000000000000263333303431393130393631343331373231383735383030313234413030383132333435363738", HexCodec.hexEncode(binaryMessage,0, binaryMessage.length));

        IsoMessage iso2 = messageFactory.parseMessage(binaryMessage, 0);
        String decodedValue = iso2.getField(3).toString();
        Assert.assertEquals(fieldValue, decodedValue);
    }

    @Test
    public void testLLBINLENGTHNUM() throws IOException, ParseException {
        final String fieldValue = "4977834199990006";
        MessageFactory messageFactory = new MessageFactory();
        messageFactory.setConfigPath("config.xml");
        messageFactory.setUseBinaryMessages(true);

        IsoMessage iso1 = messageFactory.newMessage(0x284);
        iso1.setField(3, new IsoValue<>(IsoType.LLBINLENGTHNUM, fieldValue));
        byte[] binaryMessage = iso1.writeData();

        Assert.assertEquals("02842000000000000000104977834199990006", HexCodec.hexEncode(binaryMessage,0, binaryMessage.length));

        IsoMessage iso2 = messageFactory.parseMessage(binaryMessage, 0);
        String decodeValue = iso2.getField(3).toString();
        Assert.assertEquals(fieldValue, decodeValue);
    }

    @Test
    public void testLLBINLENGTHBIN() throws IOException, ParseException {
        final String fieldValue = "000201000003010000100F3139322E3136392E3233342E31322000110101002201320023253139363338306232632D623238372D343731612D386163362D34626461343638343563623500232532314143533036303358585858585858585858585858585858585858585858585858585820002801010036284361727265666F75722020202020202020202020202020202020202020202020202020202020202000370720210126074156003806000000000112";
        MessageFactory messageFactory = new MessageFactory();
        messageFactory.setConfigPath("config.xml");
        messageFactory.setUseBinaryMessages(true);

        IsoMessage iso1 = messageFactory.newMessage(0x286);
        iso1.setField(3, new IsoValue<>(IsoType.LLBINLENGTHBIN, fieldValue));
        byte[] binaryMessage = iso1.writeData();

        Assert.assertEquals("02862000000000000000B4" + fieldValue, HexCodec.hexEncode(binaryMessage,0, binaryMessage.length));

        IsoMessage iso2 = messageFactory.parseMessage(binaryMessage, 0);
        String decodeValue = iso2.getField(3).toString();
        Assert.assertEquals(fieldValue, decodeValue);
    }
}
