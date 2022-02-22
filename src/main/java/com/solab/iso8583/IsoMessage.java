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

import com.solab.iso8583.util.HexCodec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.Map;

/**
 * Represents an ISO8583 message. This is the core class of the framework.
 * Contains the bitmap which is modified as fields are added/removed.
 * This class makes no assumptions as to what types belong in each field,
 * nor what fields should each different message type have; that is left
 * for the developer, since the different ISO8583 implementations can vary
 * greatly.
 *
 * @author Enrique Zamudio
 */
public class IsoMessage {

    /**
     * The Hex.
     */
    static final byte[] HEX = new byte[]{ '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };


    /**
     * The End of primary bitmap fields.
     */
    static final int END_OF_PRIMARY_BITMAP_FIELDS = 64;
    /**
     * The End of secondary bitmap fields.
     */
    static final int END_OF_SECONDARY_BITMAP_FIELDS = 128;
    /**
     * The End of tertiary bitmap fields.
     */
    static final int END_OF_TERTIARY_BITMAP_FIELDS = 192;
    /**
     * The Index of tertiary bitmap.
     */
    static final int INDEX_OF_TERTIARY_BITMAP = 65;

    /**
     * The constant MAX_AMOUNT_OF_FIELDS.
     */
    public static final int MAX_AMOUNT_OF_FIELDS = END_OF_TERTIARY_BITMAP_FIELDS;
    /**
     * The Primary bitmap size.
     */
    static final int PRIMARY_BITMAP_SIZE = END_OF_PRIMARY_BITMAP_FIELDS;
    /**
     * The Extended bitmap size.
     */
    static final int EXTENDED_BITMAP_SIZE = END_OF_SECONDARY_BITMAP_FIELDS;

    /**
     * The Start of primary bitmap fields.
     */
    static final int START_OF_PRIMARY_BITMAP_FIELDS = 1;
    /**
     * The Start of secondary bitmap fields.
     */
    static final int START_OF_SECONDARY_BITMAP_FIELDS = END_OF_PRIMARY_BITMAP_FIELDS + 1;
    /**
     * The Start of tertiary bitmap fields.
     */
    static final int START_OF_TERTIARY_BITMAP_FIELDS = END_OF_SECONDARY_BITMAP_FIELDS + 1;

    /** The message type. */
    private int type;

    private boolean binaryHeader;
    private boolean binaryFields;

    /** This is where the values are stored. */
    @SuppressWarnings("rawtypes")
	private final IsoValue[] fields = new IsoValue[MAX_AMOUNT_OF_FIELDS + 1];
    /** Stores the optional ISO header. */
    private String isoHeader;
    private byte[] binIsoHeader;
    private int etx = -1;
    /** Flag to enforce secondary bitmap even if empty. */
    private boolean forceb2;
    private boolean binBitmap;

    /* Flag to indicate that fields with an index above 128 are in use and a tertiary bitmap is needed */
    private boolean tertiaryBitmapNeeded = false;
    private boolean forceStringEncoding;
    private boolean encodeVariableLengthFieldsInHex;
    private String encoding = System.getProperty("file.encoding");

    /**
     * Creates a new empty message with no values set.
     */
    public IsoMessage() {
    }

    /**
     * Creates a new message with the specified ISO header. This will be prepended to the message.  
     *
     * @param header the header
     */
    protected IsoMessage(String header) {
    	isoHeader = header;
    }

    /**
     * Creates a new message with the specified binary ISO header. This will be prepended to the message.  
     *
     * @param binaryHeader the binary header
     */
    protected IsoMessage(byte[] binaryHeader) {
    	binIsoHeader = binaryHeader;
    }

    /**
     * Tells the message to encode its bitmap in binary format, even if the message
     * itself is encoded as text. This has no effect if the binary flag is set, which means
     * binary messages will always encode their bitmap in binary format.  
     *
     * @param flag the flag
     */
    public void setBinaryBitmap(boolean flag) {
        binBitmap = flag;
    }

    /**
     * Returns true if the message's bitmap is encoded in binary format, when the message
     * is encoded as text. Default is false.  
     *
     * @return the boolean
     */
    public boolean isBinaryBitmap() {
        return binBitmap;
    }

    /**
     * If set, this flag will cause the secondary bitmap to be written even if it's not needed.  
     *
     * @param flag the flag
     */
    public void setForceSecondaryBitmap(boolean flag) {
    	forceb2 = flag;
    }

    /**
     * Returns true if the secondary bitmap is always included in the message, even
     * if it's not needed. Default is false.  
     *
     * @return the force secondary bitmap
     */
    public boolean getForceSecondaryBitmap() {
    	return forceb2;
    }

    /**
     * Sets the encoding to use.  
     *
     * @param value the value
     */
    public void setCharacterEncoding(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Cannot set null encoding.");
        }
    	encoding = value;
    }

    /**
     * Returns the character encoding for Strings inside the message. Default
     * is taken from the file.encoding system property.  
     *
     * @return the character encoding
     */
    public String getCharacterEncoding() {
    	return encoding;
    }

    /**
     * Specified whether the variable-length fields should encode their length
     * headers using string conversion with the proper character encoding. Default
     * is false, which is the old behavior (encoding as ASCII). This is only useful
     * for text format.  
     *
     * @param flag the flag
     */
    public void setForceStringEncoding(boolean flag) {
        forceStringEncoding = flag;
    }

    /**
     * Specified whether the variable-length fields should encode their length
     * headers using hexadecimal values. This is only useful for binary format.  
     *
     * @param flag the flag
     */
    public void setEncodeVariableLengthFieldsInHex(boolean flag) {
        this.encodeVariableLengthFieldsInHex = flag;
    }

    /**
     * Is encode variable length fields in hex boolean.
     *
     * @return the boolean
     */
    public boolean isEncodeVariableLengthFieldsInHex() {
        return encodeVariableLengthFieldsInHex;
    }

    /**
     * Sets the string to be sent as ISO header, that is, after the length header but before the message type.
     * This is useful in case an application needs some custom data in the ISO header of each message (very rare).  
     *
     * @param value the value
     */
    public void setIsoHeader(String value) {
        isoHeader = value;
        binIsoHeader = null;
    }

    /**
     * Returns the ISO header that this message was created with.  
     *
     * @return the iso header
     */
    public String getIsoHeader() {
    	return isoHeader;
    }

    /**
     * Sets the string to be sent as ISO header, that is, after the length header but before the message type.
     * This is useful in case an application needs some custom data in the ISO header of each message (very rare).  
     *
     * @param binaryHeader the binary header
     */
    public void setBinaryIsoHeader(byte[] binaryHeader) {
        isoHeader = null;
        binIsoHeader = binaryHeader;
    }

    /**
     * Returns the binary ISO header that this message was created with.  
     *
     * @return the byte [ ]
     */
    public byte[] getBinaryIsoHeader() {
        return binIsoHeader;
    }

    /**
     * Sets the ISO message type. Common values are 0x200, 0x210, 0x400, 0x410, 0x800, 0x810.  
     *
     * @param value the value
     */
    public void setType(int value) {
    	type = value;
    }

    /**
     * Returns the ISO message type.  
     *
     * @return the type
     */
    public int getType() {
    	return type;
    }

    /**
     * Indicates whether the message should be binary. Default is false.
     * To encode the message as text but the bitmap in binary format, you can set the
     * binaryBitmap flag.  
     *
     * @param flag the flag
     */
    public void setBinary(boolean flag) {
    	binaryHeader = binaryFields = flag;
    }

    /**
     * Returns true if the message is binary coded (both header and fields); default is false.
     *
     * @return the boolean
     * @deprecated Use the new flags isBinaryHeader and isBinaryFields instead.
     */
    @Deprecated
    public boolean isBinary() {
    	return binaryHeader && binaryFields;
    }

    /**
     * header information is binary encoded  
     * @param flag the flag
     */
    public void setBinaryHeader(boolean flag) {
        binaryHeader = flag;
    }

    /**
     * header information is binary encoded  
     * @return the boolean
     */
    public boolean isBinaryHeader(){
        return binaryHeader;
    }

    /**
     * field data is binary encoded  
     * @param flag the flag
     */
    public void setBinaryFields(boolean flag){
        binaryFields = flag;
    }

    /**
     * field data is binary encoded  
     * @return the boolean
     */
    public boolean isBinaryFields(){
       return binaryFields;
    }

    /**
     * Sets the ETX character, which is sent at the end of the message as a terminator.
     * Default is -1, which means no terminator is sent.  
     * @param value the value
     */
    public void setEtx(int value) {
    	etx = value;
    }

    /**
     * Returns the stored value in the field, without converting or formatting it.
     *
     * @param <T>   the type parameter
     * @param field The field number. 1 is the secondary bitmap and is not returned as such; real fields go from 2 to 128.
     * @return the object value
     */
    public <T> T getObjectValue(int field) {
    	@SuppressWarnings("unchecked")
    	IsoValue<T> v = fields[field];
    	return v == null ? null : v.getValue();
    }

    /**
     * Returns the IsoValue for the specified field. First real field is 2.  
     * @param <T>  the type parameter
     *
     * @param field the field
     * @return the field
     */
    @SuppressWarnings("unchecked")
    public <T> IsoValue<T> getField(int field) {
    	return fields[field];
    }

    /**
     * Stored the field in the specified index. The first field is the secondary bitmap and has index 1,
     * so the first valid value for index must be 2.
     *
     * @param index the index
     * @param field the field
     * @return The receiver (useful for setting several fields in sequence).
     */
    public IsoMessage setField(int index, IsoValue<?> field) {
    	if (index < 2 || index > MAX_AMOUNT_OF_FIELDS) {
    		throw new IndexOutOfBoundsException("Field index must be between 2 and " + MAX_AMOUNT_OF_FIELDS);
    	}
    	if (index > END_OF_SECONDARY_BITMAP_FIELDS) {
            tertiaryBitmapNeeded = true;
        }
    	if (field != null) {
        	field.setCharacterEncoding(encoding);
    	}
    	fields[index] = field;
    	return this;
    }

    /**
     * Convenience method for setting several fields in one call.  
  * @param values the values
     *
     * @return the fields
     */
    public IsoMessage setFields(Map<Integer, IsoValue<?>> values) {
    	for (Map.Entry<Integer, IsoValue<?>> e : values.entrySet()) {
    		setField(e.getKey(), e.getValue());
    	}
    	return this;
    }

    /**
     * Sets the specified value in the specified field, creating an IsoValue internally.
     *
     * @param index  The field number (2 to 128)
     * @param value  The value to be stored.
     * @param t      The ISO type.
     * @param length The length of the field, used for ALPHA and NUMERIC values only, ignored with any other type.
     * @return The receiver (useful for setting several values in sequence).
     */
    public IsoMessage setValue(int index, Object value, IsoType t, int length) {
    	return setValue(index, value, null, t, length);
    }

    /**
     * Sets the specified value in the specified field, creating an IsoValue internally.
     *
     * @param <T>     the type parameter
     * @param index   The field number (2 to 128)
     * @param value   The value to be stored.
     * @param encoder An optional CustomFieldEncoder for the value.
     * @param t       The ISO type.
     * @param length  The length of the field, used for ALPHA and NUMERIC values only, ignored with any other type.
     * @return The receiver (useful for setting several values in sequence).
     */
    public <T> IsoMessage setValue(int index, T value, CustomFieldEncoder<T> encoder, IsoType t, int length) {
    	if (index < 2 || index > MAX_AMOUNT_OF_FIELDS) {
    		throw new IndexOutOfBoundsException("Field index must be between 2 and " + MAX_AMOUNT_OF_FIELDS);
    	}
    	if (value == null) {
    		fields[index] = null;
    	} else {
            if (index > END_OF_SECONDARY_BITMAP_FIELDS) {
                tertiaryBitmapNeeded = true;
            }
    		IsoValue<T> v = null;
    		if (t.needsLength()) {
    			v = new IsoValue<>(t, value, length, encoder);
    		} else {
    			v = new IsoValue<>(t, value, encoder);
    		}
    		v.setCharacterEncoding(encoding);
    		fields[index] = v;
    	}
    	return this;
    }

    /**
     * A convenience method to set new values in fields that already contain values.
     * The field's type, length and custom encoder are taken from the current value.
     * This method can only be used with fields that have been previously set,
     * usually from a template in the MessageFactory.
     *
     * @param <T>   the type parameter
     * @param index The field's index
     * @param value The new value to be set in that field.
     * @return The message itself.
     * @throws IllegalArgumentException if there is no current field at the specified index.
     */
    public <T> IsoMessage updateValue(int index, T value) {
        IsoValue<T> current = getField(index);
        if (current == null) {
            throw new IllegalArgumentException("Value-only field setter can only be used on existing fields");
        } else {
            setValue(index, value, current.getEncoder(), current.getType(), current.getLength());
            getField(index).setCharacterEncoding(current.getCharacterEncoding());
            getField(index).setTimeZone(current.getTimeZone());
        }
        return this;
    }

    /**
     * Returns true is the message has a value in the specified field.
     *
     * @param idx The field number.
     * @return the boolean
     */
    public boolean hasField(int idx) {
    	return fields[idx] != null;
    }

    /**
     * Writes a message to a stream, after writing the specified number of bytes indicating
     * the message's length. The message will first be written to an internal memory stream
     * which will then be dumped into the specified stream. This method flushes the stream
     * after the write. There are at most three write operations to the stream: one for the
     * length header, one for the message, and the last one with for the ETX.
     *
     * @param outs        The stream to write the message to.
     * @param lengthBytes The size of the message length header. Valid ranges are 0 to 4.
     * @throws IOException if there is a problem writing to the stream.
     */
    public void write(OutputStream outs, int lengthBytes) throws IOException {
    	if (lengthBytes > 4) {
    		throw new IllegalArgumentException("The length header can have at most 4 bytes");
    	}
    	byte[] data = writeData();

    	if (lengthBytes > 0) {
    		int l = data.length;
    		if (etx > -1) {
    			l++;
    		}
    		byte[] buf = new byte[lengthBytes];
    		int pos = 0;
    		if (lengthBytes == 4) {
    			buf[0] = (byte)((l & 0xff000000) >> 24);
    			pos++;
    		}
    		if (lengthBytes > 2) {
    			buf[pos] = (byte)((l & 0xff0000) >> 16);
    			pos++;
    		}
    		if (lengthBytes > 1) {
    			buf[pos] = (byte)((l & 0xff00) >> 8);
    			pos++;
    		}
    		buf[pos] = (byte)(l & 0xff);
    		outs.write(buf);
    	}
    	outs.write(data);
    	//ETX
    	if (etx > -1) {
    		outs.write(etx);
    	}
    	outs.flush();
    }

    /**
     * Creates and returns a ByteBuffer with the data of the message, including the length header.
     * The returned buffer is already flipped, so it is ready to be written to a Channel.  
     *
     * @param lengthBytes the length bytes
     * @return the byte buffer
     */
    public ByteBuffer writeToBuffer(int lengthBytes) {
    	if (lengthBytes > 4) {
    		throw new IllegalArgumentException("The length header can have at most 4 bytes");
    	}

    	byte[] data = writeData();
    	ByteBuffer buf = ByteBuffer.allocate(lengthBytes + data.length + (etx > -1 ? 1 : 0));
    	if (lengthBytes > 0) {
    		int l = data.length;
    		if (etx > -1) {
    			l++;
    		}
    		if (lengthBytes == 4) {
                buf.put((byte)((l & 0xff000000) >> 24));
    		}
    		if (lengthBytes > 2) {
                buf.put((byte)((l & 0xff0000) >> 16));
    		}
    		if (lengthBytes > 1) {
                buf.put((byte)((l & 0xff00) >> 8));
    		}
            buf.put((byte)(l & 0xff));
    	}
    	buf.put(data);
    	//ETX
    	if (etx > -1) {
    		buf.put((byte)etx);
    	}
    	buf.flip();
    	return buf;
    }

    /**
     * Creates a BitSet for the bitmap.  
     * @return the bit set
     */
    protected BitSet createBitmapBitSet() {
        BitSet bs = new BitSet(forceb2 ? 128 : 64);
        for (int i = 2 ; i <= END_OF_SECONDARY_BITMAP_FIELDS; i++) {
            if (fields[i] != null) {
                bs.set(i - 1);
            }
        }
        if (forceb2) {
            bs.set(0);
        } else if (bs.length() > PRIMARY_BITMAP_SIZE) {
            //Extend to 128 if needed
            BitSet b2 = new BitSet(EXTENDED_BITMAP_SIZE);
            b2.or(bs);
            bs = b2;
            bs.set(0);
        }
        return bs;
    }

    /**
     * Create tertiary bit set bit set.
     *
     * @return the bit set
     */
    /* Creates a BitSet for fields 129-196 */
    protected BitSet createTertiaryBitSet() {
        BitSet tertiaryBitmap = new BitSet(64);
        for (int i = START_OF_TERTIARY_BITMAP_FIELDS ; i <= END_OF_TERTIARY_BITMAP_FIELDS; i++) {
            if (fields[i] != null) {
                tertiaryBitmap.set(i - START_OF_TERTIARY_BITMAP_FIELDS);
            }
        }
        return tertiaryBitmap;
    }

    private void fillTertiaryBitmapField(){
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        BitSet bitset = createTertiaryBitSet();
        writeBitmapToStreamAsBinary(bout, bitset);
        IsoValue<byte[]> bitmapValue = new IsoValue<>(IsoType.BINARY, bout.toByteArray(), bout.size());
        setField(INDEX_OF_TERTIARY_BITMAP, bitmapValue);
    }

    /**
     * Writes the message to a memory stream and returns a byte array with the result.  
     * @return the byte array
     */
    public byte[] writeData() {
    	ByteArrayOutputStream bout = new ByteArrayOutputStream();
    	if (isoHeader != null) {
    		try {
    			bout.write(isoHeader.getBytes(encoding));
    		} catch (IOException ex) {
    			//should never happen, writing to a ByteArrayOutputStream
    		}
    	} else if (binIsoHeader != null) {
            try {
                bout.write(binIsoHeader);
            } catch (IOException ex) {
                //should never happen, writing to a ByteArrayOutputStream
            }
        }
    	//Message Type
    	if (binaryHeader) {
        	bout.write((type & 0xff00) >> 8);
        	bout.write(type & 0xff);
    	} else {
    		try {
    			bout.write(String.format("%04x", type).getBytes(encoding));
    		} catch (IOException ex) {
    			//should never happen, writing to a ByteArrayOutputStream
    		}
    	}

    	//Bitmap
        if (tertiaryBitmapNeeded){
            fillTertiaryBitmapField();
        }

        BitSet bs = createBitmapBitSet();
        //Write bitmap to stream
        if (binaryHeader || binBitmap) {
            writeBitmapToStreamAsBinary(bout, bs);
        } else {
            writeBitmapToStreamAsAscii(bout, bs);
        }

        //Fields
    	for (int i = 2; i < fields.length; i++) {
    		IsoValue<?> v = fields[i];
    		if (v != null) {
        		try {
        			v.write(bout, binaryFields, forceStringEncoding);
        		} catch (IOException ex) {
        			//should never happen, writing to a ByteArrayOutputStream
        		}
    		}
    	}
    	return bout.toByteArray();
    }

    private void writeBitmapToStreamAsBinary(ByteArrayOutputStream bout, BitSet bs) {
        int bitPosition = 0x80; // byte: 1000 0000
        int resultByte = 0x00;
        for (int i = 0; i < bs.size(); i++) {
            if (bs.get(i)) {
                resultByte |= bitPosition; // set the bit selected by bitPosition to 1
            }
            bitPosition >>= 1; // move the selected bit 1 to the right
            if (bitPosition == 0) { // if all bits have been used
                bout.write(resultByte); // write the resulting bite
                bitPosition = 128; // start over
                resultByte = 0;
            }
        }
    }

    private void writeBitmapToStreamAsAscii(ByteArrayOutputStream bout, BitSet bs) {
        ByteArrayOutputStream bout2 = null;
        if (forceStringEncoding) {
            bout2 = bout;
            bout = new ByteArrayOutputStream();
        }
        int pos = 0;
        int lim = bs.size() / 4;
        for (int i = 0; i < lim; i++) {
            int nibble = 0;
            if (bs.get(pos++))
                nibble |= 8;
            if (bs.get(pos++))
                nibble |= 4;
            if (bs.get(pos++))
                nibble |= 2;
            if (bs.get(pos++))
                nibble |= 1;
            bout.write(HEX[nibble]);
        }
        if (forceStringEncoding) {
            final String _hb = new String(bout.toByteArray());
            bout = bout2;
            try {
                bout.write(_hb.getBytes(encoding));
            } catch (IOException ignore) {
                //never happen
            }
        }
    }

    /**
     * Returns a string representation of the message, as if it were encoded
     * in ASCII with no binary bitmap.  
     * @return the string
     */
    public String debugString() {
        StringBuilder sb = new StringBuilder();
        if (isoHeader != null) {
            sb.append(isoHeader);
        } else if (binIsoHeader != null) {
            sb.append("[0x").append(HexCodec.hexEncode(binIsoHeader, 0, binIsoHeader.length)).append("]");
        }
        sb.append(String.format("%04x", type));

        //Bitmap
        BitSet bs = createBitmapBitSet();
        int pos = 0;
        int lim = bs.size() / 4;
        for (int i = 0; i < lim; i++) {
            int nibble = 0;
            if (bs.get(pos++))
               nibble |= 8;
            if (bs.get(pos++))
               nibble |= 4;
            if (bs.get(pos++))
               nibble |= 2;
            if (bs.get(pos++))
               nibble |= 1;
            sb.append(new String(HEX, nibble, 1));
        }

        //Fields
        for (int i = 2; i <= MAX_AMOUNT_OF_FIELDS; i++) {
            IsoValue<?> v = fields[i];
            if (v != null) {
                String desc = v.toString();
                if (v.getType() == IsoType.LLBIN || v.getType() == IsoType.LLBCDBIN || v.getType() == IsoType.LLVAR || v.getType() == IsoType.LLBINLENGTHNUM || v.getType() == IsoType.LLBINLENGTHALPHANUM || v.getType() == IsoType.LLBINLENGTHBIN)  {
                    sb.append(String.format("%02d", desc.length()));
                } else if (v.getType() == IsoType.LLLBIN || v.getType() == IsoType.LLLBCDBIN || v.getType() == IsoType.LLLVAR) {
                    sb.append(String.format("%03d", desc.length()));
                } else if (v.getType() == IsoType.LLLLBIN || v.getType() == IsoType.LLLLBCDBIN || v.getType() == IsoType.LLLLVAR || v.getType() == IsoType.LLLLBINLENGTHNUM || v.getType() == IsoType.LLLLBINLENGTHBIN) {
                    sb.append(String.format("%04d", desc.length()));
                }
                sb.append(desc);
            }
        }
        return sb.toString();
    }

    //These are for Groovy compat

    /**
     * Sets the specified value in the specified field, just like {@link #setField(int, IsoValue)}.  
     * @param <T>  the type parameter
     *
     * @param i the
     * @param v the v
     */
    public <T> void putAt(int i, IsoValue<T> v) {
    	setField(i, v);
    }

    /**
     * Returns the IsoValue in the specified field, just like {@link #getField(int)}.  
     *
     * @param <T>  the type parameter
     * @param i the
     * @return the at
     */
    public <T> IsoValue<T> getAt(int i) {
    	return getField(i);
    }

	//These are for Scala compat

    /**
     * Sets the specified value in the specified field, just like {@link #setField(int, IsoValue)}.  
     *
     * @param <T>  the type parameter
     * @param i the
     * @param v the v
     */
    public <T> void update(int i, IsoValue<T> v) {
		setField(i, v);
	}

    /**
     * Returns the IsoValue in the specified field, just like {@link #getField(int)}.  
     *
     * @param <T>  the type parameter
     * @param i the
     * @return the iso value
     */
    public <T> IsoValue<T> apply(int i) {
		return getField(i);
	}

    /**
     * Copies the specified fields from the other message into the recipient. If a specified field is
     * not present in the source message it is simply ignored.  
     *
     * @param src the src
     * @param idx the idx
     */
    public void copyFieldsFrom(IsoMessage src, int...idx) {
    	for (int i : idx) {
    		IsoValue<Object> v = src.getField(i);
    		if (v != null) {
        		setValue(i, v.getValue(), v.getEncoder(), v.getType(), v.getLength());
    		}
    	}
    }

    /**
     * Remove the specified fields from the message.  
     * @param idx the idx
     */
    public void removeFields(int... idx) {
        for (int i : idx) {
            setField(i, null);
        }
    }

    /**
     * Returns true is the message contains all the specified fields.
     * A convenience for m.hasField(x) &amp;&amp; m.hasField(y) &amp;&amp; m.hasField(z) &amp;&amp; ...  
     *
     * @param idx the idx
     * @return the boolean
     */
    public boolean hasEveryField(int... idx) {
        for (int i : idx) {
            if (!hasField(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true is the message contains at least one of the specified fields.
     * A convenience for m.hasField(x) || m.hasField(y) || m.hasField(z) || ...  
     *
     * @param idx the idx
     * @return the boolean
     */
    public boolean hasAnyField(int... idx) {
        for (int i : idx) {
            if (hasField(i)) {
                return true;
            }
        }
        return false;
    }
}
