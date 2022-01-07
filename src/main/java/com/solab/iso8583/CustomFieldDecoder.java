package com.solab.iso8583;

/**
 * A functional interface to decode a value from a field (string or binary data) into some
 * other data type.
 *
 * @param <DataType> the type parameter
 * @author Enrique Zamudio Date: 2019-02-08 11:21
 */
public interface CustomFieldDecoder<DataType> {

    /**
     * Decode field data type.
     *
     * @param value the value
     * @return the data type
     */
    DataType decodeField(String value);
}
