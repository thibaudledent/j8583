package com.solab.iso8583;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Test ISO headers.
 *
 * @author Enrique Zamudio
 *         Date: 01/07/16 8:21 AM
 */
class TestHeaders {

    private MessageFactory<IsoMessage> mf;

   	@BeforeEach
   	void init() throws IOException {
   		mf = new MessageFactory<>();
   		mf.setCharacterEncoding("UTF-8");
   		mf.setConfigPath("config.xml");
   	}

    @Test
    void testBinaryHeader() throws Exception {
        IsoMessage m = mf.newMessage(0x280);
        Assertions.assertNotNull(m.getBinaryIsoHeader());
        byte[] buf = m.writeData();
        Assertions.assertEquals(4+4+16+2, buf.length);
        for (int i=0; i < 4; i++) {
            Assertions.assertEquals(buf[i], (byte)0xff);
        }
        Assertions.assertEquals(buf[4], 0x30);
        Assertions.assertEquals(buf[5], 0x32);
        Assertions.assertEquals(buf[6], 0x38);
        Assertions.assertEquals(buf[7], 0x30);
        //Then parse and check the header is binary 0xffffffff
        m = mf.parseMessage(buf, 4, true);
        Assertions.assertNull(m.getIsoHeader());
        buf = m.getBinaryIsoHeader();
        Assertions.assertNotNull(buf);
        for (int i=0; i < 4; i++) {
            Assertions.assertEquals(buf[i], (byte)0xff);
        }
        Assertions.assertEquals(0x280, m.getType());
        Assertions.assertTrue(m.hasField(3));
    }

}
