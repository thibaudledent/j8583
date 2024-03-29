package com.solab.iso8583;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.text.ParseException;
import java.util.TimeZone;

/**
 * Test timezone assignment in date/time fields.
 *
 * @author Enrique Zamudio
 * Date: 4/17/18 11:50 AM
 */
class TestTimezones {

    private final TimeZone utc = TimeZone.getTimeZone("UTC");
    private final TimeZone gmt5 = TimeZone.getTimeZone("GMT-500");
    private MessageFactory<IsoMessage> mf;

   	@BeforeEach
   	void init() throws IOException {
        mf = new MessageFactory<>();
        mf.setCharacterEncoding("UTF-8");
        mf.setConfigPath("timezones.xml");
        mf.setAssignDate(true);
   	}

    @Test
    void testTemplate() {
        IsoMessage t = mf.getMessageTemplate(0x100);
        Assertions.assertTrue(t.hasEveryField(7, 12, 13));
        Assertions.assertEquals(utc, t.getField(7).getTimeZone());
        Assertions.assertEquals(gmt5, t.getField(12).getTimeZone());
        Assertions.assertNull(t.getField(13).getTimeZone());
        t = mf.newMessage(0x100);
        Assertions.assertTrue(t.hasField(7));
        Assertions.assertEquals(utc, t.getField(7).getTimeZone());
    }

    @Test
    void testParsingGuide() throws ParseException, IOException {
        String trama = "011002180000000000001231112233112233112233";
        IsoMessage m = mf.parseMessage(trama.getBytes(), 0);
        Assertions.assertTrue(m.hasEveryField(7, 12, 13));
        Assertions.assertEquals(utc, m.getField(7).getTimeZone());
        Assertions.assertEquals(gmt5, m.getField(12).getTimeZone());
        Assertions.assertNull(m.getField(13).getTimeZone());
    }

}
