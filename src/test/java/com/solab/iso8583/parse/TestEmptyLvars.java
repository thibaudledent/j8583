package com.solab.iso8583.parse;

import com.solab.iso8583.IsoType;
import com.solab.iso8583.MessageFactory;
import com.solab.iso8583.IsoMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.text.ParseException;

/**
 * Issue 38.
 *
 * @author Enrique Zamudio
 *         Date: 26/03/15 10:54
 */
class TestEmptyLvars {

    private static MessageFactory<IsoMessage> txtfact = new MessageFactory<>();
    private static MessageFactory<IsoMessage> binfact = new MessageFactory<>();
    @BeforeAll
    public static void setupSpec() throws IOException {
        txtfact.setConfigPath("issue38.xml");
        binfact.setUseBinaryMessages(true);
        binfact.setConfigPath("issue38.xml");
    }

    private void checkString(byte[] txt, byte[] bin, int field)
            throws IOException, ParseException {
        IsoMessage t = txtfact.parseMessage(txt, 0);
        IsoMessage b = binfact.parseMessage(bin, 0);
        Assertions.assertTrue(t.hasField(field));
        Assertions.assertTrue(b.hasField(field));
        Assertions.assertTrue(((String) t.getObjectValue(field)).isEmpty());
        Assertions.assertTrue(((String) b.getObjectValue(field)).isEmpty());
    }
    private void checkBin(byte[] txt, byte[] bin, int field)
            throws IOException, ParseException {
        IsoMessage t = txtfact.parseMessage(txt, 0);
        IsoMessage b = binfact.parseMessage(bin, 0);
        Assertions.assertTrue(t.hasField(field));
        Assertions.assertTrue(b.hasField(field));
        Assertions.assertEquals(0, ((byte[]) t.getObjectValue(field)).length);
        Assertions.assertEquals(0, ((byte[]) b.getObjectValue(field)).length);
    }
    @Test
    void testEmptyLLVAR() throws Exception {
        IsoMessage t = txtfact.newMessage(0x100);
        IsoMessage b = binfact.newMessage(0x100);
        t.setValue(2, "", IsoType.LLVAR, 0);
        b.setValue(2, "", IsoType.LLVAR, 0);
        checkString(t.writeData(), b.writeData(), 2);
    }
    @Test
    void testEmptyLLLVAR() throws Exception {
        IsoMessage t = txtfact.newMessage(0x100);
        IsoMessage b = binfact.newMessage(0x100);
        t.setValue(3, "", IsoType.LLLVAR, 0);
        b.setValue(3, "", IsoType.LLLVAR, 0);
        checkString(t.writeData(), b.writeData(), 3);
    }
    @Test
    void testEmptyLLLLVAR() throws Exception {
        IsoMessage t = txtfact.newMessage(0x100);
        IsoMessage b = binfact.newMessage(0x100);
        t.setValue(4, "", IsoType.LLLLVAR, 0);
        b.setValue(4, "", IsoType.LLLLVAR, 0);
        checkString(t.writeData(), b.writeData(), 4);
    }
    @Test
    void testEmptyLLBIN() throws Exception {
        IsoMessage t = txtfact.newMessage(0x100);
        IsoMessage b = binfact.newMessage(0x100);
        t.setValue(5, new byte[0], IsoType.LLBIN, 0);
        b.setValue(5, new byte[0], IsoType.LLBIN, 0);
        checkBin(t.writeData(), b.writeData(), 5);
    }
    @Test
    void testEmptyLLLBIN() throws Exception {
        IsoMessage t = txtfact.newMessage(0x100);
        IsoMessage b = binfact.newMessage(0x100);
        t.setValue(6, new byte[0], IsoType.LLLBIN, 0);
        b.setValue(6, new byte[0], IsoType.LLLBIN, 0);
        checkBin(t.writeData(), b.writeData(), 6);
    }
    @Test
    void testEmptyLLLLBIN() throws Exception {
        IsoMessage t = txtfact.newMessage(0x100);
        IsoMessage b = binfact.newMessage(0x100);
        t.setValue(7, new byte[0], IsoType.LLLLBIN, 0);
        b.setValue(7, new byte[0], IsoType.LLLLBIN, 0);
        checkBin(t.writeData(), b.writeData(), 7);
    }

}
