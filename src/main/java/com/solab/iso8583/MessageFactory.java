/*
j8583 A Java implementation of the ISO8583 protocol
Copyright (C) 2007 Enrique Zamudio Lopez

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 3 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
*/
package com.solab.iso8583;

import com.solab.iso8583.parse.ConfigParser;
import com.solab.iso8583.parse.DateTimeParseInfo;
import com.solab.iso8583.parse.FieldParseInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static com.solab.iso8583.IsoMessage.MAX_AMOUNT_OF_FIELDS;
import static com.solab.iso8583.IsoMessage.PRIMARY_BITMAP_SIZE;
import static com.solab.iso8583.IsoMessage.START_OF_PRIMARY_BITMAP_FIELDS;
import static com.solab.iso8583.IsoMessage.START_OF_SECONDARY_BITMAP_FIELDS;
import static com.solab.iso8583.IsoMessage.START_OF_TERTIARY_BITMAP_FIELDS;
import static com.solab.iso8583.IsoType.VARIABLE_LENGTH_VAR_TYPES;

/**
 * This class is used to create messages, either from scratch or from an existing String or byte
 * buffer. It can be configured to put default values on newly created messages, and also to know
 * what to expect when reading messages from an InputStream.
 * <p>
 * The factory can be configured to know what values to set for newly created messages, both from
 * a template (useful for fields that must be set with the same value for EVERY message created)
 * and individually (for trace [field 11] and message date [field 7]).
 * <p>
 * It can also be configured to know what fields to expect in incoming messages (all possible values
 * must be stated, indicating the date type for each). This way the messages can be parsed from
 * a byte buffer.
 *
 * @param <T> the type parameter
 * @author Enrique Zamudio
 */
public class MessageFactory<T extends IsoMessage> {

    /**
     * The Log.
     */
    protected final Logger log = LoggerFactory.getLogger(getClass());
    /**
     * Stores the information needed to parse messages sorted by type.
     */
    protected Map<Integer, Map<Integer, FieldParseInfo>> parseMap = new HashMap<>();
    /**
     * Stores the field numbers to be parsed, in order of appearance.
     */
    protected Map<Integer, List<Integer>> parseOrder = new HashMap<>();
    /**
     * This map stores the message template for each message type.
     */
    private Map<Integer, T> typeTemplates = new HashMap<>();
    private TraceNumberGenerator traceGen;
    /**
     * The ISO header to be included in each message type.
     */
    private Map<Integer, String> isoHeaders = new HashMap<>();
    private Map<Integer, byte[]> binIsoHeaders = new HashMap<>();
    /**
     * A map for the custom field encoder/decoders, keyed by field number.
     */
    @SuppressWarnings("rawtypes")
    private Map<Integer, CustomField> customFields = new HashMap<>();
    /**
     * Indicates if the current date should be set on new messages (field 7).
     */
    private boolean setDate;

    /**
     * Indicates that the header should be written/parsed as binary
     */
    private boolean binaryHeader;

    /**
     * Indicates that the fields should be written/parsed as binary
     */
    private boolean binaryFields;

    private int etx = -1;
    /**
     * Flag to specify if missing fields should be ignored as long as they're at
     * the end of the message.
     */
    private boolean ignoreLast;
    private boolean useTertiaryBitmap = false;
    private boolean forceb2;
    private boolean binBitmap;
    private boolean forceStringEncoding;
    /* Flag specifying that variable length fields have the length header encoded in hexadecimal format */
    private boolean variableLengthFieldsInHex;
    private String encoding = System.getProperty("file.encoding");

    /**
     * Is force string encoding boolean.
     *
     * @return the boolean
     */
    public boolean isForceStringEncoding() {
        return forceStringEncoding;
    }

    /**
     * This flag gets passed on to newly created messages and also sets this value for all
     * field parsers in parsing guides.
     *
     * @param flag the flag
     */
    public void setForceStringEncoding(boolean flag) {
        forceStringEncoding = flag;
        for (Map<Integer, FieldParseInfo> pm : parseMap.values()) {
            for (FieldParseInfo parser : pm.values()) {
                parser.setForceStringDecoding(flag);
            }
        }
    }

    /**
     * Is variable length fields in hex boolean.
     *
     * @return the boolean
     */
    public boolean isVariableLengthFieldsInHex() {
        return variableLengthFieldsInHex;
    }

    /**
     * This flag gets passed on to newly created messages and also sets this value for all
     * field parsers in parsing guides.
     *
     * @param flag the flag
     */
    public void setVariableLengthFieldsInHex(boolean flag) {
        this.variableLengthFieldsInHex = flag;
        for (Map<Integer, FieldParseInfo> pm : parseMap.values()) {
            for (FieldParseInfo parser : pm.values()) {
                parser.setForceHexadecimalLength(flag);
            }
        }
    }

    /**
     * Returns true if the factory is set to create and parse bitmaps in binary format
     * when the messages are encoded as text.
     *
     * @return the boolean
     */
    public boolean isUseBinaryBitmap() {
        return binBitmap;
    }

    /**
     * Tells the factory to create messages that encode their bitmaps in binary format
     * even when they're encoded as text. Has no effect on binary messages.
     *
     * @param flag the flag
     */
    public void setUseBinaryBitmap(boolean flag) {
        binBitmap = flag;
    }

    /**
     * Returns the encoding used to parse ALPHA, LLVAR and LLLVAR fields. The default is the
     * file.encoding system property.
     *
     * @return the character encoding
     */
    public String getCharacterEncoding() {
        return encoding;
    }

    /**
     * Sets the character encoding used for parsing ALPHA, LLVAR and LLLVAR fields.
     *
     * @param value the value
     */
    public void setCharacterEncoding(String value) {
        if (encoding == null) {
            throw new IllegalArgumentException("Cannot set null encoding.");
        }
        encoding = value;
        if (!parseMap.isEmpty()) {
            for (Map<Integer, FieldParseInfo> pt : parseMap.values()) {
                for (FieldParseInfo fpi : pt.values()) {
                    fpi.setCharacterEncoding(encoding);
                }
            }
        }
        if (!typeTemplates.isEmpty()) {
            for (T tmpl : typeTemplates.values()) {
                tmpl.setCharacterEncoding(encoding);
                for (int i = 2; i <= MAX_AMOUNT_OF_FIELDS; i++) {
                    IsoValue<?> v = tmpl.getField(i);
                    if (v != null) {
                        v.setCharacterEncoding(encoding);
                    }
                }
            }
        }
    }

    /**
     * Is force secondary bitmap boolean.
     *
     * @return the boolean
     */
    public boolean isForceSecondaryBitmap() {
        return forceb2;
    }

    /**
     * Sets or clears the flag to pass to new messages, to include a secondary bitmap
     * even if it's not needed.
     *
     * @param flag the flag
     */
    public void setForceSecondaryBitmap(boolean flag) {
        forceb2 = flag;
    }

    /**
     * Sets or clears the flag to specify if field P-65 should be interpreted as a tertiary bitmap
     *
     * @param flag the flag
     */
    public void setUseTertiaryBitmap(boolean flag) {
        useTertiaryBitmap = flag;
    }

    /**
     * Tertiary bitmap is used boolean.
     *
     * @return the boolean
     */
    public boolean tertiaryBitmapIsUsed() {
        return useTertiaryBitmap;
    }

    /**
     * This flag indicates if the MessageFactory throws an exception if the last field of a message
     * is not really present even though it's specified in the bitmap. Default is false which means
     * an exception is thrown.
     *
     * @return the ignore last missing field
     */
    public boolean getIgnoreLastMissingField() {
        return ignoreLast;
    }

    /**
     * Setting this property to true avoids getting a ParseException when parsing messages that don't have
     * the last field specified in the bitmap. This is common with certain providers where field 128 is
     * specified in the bitmap but not actually included in the messages. Default is false, which has
     * been the behavior in previous versions when this option didn't exist.
     *
     * @param flag the flag
     */
    public void setIgnoreLastMissingField(boolean flag) {
        ignoreLast = flag;
    }

    /**
     * Specifies a map for custom field encoder/decoders. The keys are the field numbers.
     *
     * @param value the value
     */
    @SuppressWarnings("rawtypes")
    public void setCustomFields(Map<Integer, CustomField> value) {
        customFields = value;
    }

    /**
     * Sets the CustomField encoder for the specified field number.
     *
     * @param index the index
     * @param value the value
     */
    public void setCustomField(int index, CustomField<?> value) {
        customFields.put(index, value);
    }

    /**
     * Returns a custom field encoder/decoder for the specified field number, if one is available.
     *
     * @param <F>   the type parameter
     * @param index the index
     * @return the custom field
     */
    @SuppressWarnings("unchecked")
    public <F> CustomField<F> getCustomField(int index) {
        return customFields.get(index);
    }

    /**
     * Returns a custom field encoder/decoder for the specified field number, if one is available.
     *
     * @param <F>   the type parameter
     * @param index the index
     * @return the custom field
     */
    @SuppressWarnings("unchecked")
    public <F> CustomField<F> getCustomField(Integer index) {
        return customFields.get(index);
    }

    /**
     * Tells the receiver to read the configuration at the specified path. This just calls
     * ConfigParser.configureFromClasspathConfig() with itself and the specified path at arguments,
     * but is really convenient in case the MessageFactory is being configured from within, say, Spring.
     *
     * @param path the path
     * @throws IOException the io exception
     */
    public void setConfigPath(String path) throws IOException {
        ConfigParser.configureFromClasspathConfig(this, path);
        //Now re-set some properties that need to be propagated down to the recently assigned objects
        setCharacterEncoding(encoding);
        setForceStringEncoding(forceStringEncoding);
    }

    /**
     * Returns true is the factory is set to create and parse binary messages,
     * false if it uses ASCII messages. Default is false. True if both binaryHeader &amp; binaryFields
     * are set to true
     *
     * @return the use binary messages
     * @deprecated Check the new flags binaryHeader and binaryFields instead.
     */
    @Deprecated
    public boolean getUseBinaryMessages() {
        return binaryHeader && binaryFields;
    }

    /**
     * Tells the receiver to create and parse binary messages if the flag is true.
     * Default is false, that is, create and parse ASCII messages. Sets both binaryHeader and fields to the flag.
     *
     * @param flag the flag
     */
    public void setUseBinaryMessages(boolean flag) {
        binaryHeader = binaryFields = flag;
    }

    /**
     * header portion of the message is written/parsed in binary, default is false
     *
     * @return the boolean
     */
    public boolean isBinaryHeader() {
        return binaryHeader;
    }

    /**
     * header portion of the message is written/parsed in binary, default is false
     *
     * @param flag the flag
     */
    public void setBinaryHeader(boolean flag) {
        binaryHeader = flag;
    }

    /**
     * fields portion of the message is written/parsed in binary, default is false
     *
     * @return the boolean
     */
    public boolean isBinaryFields() {
        return binaryFields;
    }

    /**
     * fields portion of the message is written/parsed in binary, default is false
     *
     * @param flag the flag
     */
    public void setBinaryFields(boolean flag) {
        binaryFields = flag;
    }


    /** fields portion of the message is written/parsed in binary */

    /**
     * Gets etx.
     *
     * @return the etx
     */
    public int getEtx() {
        return etx;
    }

    /**
     * Sets the ETX character to be sent at the end of the message. This is optional and the
     * default is -1, which means nothing should be sent as terminator.
     *
     * @param value The ASCII value of the ETX character or -1 to indicate no terminator should be used.
     */
    public void setEtx(int value) {
        etx = value;
    }

    /**
     * Creates a new message of the specified type, with optional trace and date values as well
     * as any other values specified in a message template. If the factory is set to use binary
     * messages, then the returned message will be written using binary coding.
     *
     * @param type The message type, for example 0x200, 0x400, etc.
     * @return the t
     */
    public T newMessage(int type) {
        T m;
        if (binIsoHeaders.get(type) != null) {
            m = createIsoMessageWithBinaryHeader(binIsoHeaders.get(type));
        } else {
            m = createIsoMessage(isoHeaders.get(type));
        }
        m.setType(type);
        m.setEtx(etx);
        m.setBinaryHeader(isBinaryHeader());
        m.setBinaryFields(isBinaryFields());
        m.setForceSecondaryBitmap(forceb2);
        m.setBinaryBitmap(binBitmap);
        m.setCharacterEncoding(encoding);
        m.setForceStringEncoding(forceStringEncoding);
        m.setEncodeVariableLengthFieldsInHex(variableLengthFieldsInHex);

        //Copy the values from the template
        IsoMessage templ = typeTemplates.get(type);
        if (templ != null) {
            for (int i = 2; i <= MAX_AMOUNT_OF_FIELDS; i++) {
                if (templ.hasField(i)) {
                    //We could detect here if there's a custom object with a CustomField,
                    //but we can't copy the value so there's no point.
                    m.setField(i, new IsoValue<>(templ.getField(i)));
                }
            }
        }
        if (traceGen != null) {
            m.setValue(11, traceGen.nextTrace(), IsoType.NUMERIC, 6);
        }
        if (setDate) {
            if (m.hasField(7)) {
                //We may have a field with a timezone but no value
                m.updateValue(7, new Date());
            } else {
                IsoValue<Date> now = new IsoValue<>(IsoType.DATE10, new Date());
                if (DateTimeParseInfo.getDefaultTimeZone() != null) {
                    now.setTimeZone(DateTimeParseInfo.getDefaultTimeZone());
                }
                m.setField(7, now);
            }
        }
        return m;
    }

    /**
     * Creates a response message by calling {@link #createResponse(IsoMessage, boolean)}
     * with true as the second parameter.
     *
     * @param request the request
     * @return the t
     */
    public T createResponse(T request) {
        return createResponse(request, true);
    }

    /**
     * Creates a message to respond to a request. Increments the message type by 16,
     * sets all fields from the template if there is one,
     * and either copies all values from the request or only the ones already in the template,
     * depending on the value of copyAllFields flag.
     *
     * @param request       An ISO8583 message with a request type (ending in 00).
     * @param copyAllFields If true, copies all fields from the request to the response,                      overwriting any values already set from the template; otherwise                      it only overwrites values for existing fields from the template.                      If the template for a response does not exist, then all fields from                      the request are copied even in this flag is false.
     * @return the t
     */
    public T createResponse(T request, boolean copyAllFields) {
        T resp = createIsoMessage(isoHeaders.get(request.getType() + 16));
        resp.setCharacterEncoding(request.getCharacterEncoding());
        resp.setBinaryHeader(request.isBinaryHeader());
        resp.setBinaryFields(request.isBinaryFields());
        resp.setBinaryBitmap(request.isBinaryBitmap());
        resp.setType(request.getType() + 16);
        resp.setEtx(etx);
        resp.setForceSecondaryBitmap(forceb2);
        resp.setEncodeVariableLengthFieldsInHex(request.isEncodeVariableLengthFieldsInHex());
        //Copy the values from the template or the request (request has preference)
        IsoMessage templ = typeTemplates.get(resp.getType());
        if (templ == null) {
            for (int i = 2; i <= MAX_AMOUNT_OF_FIELDS; i++) {
                if (request.hasField(i)) {
                    resp.setField(i, new IsoValue<>(request.getField(i)));
                }
            }
        } else if (copyAllFields) {
            for (int i = 2; i <= MAX_AMOUNT_OF_FIELDS; i++) {
                if (request.hasField(i)) {
                    resp.setField(i, new IsoValue<>(request.getField(i)));
                } else if (templ.hasField(i)) {
                    resp.setField(i, new IsoValue<>(templ.getField(i)));
                }
            }
        } else {
            for (int i = 2; i <= MAX_AMOUNT_OF_FIELDS; i++) {
                if (templ.hasField(i)) {
                    IsoMessage srcmsg = request.hasField(i) ? request : templ;
                    resp.setField(i, new IsoValue<>(srcmsg.getField(i)));
                }
            }
        }
        return resp;
    }

    /**
     * Sets the timezone for the specified FieldParseInfo, if it's needed for parsing dates.
     *
     * @param messageType the message type
     * @param field       the field
     * @param tz          the tz
     */
    public void setTimezoneForParseGuide(int messageType, int field, TimeZone tz) {
        if (field == 0) {
            DateTimeParseInfo.setDefaultTimeZone(tz);
        }
        Map<Integer, FieldParseInfo> guide = parseMap.get(messageType);
        if (guide != null) {
            FieldParseInfo fpi = guide.get(field);
            if (fpi instanceof DateTimeParseInfo) {
                ((DateTimeParseInfo) fpi).setTimeZone(tz);
                return;
            }
        }
        log.warn("Field {} for message type {} is not for dates, cannot set timezone",
                field, messageType);
    }

    /**
     * Convenience for parseMessage(buf, isoHeaderLength, false)
     *
     * @param buf             the buf
     * @param isoHeaderLength the iso header length
     * @return the t
     * @throws ParseException               the parse exception
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    public T parseMessage(byte[] buf, int isoHeaderLength)
            throws ParseException, UnsupportedEncodingException {
        return parseMessage(buf, isoHeaderLength, false);
    }

    /**
     * Creates a new message instance from the buffer, which must contain a valid ISO8583
     * message. If the factory is set to use binary messages then it will try to parse
     * a binary message.
     *
     * @param buf             The byte buffer containing the message. Must not include the length header.
     * @param isoHeaderLength The expected length of the ISO header, after which the message type and the rest of the message must come.
     * @param binaryIsoHeader the binary iso header
     * @return the t
     * @throws ParseException               the parse exception
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    public T parseMessage(byte[] buf, int isoHeaderLength, boolean binaryIsoHeader)
            throws ParseException, UnsupportedEncodingException {
        final int minlength = isoHeaderLength + (binaryHeader ? 2 : 4) + (binBitmap || binaryHeader ? 8 : 16);
        if (buf.length < minlength) {
            throw new ParseException("Insufficient buffer length, needs to be at least " + minlength, 0);
        }
        final T m;
        // parse the header
        if (binaryIsoHeader && isoHeaderLength > 0) {
            byte[] _bih = new byte[isoHeaderLength];
            System.arraycopy(buf, 0, _bih, 0, isoHeaderLength);
            m = createIsoMessageWithBinaryHeader(_bih);
        } else {
            m = createIsoMessage(isoHeaderLength > 0 ?
                    new String(buf, 0, isoHeaderLength, encoding) : null);
        }
        m.setCharacterEncoding(encoding);
        final int type;
        if (binaryHeader) {
            type = ((buf[isoHeaderLength] & 0xff) << 8) | (buf[isoHeaderLength + 1] & 0xff);
        } else if (forceStringEncoding) {
            type = Integer.parseInt(new String(buf, isoHeaderLength, 4, encoding), 16);
        } else {
            type = ((buf[isoHeaderLength] - 48) << 12)
                    | ((buf[isoHeaderLength + 1] - 48) << 8)
                    | ((buf[isoHeaderLength + 2] - 48) << 4)
                    | (buf[isoHeaderLength + 3] - 48);
        }
        m.setType(type);
        //Parse the bitmap (primary first)
        final BitSet bs = new BitSet(PRIMARY_BITMAP_SIZE);
        int pos = 0;
        if (binaryHeader || binBitmap) {
            pos = isoHeaderLength + (binaryHeader ? 2 : 4);
            final byte[] primaryBitmap = new byte[8];
            System.arraycopy(buf, pos, primaryBitmap, 0, primaryBitmap.length);
            updateBitSetFromBinaryBitmap(bs, primaryBitmap, START_OF_PRIMARY_BITMAP_FIELDS - 1); // field x can be found at bitmap position x-1
            pos += primaryBitmap.length;
            //Check for secondary bitmap and parse if necessary
            if (bs.get(0)) {
                if (buf.length < minlength + 8) {
                    throw new ParseException("Insufficient length for secondary bitmap", minlength);
                }
                final byte[] secondaryBitmap = new byte[8];
                System.arraycopy(buf, pos, secondaryBitmap, 0, primaryBitmap.length);
                updateBitSetFromBinaryBitmap(bs, secondaryBitmap, 64);
                pos += secondaryBitmap.length;
            }
        } else {
            //ASCII parsing
            try {
                final byte[] primaryBitmap = new byte[16];
                int primaryBitmapStart = isoHeaderLength + 4;
                if (forceStringEncoding) {
                    byte[] _bb = new String(buf, primaryBitmapStart, 16, encoding).getBytes();
                    System.arraycopy(_bb, 0, primaryBitmap, 0, primaryBitmap.length);
                } else {
                    System.arraycopy(buf, primaryBitmapStart, primaryBitmap, 0, 16);
                }
                updateBitSetFromAsciiBitMap(bs, primaryBitmap, START_OF_PRIMARY_BITMAP_FIELDS - 1, primaryBitmapStart); // field x can be found at position x-1
                //Check for secondary bitmap and parse it if necessary
                if (bs.get(0)) {
                    int secondaryBitmapStart = primaryBitmapStart + primaryBitmap.length;
                    final byte[] secondaryBitmap = new byte[16];
                    if (buf.length < minlength + secondaryBitmap.length) {
                        throw new ParseException("Insufficient length for secondary bitmap", minlength);
                    }
                    if (forceStringEncoding) {
                        byte[] _bb = new String(buf, secondaryBitmapStart, secondaryBitmap.length, encoding).getBytes();
                        System.arraycopy(_bb, 0, secondaryBitmap, 0, secondaryBitmap.length);
                    } else {
                        System.arraycopy(buf, secondaryBitmapStart, secondaryBitmap, 0, 16);
                    }
                    updateBitSetFromAsciiBitMap(bs, secondaryBitmap, START_OF_SECONDARY_BITMAP_FIELDS - 1, secondaryBitmapStart); // field x can be found at position x-1
                    pos = minlength + secondaryBitmap.length; // end of bitmap
                } else {
                    pos = minlength; // end of bitmap
                }
            } catch (NumberFormatException ex) {
                ParseException _e = new ParseException("Invalid ISO8583 bitmap", pos);
                _e.initCause(ex);
                throw _e;
            }
        }
        //Parse each field
        Map<Integer, FieldParseInfo> parseGuide = parseMap.get(type);
        List<Integer> index = parseOrder.get(type);
        if (index == null) {
            log.error(String.format("ISO8583 MessageFactory has no parsing guide for message type %04x [%s]",
                    type, new String(buf)));
            throw new ParseException(String.format(
                    "ISO8583 MessageFactory has no parsing guide for message type %04x [%s]",
                    type,
                    new String(buf)), 0);
        }
        //First we check if the message contains fields not specified in the parsing template
        assertAllFieldsPresentHaveParsingGuides(type, bs, index);
        //Now we parse each field
        if (binaryFields) {
            for (Integer i : index) {
                FieldParseInfo fpi = parseGuide.get(i);
                if (bs.get(i - 1)) {
                    if (ignoreLast && pos >= buf.length && i.intValue() == index.get(index.size() - 1)) {
                        log.warn("Field {} is not really in the message even though it's in the bitmap", i);
                        bs.clear(i - 1);
                    } else {
                        CustomField<?> decoder = fpi.getDecoder();
                        if (decoder == null) {
                            decoder = getCustomField(i);
                        }
                        IsoValue<?> val = (VARIABLE_LENGTH_VAR_TYPES.contains(fpi.getType()) && forceStringEncoding) ?
                                fpi.parse(i, buf, pos, decoder)
                                : fpi.parseBinary(i, buf, pos, decoder);

                        if (useTertiaryBitmap && i == IsoMessage.INDEX_OF_TERTIARY_BITMAP) {
                            final byte[] tertiaryBitmap = (byte[]) val.getValue();
                            updateBitSetFromBinaryBitmap(bs, tertiaryBitmap, START_OF_TERTIARY_BITMAP_FIELDS - 1); // field x can be found at bitmap position x-1
                            assertAllFieldsPresentHaveParsingGuides(type, bs, index); // check again for the new fields added to the bitmap
                        }

                        m.setField(i, val);
                        if (val != null) {
                            if (val.getType() == IsoType.NUMERIC || val.getType() == IsoType.DATE10
                                    || val.getType() == IsoType.DATE4
                                    || val.getType() == IsoType.DATE12
                                    || val.getType() == IsoType.DATE14
                                    || val.getType() == IsoType.DATE6
                                    || val.getType() == IsoType.DATE_EXP
                                    || val.getType() == IsoType.AMOUNT
                                    || val.getType() == IsoType.TIME
                                    || val.getType() == IsoType.LLBINLENGTHNUM
                                    || val.getType() == IsoType.LLLLBINLENGTHNUM) {
                                pos += (val.getLength() / 2) + (val.getLength() % 2);
                            } else if (val.getType() == IsoType.LLBCDBIN || val.getType() == IsoType.LLLBCDBIN || val.getType() == IsoType.LLLLBCDBIN || val.getType() == IsoType.LLLLBINLENGTHBIN) {
                                pos += val.getLength() / 2 + ((val.getLength() % 2 == 0) ? 0 : 1);
                            } else {
                                pos += val.getLength();
                            }

                            if (VARIABLE_LENGTH_VAR_TYPES.contains(fpi.getType()) && forceStringEncoding) {
                                if (val.getType() == IsoType.LLVAR) {
                                    pos += 2;
                                } else if (val.getType() == IsoType.LLLVAR) {
                                    pos += 3;
                                } else if (val.getType() == IsoType.LLLLVAR) {
                                    pos += 4;
                                }
                            } else if (val.getType() == IsoType.LLVAR || val.getType() == IsoType.LLBIN || val.getType() == IsoType.LLBCDBIN || val.getType() == IsoType.LLBINLENGTHNUM || val.getType() == IsoType.LLBINLENGTHALPHANUM || val.getType() == IsoType.LLBINLENGTHBIN || val.getType() == IsoType.LLBCDLENGTHALPHANUM) {
                                pos++;
                            } else if (val.getType() == IsoType.LLLVAR
                                    || val.getType() == IsoType.LLLBIN
                                    || val.getType() == IsoType.LLLBCDBIN
                                    || val.getType() == IsoType.LLLLVAR
                                    || val.getType() == IsoType.LLLLBIN
                                    || val.getType() == IsoType.LLLLBCDBIN
                                    || val.getType() == IsoType.LLLLBINLENGTHNUM
                                    || val.getType() == IsoType.LLLLBINLENGTHBIN
                                    || val.getType() == IsoType.LLLLBINLENGTHALPHANUM) {
                                pos += 2;
                            }
                        }
                    }
                }
            }
        } else {
            for (Integer i : index) {
                FieldParseInfo fpi = parseGuide.get(i);
                if (bs.get(i - 1)) {
                    if (ignoreLast && pos >= buf.length && i.intValue() == index.get(index.size() - 1)) {
                        log.warn("Field {} is not really in the message even though it's in the bitmap", i);
                        bs.clear(i - 1);
                    } else {
                        CustomField<?> decoder = fpi.getDecoder();
                        if (decoder == null) {
                            decoder = getCustomField(i);
                        }
                        IsoValue<?> val = fpi.parse(i, buf, pos, decoder);
                        if (useTertiaryBitmap && i == IsoMessage.INDEX_OF_TERTIARY_BITMAP) {
                            final byte[] tertiaryBitmap = ((byte[]) val.getValue());
                            updateBitSetFromBinaryBitmap(bs, tertiaryBitmap, START_OF_TERTIARY_BITMAP_FIELDS - 1); // field x can be found at position x-1
                            assertAllFieldsPresentHaveParsingGuides(type, bs, index); // check again for the new fields added to the bitmap
                        }
                        m.setField(i, val);
                        //To get the correct next position, we need to get the number of bytes, not chars
                        pos += val.toString().getBytes(fpi.getCharacterEncoding()).length;
                        if (val.getType() == IsoType.LLVAR || val.getType() == IsoType.LLBIN || val.getType() == IsoType.LLBCDBIN || val.getType() == IsoType.LLBCDLENGTHALPHANUM || val.getType() == IsoType.LLBINLENGTHNUM || val.getType() == IsoType.LLBINLENGTHALPHANUM || val.getType() == IsoType.LLBINLENGTHBIN) {
                            pos += 2;
                        } else if (val.getType() == IsoType.LLLVAR || val.getType() == IsoType.LLLBIN || val.getType() == IsoType.LLLBCDBIN) {
                            pos += 3;
                        } else if (val.getType() == IsoType.LLLLVAR || val.getType() == IsoType.LLLLBIN || val.getType() == IsoType.LLLLBCDBIN || val.getType() == IsoType.LLLLBINLENGTHBIN || val.getType() == IsoType.LLLLBINLENGTHALPHANUM) {
                            pos += 4;
                        }
                    }
                }
            }
        }
        m.setBinaryHeader(binaryHeader);
        m.setBinaryFields(binaryFields);
        m.setBinaryBitmap(binBitmap);
        m.setForceStringEncoding(forceStringEncoding);
        return m;
    }

    private void assertAllFieldsPresentHaveParsingGuides(int messageType, BitSet bs, List<Integer> fieldsWithParseGuide) throws ParseException {
        boolean abandon = false;
        for (int i = 1; i < bs.length(); i++) {
            if (bs.get(i) && !fieldsWithParseGuide.contains(i + 1)) {
                log.warn("ISO8583 MessageFactory cannot parse field {}: unspecified in parsing guide for type {}",
                        i + 1, Integer.toString(messageType, 16));
                abandon = true;
            }
        }
        if (abandon) {
            throw new ParseException("ISO8583 MessageFactory cannot parse fields", 0);
        }
    }

    /**
     * fills the bitset representing the iso fields that are present based on the byteArray.
     *
     * @param bitSet     - the bitset that is to be updated based on the bitmap
     * @param bitmap     - the binary map representing the iso fields
     * @param fieldIndex - which field is the bitmap representing (primary: 1(-64),secondary: 65(-128), tertiary: 129(-192))
     **/
    private void updateBitSetFromBinaryBitmap(BitSet bitSet, byte[] bitmap, int fieldIndex) {
        for (byte value : bitmap) {
            int bit = 0x80;
            for (int b = 0; b < 8; b++) { // manual conversion of byte at position pos - pos+4 to int
                bitSet.set(fieldIndex++, (value & bit) != 0);
                bit >>= 1;
            }
        }
    }

    /**
     * fills the bitset representing the iso fields that are present based on the byteArray.
     *
     * @param bitSet                - the bitset that is to be updated based on the bitmap
     * @param bitmap                - the binary map representing the iso fields
     * @param fieldIndex            - the position on the combined bitmap (primary: 0(-63) representing fields 1-64,secondary: 64(-128), tertiary: 128(-191))
     * @param originalMessageOffset - used to give the exact location in the message in case of error.
     **/
    private void updateBitSetFromAsciiBitMap(BitSet bitSet, byte[] bitmap, int fieldIndex, int originalMessageOffset) throws ParseException {
        int i = 0;
        try {
            for (; i < bitmap.length; i++) {
                if (bitmap[i] >= '0' && bitmap[i] <= '9') {
                    bitSet.set(fieldIndex++, ((bitmap[i] - 48) & 8) > 0);
                    bitSet.set(fieldIndex++, ((bitmap[i] - 48) & 4) > 0);
                    bitSet.set(fieldIndex++, ((bitmap[i] - 48) & 2) > 0);
                    bitSet.set(fieldIndex++, ((bitmap[i] - 48) & 1) > 0);
                } else if (bitmap[i] >= 'A' && bitmap[i] <= 'F') {
                    bitSet.set(fieldIndex++, ((bitmap[i] - 55) & 8) > 0);
                    bitSet.set(fieldIndex++, ((bitmap[i] - 55) & 4) > 0);
                    bitSet.set(fieldIndex++, ((bitmap[i] - 55) & 2) > 0);
                    bitSet.set(fieldIndex++, ((bitmap[i] - 55) & 1) > 0);
                } else if (bitmap[i] >= 'a' && bitmap[i] <= 'f') {
                    bitSet.set(fieldIndex++, ((bitmap[i] - 87) & 8) > 0);
                    bitSet.set(fieldIndex++, ((bitmap[i] - 87) & 4) > 0);
                    bitSet.set(fieldIndex++, ((bitmap[i] - 87) & 2) > 0);
                    bitSet.set(fieldIndex++, ((bitmap[i] - 87) & 1) > 0);
                }
            }
        } catch (NumberFormatException ex) {
            ParseException _e = new ParseException("Invalid ISO8583 bitmap", originalMessageOffset + i);
            _e.initCause(ex);
            throw _e;
        }
    }

    /**
     * Creates a Iso message, override this method in the subclass to provide your
     * own implementations of IsoMessage.
     *
     * @param header The optional ISO header that goes before the message type
     * @return IsoMessage t
     */
    @SuppressWarnings("unchecked")
    protected T createIsoMessage(String header) {
        return (T) new IsoMessage(header);
    }

    /**
     * Creates a Iso message with the specified binary ISO header.
     * Override this method in the subclass to provide your
     * own implementations of IsoMessage.
     *
     * @param binHeader The optional ISO header that goes before the message type
     * @return IsoMessage t
     */
    @SuppressWarnings("unchecked")
    protected T createIsoMessageWithBinaryHeader(byte[] binHeader) {
        return (T) new IsoMessage(binHeader);
    }

    /**
     * Returns true if the factory is assigning the current date to newly created messages
     * (field 7). Default is false.
     *
     * @return the assign date
     */
    public boolean getAssignDate() {
        return setDate;
    }

    /**
     * Sets whether the factory should set the current date on newly created messages,
     * in field 7. Default is false.
     *
     * @param flag the flag
     */
    public void setAssignDate(boolean flag) {
        setDate = flag;
    }

    /**
     * Returns the generator used to assign trace numbers to new messages.
     *
     * @return the trace number generator
     */
    public TraceNumberGenerator getTraceNumberGenerator() {
        return traceGen;
    }

    /**
     * Sets the generator that this factory will get new trace numbers from. There is no
     * default generator.
     *
     * @param value the value
     */
    public void setTraceNumberGenerator(TraceNumberGenerator value) {
        traceGen = value;
    }

    /**
     * Sets the ISO header to be used in each message type.
     *
     * @param value A map where the keys are the message types and the values are the ISO headers.
     */
    public void setIsoHeaders(Map<Integer, String> value) {
        isoHeaders.clear();
        isoHeaders.putAll(value);
    }

    /**
     * Sets the ISO header for a specific message type.
     *
     * @param type  The message type, for example 0x200.
     * @param value The ISO header, or NULL to remove any headers for this message type.
     */
    public void setIsoHeader(int type, String value) {
        if (value == null) {
            isoHeaders.remove(type);
        } else {
            isoHeaders.put(type, value);
            binIsoHeaders.remove(type);
        }
    }

    /**
     * Returns the ISO header used for the specified type.
     *
     * @param type the type
     * @return the iso header
     */
    public String getIsoHeader(int type) {
        return isoHeaders.get(type);
    }

    /**
     * Sets the ISO header for a specific message type, in binary format.
     *
     * @param type  The message type, for example 0x200.
     * @param value The ISO header, or NULL to remove any headers for this message type.
     */
    public void setBinaryIsoHeader(int type, byte[] value) {
        if (value == null) {
            binIsoHeaders.remove(type);
        } else {
            binIsoHeaders.put(type, value);
            isoHeaders.remove(type);
        }
    }

    /**
     * Returns the binary ISO header used for the specified type.
     *
     * @param type the type
     * @return the byte [ ]
     */
    public byte[] getBinaryIsoHeader(int type) {
        return binIsoHeaders.get(type);
    }

    /**
     * Adds a message template to the factory. If there was a template for the same
     * message type as the new one, it is overwritten.
     *
     * @param templ the templ
     */
    public void addMessageTemplate(T templ) {
        if (templ != null) {
            typeTemplates.put(templ.getType(), templ);
        }
    }

    /**
     * Removes the message template for the specified type.
     *
     * @param type the type
     */
    public void removeMessageTemplate(int type) {
        typeTemplates.remove(type);
    }

    /**
     * Returns the template for the specified message type. This allows templates to be modified
     * programmatically.
     *
     * @param type the type
     * @return the message template
     */
    public T getMessageTemplate(int type) {
        return typeTemplates.get(type);
    }

    /**
     * Invoke this method in case you want to freeze the configuration, making message and parsing
     * templates, as well as iso headers and custom fields, immutable.
     */
    public void freeze() {
        typeTemplates = Collections.unmodifiableMap(typeTemplates);
        parseMap = Collections.unmodifiableMap(parseMap);
        parseOrder = Collections.unmodifiableMap(parseOrder);
        isoHeaders = Collections.unmodifiableMap(isoHeaders);
        binIsoHeaders = Collections.unmodifiableMap(binIsoHeaders);
        customFields = Collections.unmodifiableMap(customFields);
    }

    /**
     * Sets a map with the fields that are to be expected when parsing a certain type of
     * message.
     *
     * @param type The message type.
     * @param map  A map of FieldParseInfo instances, each of which define what type and length of field to expect. The keys will be the field numbers.
     */
    public void setParseMap(int type, Map<Integer, FieldParseInfo> map) {
        parseMap.put(type, map);
        ArrayList<Integer> index = new ArrayList<>();
        index.addAll(map.keySet());
        Collections.sort(index);
        log.trace(String.format("ISO8583 MessageFactory adding parse map for type %04x with fields %s",
                type, index));
        parseOrder.put(type, index);
    }

}
