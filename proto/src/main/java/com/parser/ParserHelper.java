package com.parser;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

/**
 * Helper class used by {@link ByteFileParser} to process input streams, byte arrays, and record calculations.
 */
public class ParserHelper {

  /**
   * The type of payment record to be decoded.
   */
  public enum RecordType {
    DEBIT(0, true),
    CREDIT(1, true),
    START_AUTO(2, false),
    END_AUTO(3, false);

    /**
     * The integer value that represents the record type.
     */
    private int recordIndicator;
    
    /**
     * Whether the record type has an amount attached to the record. 
     */
    private boolean hasAmount;

    RecordType(int recordIndicator, boolean hasAmount) {
      this.recordIndicator = recordIndicator;
      this.hasAmount = hasAmount;
    }

    public int getRecordIndicator() {
      return recordIndicator;
    }

    public boolean isHasAmount() {
      return hasAmount;
    }

    /**
     * Returns the matching record type enum from the record indicator value. If no enum values match, return null.
     * 
     * @param recordIndicator the integer value to match on
     * @return the {@link RecordType} enum, or null
     */
    public static RecordType getRecordTypeFromRecordIndicator(int recordIndicator) {
      for (RecordType type : values()) {
        if (type.getRecordIndicator() == recordIndicator) {
          return type;
        }
      }

      return null;
    }
  }

  /**
   * Reads the specified byte length from the given input stream.
   * 
   * @param reader the input stream currently reading a file
   * @param byteLength the number of bytes to read off the input stream
   * @return a byte array containing the specified number of bytes, or an empty or incomplete array if end of file is
   *         reached
   * @throws IOException thrown if an error occurs reading the bytes
   */
  public static byte[] getArray(InputStream reader, int byteLength) throws IOException {
    // Create a byte array for the given length
    byte[] array = new byte[byteLength];

    for (int i = 0; i < byteLength; i++) {
      // Read the byte
      int r = reader.read();
      // If the byte comes back as -1 (null), break out of the loop
      if (r == -1) {
        System.out.println("End of File");
        break;
      }
      // Set the byte to the proper position
      array[i] = (byte) r;
    }

    // Return the byte array
    return array;
  }

  /**
   * Reads off a single byte from the given input stream.
   * 
   * @param reader the input stream currently reading a file
   * @return a valid single byte, or -1 if no bytes are left to read
   * @throws IOException thrown if an error occurs reading the bytes
   */
  public static byte getSingleByte(InputStream reader) throws IOException {
    // Will return -1 if no bytes are left to read
    return (byte) reader.read();
  }

  /**
   * Converts a byte array to an unsigned INT32 (primitive long). Reads bytes big-endian.
   * 
   * @param data the byte array to convert
   * @return a primitive long
   */
  public static long getUnsignedInt(byte[] data) {
    ByteBuffer bb = ByteBuffer.wrap(data);
    bb.order(ByteOrder.BIG_ENDIAN);
    return bb.getInt() & 0xffffffffl;
  }

  /**
   * Converts a byte array to a signed {@link Double}. Reads bytes big-endian.
   * 
   * @param data the byte array to convert
   * @return a signed {@link Double}
   */
  public static Double getDouble(byte[] data) {
    ByteBuffer bb = ByteBuffer.wrap(data);
    bb.order(ByteOrder.BIG_ENDIAN);
    return new Double(bb.getDouble());
  }

  /**
   * Calculates a dollar amount from the given list of records, filter function, and mapping function. Outputs a message
   * from the given log string and the final calculated amount.
   * 
   * @param filter a function used to filter the record list
   * @param mapper a function used to map the record list into a calculable format
   * @param records a linked list of payment records
   * @param log a log string used in the output
   */
  public static void getAmountFromList(
      Predicate<? super RecordEntity> filter,
      ToDoubleFunction<? super RecordEntity> mapper,
      List<RecordEntity> records,
      String log) {
    // Apply the filter and mapper to the record list to get a valid, calculated amount as a Double
    Double amount = 
        (double) Math.round(
            records
              .stream()
              .filter(filter)       // Apply the filter
              .mapToDouble(mapper)  // Apply the mapper
              .sum()                // Sum the values
              * 100.00              // Multiply by 100
            ) / 100.00;             // Divide the rounded value by 100
    // Set the formatter to US currency
    NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
    // Output the valid log statement and calculated, formatted amount
    System.out.println(log + formatter.format(amount));
  }

  /**
   * Calculates a count of records from the given list of records and {@link RecordType} for comparison. Outputs a
   * message from the given log string and the final count.
   * 
   * @param compareType the {@link RecordType} to filter on
   * @param records a linked list of payment records
   * @param log a log string used in the output
   */
  public static void getCountFromList(RecordType compareType, List<RecordEntity> records, String log) {
    // Calculate the count of applicable payment records for the given record type
    int count = 
        records
          .stream()
          .filter(record -> record.getRecordType().equals(compareType))
          .collect(Collectors.toList())
          .size();
    // Output the valid log statement and calculated count
    System.out.println(log + count);
  }

}
