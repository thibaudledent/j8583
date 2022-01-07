package com.solab.iso8583;

/**
 * Defines the behavior of a custom field encoder, which will convert a value of some
 * data type to a String.
 *
 * @param <DataType> the type parameter
 * @author Enrique Zamudio Date: 2019-02-08 11:20
 */
public interface CustomFieldEncoder<DataType> {

    /**
     * Encode field string.
     *
     * @param value the value
     * @return the string
     */
    String encodeField(DataType value);
}
