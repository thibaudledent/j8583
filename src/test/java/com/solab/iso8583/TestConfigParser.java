package com.solab.iso8583;

import com.solab.iso8583.codecs.CompositeField;
import com.solab.iso8583.parse.FieldParseInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.text.ParseException;

/**
 * Test certain ConfigParser features.
 *
 * @author Enrique Zamudio
 *         Date: 25/11/13 18:41
 */
class TestConfigParser {

    private MessageFactory<IsoMessage> config(String path) throws IOException {
        MessageFactory<IsoMessage> mfact = new MessageFactory<>();
        mfact.setConfigPath(path);
        return mfact;
    }

    @Test
    void testParser() throws IOException, ParseException {
        final MessageFactory<IsoMessage> mfact = config("config.xml");
        //Headers
        Assertions.assertNotNull(mfact.getIsoHeader(0x800));
        Assertions.assertNotNull(mfact.getIsoHeader(0x810));
        Assertions.assertEquals(mfact.getIsoHeader(0x800), mfact.getIsoHeader(0x810));
        //Templates
        final IsoMessage m200 = mfact.getMessageTemplate(0x200);
        Assertions.assertNotNull(m200);
        final IsoMessage m400 = mfact.getMessageTemplate(0x400);
        Assertions.assertNotNull(m400);
        for (int i = 2; i < 89; i++) {
            IsoValue<?> v = m200.getField(i);
            if (v == null) {
                Assertions.assertFalse(m400.hasField(i));
            } else {
                Assertions.assertTrue(m400.hasField(i));
                Assertions.assertEquals(v, m400.getField(i));
            }
        }
        Assertions.assertFalse(m200.hasField(90));
        Assertions.assertTrue(m400.hasField(90));
        Assertions.assertTrue(m200.hasField(102));
        Assertions.assertFalse(m400.hasField(102));

        //Parsing guides
        final String s800 = "0800201080000000000012345611251125";
        final String s810 = "08102010000002000000123456112500";
        IsoMessage m = mfact.parseMessage(s800.getBytes(), 0);
        Assertions.assertNotNull(m);
        Assertions.assertTrue(m.hasField(3));
        Assertions.assertTrue(m.hasField(12));
        Assertions.assertTrue(m.hasField(17));
        Assertions.assertFalse(m.hasField(39));
        m = mfact.parseMessage(s810.getBytes(), 0);
        Assertions.assertNotNull(m);
        Assertions.assertTrue(m.hasField(3));
        Assertions.assertTrue(m.hasField(12));
        Assertions.assertFalse(m.hasField(17));
        Assertions.assertTrue(m.hasField(39));
    }

    @Test
    void testSimpleCompositeParsers() throws IOException, ParseException {
        MessageFactory<IsoMessage> mfact = config("composites.xml");

        IsoMessage m = mfact.parseMessage("01000040000000000000016one  03two12345.".getBytes(), 0);
        Assertions.assertNotNull(m);
        CompositeField f = m.getObjectValue(10);
        Assertions.assertNotNull(f);
        Assertions.assertEquals(4, f.getValues().size());
        Assertions.assertEquals("one  ", f.getObjectValue(0));
        Assertions.assertEquals("two", f.getObjectValue(1));
        Assertions.assertEquals("12345", f.getObjectValue(2));
        Assertions.assertEquals(".", f.getObjectValue(3));

        m = mfact.parseMessage("01000040000000000000018ALPHA05LLVAR12345X".getBytes(), 0);
        Assertions.assertNotNull(m);
        Assertions.assertTrue(m.hasField(10));
        f = m.getObjectValue(10);
        Assertions.assertNotNull(f.getField(0));
        Assertions.assertNotNull(f.getField(1));
        Assertions.assertNotNull(f.getField(2));
        Assertions.assertNotNull(f.getField(3));
        Assertions.assertNull(f.getField(4));
        Assertions.assertEquals("ALPHA", f.getObjectValue(0));
        Assertions.assertEquals("LLVAR", f.getObjectValue(1));
        Assertions.assertEquals("12345", f.getObjectValue(2));
        Assertions.assertEquals("X", f.getObjectValue(3));
    }

    @Test
    void testNestedCompositeParser() throws IOException, ParseException {
        MessageFactory<IsoMessage> mfact = config("composites.xml");
        IsoMessage m = mfact.parseMessage("01010040000000000000019ALPHA11F1F205F03F4X".getBytes(), 0);
        Assertions.assertNotNull(m);
        Assertions.assertTrue(m.hasField(10));
        CompositeField f = m.getObjectValue(10);
        Assertions.assertNotNull(f.getField(0));
        Assertions.assertNotNull(f.getField(1));
        Assertions.assertNotNull(f.getField(2));
        Assertions.assertNull(f.getField(3));
        Assertions.assertEquals("ALPHA", f.getObjectValue(0));
        Assertions.assertEquals("X", f.getObjectValue(2));
        f = f.getObjectValue(1);
        Assertions.assertEquals("F1", f.getObjectValue(0));
        Assertions.assertEquals("F2", f.getObjectValue(1));
        f = f.getObjectValue(2);
        Assertions.assertEquals("F03", f.getObjectValue(0));
        Assertions.assertEquals("F4", f.getObjectValue(1));
    }

    @Test
    void testSimpleCompositeTemplate() throws IOException {
        MessageFactory<IsoMessage> mfact = config("composites.xml");
        IsoMessage m = mfact.newMessage(0x100);
        //Simple composite
        Assertions.assertNotNull(m);
        Assertions.assertFalse(m.hasField(1));
        Assertions.assertFalse(m.hasField(2));
        Assertions.assertFalse(m.hasField(3));
        Assertions.assertFalse(m.hasField(4));
        CompositeField f = m.getObjectValue(10);
        Assertions.assertNotNull(f);
        Assertions.assertEquals(f.getObjectValue(0), "abcde");
        Assertions.assertEquals(f.getObjectValue(1), "llvar");
        Assertions.assertEquals(f.getObjectValue(2), "12345");
        Assertions.assertEquals(f.getObjectValue(3), "X");
        Assertions.assertFalse(m.hasField(4));
    }

    private void testNestedCompositeTemplate(int type, int fnum) throws IOException {
        MessageFactory<IsoMessage> mfact = config("composites.xml");
        IsoMessage m = mfact.newMessage(type);
        Assertions.assertNotNull(m);
        Assertions.assertFalse(m.hasField(1));
        Assertions.assertFalse(m.hasField(2));
        Assertions.assertFalse(m.hasField(3));
        Assertions.assertFalse(m.hasField(4));
        CompositeField f = m.getObjectValue(fnum);
        Assertions.assertEquals(f.getObjectValue(0), "fghij");
        Assertions.assertEquals(f.getObjectValue(2), "67890");
        Assertions.assertEquals(f.getObjectValue(3), "Y");
        f = f.getObjectValue(1);
        Assertions.assertEquals(f.getObjectValue(0), "KL");
        Assertions.assertEquals(f.getObjectValue(1), "mn");
        f = f.getObjectValue(2);
        Assertions.assertEquals(f.getObjectValue(0), "123");
        Assertions.assertEquals(f.getObjectValue(1), "45");
    }

    @Test
    void testNestedCompositeTemplate() throws IOException {
        testNestedCompositeTemplate(0x101, 10);
    }

    @Test
    void testNestedCompositeFromExtendedTemplate() throws IOException {
        testNestedCompositeTemplate(0x102, 10);
        testNestedCompositeTemplate(0x102, 12);
    }

    @Test //issue 34
    void testMultilevelExtendParseGuides() throws IOException, ParseException {
        final MessageFactory<IsoMessage> mfact = config("issue34.xml");
        //Parse a 200
        final String m200 = "0200422000000880800001X1231235959123456101010202020TERMINAL484";
        final String m210 = "0210422000000A80800001X123123595912345610101020202099TERMINAL484";
        final String m400 = "0400422000000880800401X1231235959123456101010202020TERMINAL484001X";
        final String m410 = "0410422000000a80800801X123123595912345610101020202099TERMINAL484001X";
        IsoMessage m = mfact.parseMessage(m200.getBytes(), 0);
        Assertions.assertNotNull(m);
        Assertions.assertEquals("X", m.getObjectValue(2));
        Assertions.assertEquals("123456", m.getObjectValue(11));
        Assertions.assertEquals("TERMINAL", m.getObjectValue(41));
        Assertions.assertEquals("484", m.getObjectValue(49));
        m = mfact.parseMessage(m210.getBytes(), 0);
        Assertions.assertNotNull(m);
        Assertions.assertEquals("X", m.getObjectValue(2));
        Assertions.assertEquals("123456", m.getObjectValue(11));
        Assertions.assertEquals("TERMINAL", m.getObjectValue(41));
        Assertions.assertEquals("484", m.getObjectValue(49));
        Assertions.assertEquals("99", m.getObjectValue(39));
        m = mfact.parseMessage(m400.getBytes(), 0);
        Assertions.assertNotNull(m);
        Assertions.assertEquals("X", m.getObjectValue(2));
        Assertions.assertEquals("123456", m.getObjectValue(11));
        Assertions.assertEquals("TERMINAL", m.getObjectValue(41));
        Assertions.assertEquals("484", m.getObjectValue(49));
        Assertions.assertEquals("X", m.getObjectValue(62));
        m = mfact.parseMessage(m410.getBytes(), 0);
        Assertions.assertNotNull(m);
        Assertions.assertEquals("X", m.getObjectValue(2));
        Assertions.assertEquals("123456", m.getObjectValue(11));
        Assertions.assertEquals("TERMINAL", m.getObjectValue(41));
        Assertions.assertEquals("484", m.getObjectValue(49));
        Assertions.assertEquals("99", m.getObjectValue(39));
        Assertions.assertEquals("X", m.getObjectValue(61));
    }
    
    @Test // issue 47
    void testExtendCompositeWithSameField() throws IOException, ParseException {
    	final MessageFactory<IsoMessage> mfact = config("issue47.xml");
    	
    	final String m200 = "02001000000000000004000000100000013ABCDEFGHIJKLM";
    	IsoMessage isoMessage = mfact.parseMessage(m200.getBytes(), 0);
    	
    	// check field num 4
    	IsoValue<Object> field4 = isoMessage.getField(4);
		Assertions.assertEquals(IsoType.AMOUNT, field4.getType());
    	Assertions.assertEquals(IsoType.AMOUNT.getLength(), field4.getLength());
		
    	// check nested field num 4 from composite field 62
		CompositeField compositeField62 = isoMessage.<CompositeField>getField(62).getValue();
		IsoValue<Object> nestedField4 = compositeField62.getField(0); // first in list
		Assertions.assertEquals(IsoType.ALPHA, nestedField4.getType());
		Assertions.assertEquals(13, nestedField4.getLength());
    }

    @Test
    void testEmptyFields() throws IOException, ParseException {
        final MessageFactory<IsoMessage> mfact = config("issue64.xml");
        IsoMessage msg = mfact.newMessage(0x200);
        Assertions.assertEquals("", msg.getObjectValue(3));
    }

    @Test
    void testAllTypesHaveParseInfo() {
        for (IsoType t : IsoType.values()) {
            FieldParseInfo fpi = FieldParseInfo.getInstance(t, t.getLength(), "UTF-8");
            Assertions.assertNotNull(fpi);
        }
    }
}
