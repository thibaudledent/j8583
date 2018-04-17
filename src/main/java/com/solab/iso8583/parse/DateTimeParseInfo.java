package com.solab.iso8583.parse;

import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Abstract class for date/time parsers.
 *
 * @author Enrique Zamudio
 *         Date: 18/12/13 18:21
 */
public abstract class DateTimeParseInfo extends FieldParseInfo {

    protected static final long FUTURE_TOLERANCE;
    protected TimeZone tz;
    private static TimeZone defaultTimezone;

   	static {
   		FUTURE_TOLERANCE = Long.parseLong(System.getProperty("j8583.future.tolerance", "900000"));
   	}

   	public static void setDefaultTimeZone(TimeZone tz) {
   	    defaultTimezone = tz;
    }
    public static TimeZone getDefaultTimeZone() {
   	    return defaultTimezone;
    }

    protected DateTimeParseInfo(IsoType type, int length) {
        super(type, length);
    }

    public void setTimeZone(TimeZone value) {
        tz = value;
    }
    public TimeZone getTimeZone() {
        return tz;
    }

    public static void adjustWithFutureTolerance(Calendar cal) {
   		//We need to handle a small tolerance into the future (a couple of minutes)
   		long now = System.currentTimeMillis();
   		long then = cal.getTimeInMillis();
   		if (then > now && then-now > FUTURE_TOLERANCE) {
   			cal.add(Calendar.YEAR, -1);
   		}
   	}

   	protected IsoValue<Date> createValue(Calendar cal, boolean adjusting) {
        if (tz != null) {
            cal.setTimeZone(tz);
        } else if (getDefaultTimeZone() != null) {
            cal.setTimeZone(getDefaultTimeZone());
        }
        if (adjusting) {
            adjustWithFutureTolerance(cal);
        }
        IsoValue<Date> v = new IsoValue<>(type, cal.getTime(), null);
        if (tz != null) {
            v.setTimeZone(tz);
        } else if (getDefaultTimeZone() != null) {
            v.setTimeZone(getDefaultTimeZone());
        }
		return v;
    }
}
