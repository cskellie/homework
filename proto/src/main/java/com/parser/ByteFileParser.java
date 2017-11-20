package com.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.parser.ParserHelper.RecordType;

/**
 * The main entry point for an application that takes a file encoded as bytes and parses and reads the unencoded values
 * as a transaction log of payment records. This application then calculates the following metrics:
 * <ul>
 *    <li>Total amount in DEBITS
 *    <li>Total amount in CREDITS
 *    <li>Total number of autopays STARTED
 *    <li>Total number of autopays ENDED
 *    <li>Total balance of a user ID, assuming a $0.00 balance at the beginning of the given record list
 * </ul>
 * <p>
 * The results are seen as output on the command line.
 */
public class ByteFileParser {
  /**
   * The user ID to calculate a balance for.
   */
  private BigInteger userId;

  /**
   * Parses and sets the user ID.
   * 
   * @param userId a string containing the user ID as an integer
   * @throws NumberFormatException thrown if the given string cannot be parsed to an integer
   */
  public void setUserId(String userId) throws NumberFormatException {
    this.userId = new BigInteger(userId);
  }

  /**
   * Get the user ID.
   * 
   * @return the user ID as a {@link BigInteger}
   */
  public BigInteger getUserId() {
    return userId;
  }

  /**
   * Main application entry point. Requires the following:
   * <ul>
   *    <li><b>MPS7 file : </b>The location of the file to be processed. Must have read access to the file.
   *    <li><b>User ID : </b>The ID of a user. Must be an Integer.
   * </ul>
   * 
   * @param args the arguments used within the application
   */
  public static void main(String[] args) {
    // Must provide 2 arguments for the application to run
    if (args.length != 2) {
      System.err.println("Please provide the MPS7 file and the user ID to search for, in that order.");
      return;
    }

    try {
      ByteFileParser parser = new ByteFileParser();
      // Set the user ID and file
      parser.setUserId(args[1]);
      File file = new File(args[0]);
      // Process the file
      parser.processFile(file);

    } catch (NumberFormatException e) {
      System.err.println("The given user ID is not a valid integer.");
      e.printStackTrace();
    }
  }

  /**
   * Processes the given file from the following structure:
   * <ul>
   *    <li><b>Header : </b>1 set of bytes
   *    <li><b>Record : </b>1+ set of bytes
   * </ul>
   * 
   * @param file the file to process for records
   */
  private void processFile(File file) {
    try {
      InputStream in = new FileInputStream(file);
      List<RecordEntity> recordList = new LinkedList<>();

      // Read the header
      readHeader(in);

      // While the record is valid, continue processing
      while (true) {
        if (readRow(in, recordList) == -1) {
          break;
        }
      }

      in.close();
      // Calculate the metrics
      calculateMetrics(recordList);
    } catch (FileNotFoundException e) {
      System.err.println("Error occurred finding the file.");
      e.printStackTrace();
    } catch (IOException e) {
      System.err.println("Error occurred accessing or reading a file.");
      e.printStackTrace();
    }
  }

  /**
   * Read off the header bytes according to the specification:
   * <ul>
   *    <li><b>Magic String : </b>4 bytes
   *    <li><b>Version : </b>1 byte
   *    <li><b>Record Count : </b>4 bytes, unsigned INT32
   * </ul>
   * 
   * @param reader the input stream currently reading the file
   * @throws IOException thrown if an error occurs reading the bytes
   */
  private void readHeader(InputStream reader) throws IOException {
    // Read the Magic String
    ParserHelper.getArray(reader, 4);
    // Read the version
    ParserHelper.getSingleByte(reader);
    // Read the record count
    ParserHelper.getArray(reader, 4);
  }

  /**
   * Read off a "row" of bytes according to the specification:
   * <ul>
   *    <li><b>Record Type : </b>1 byte
   *    <li><b>Timestamp : </b>4 bytes, unsigned INT32
   *    <li><b>User ID : </b>8 bytes, unsigned INT64
   * </ul>
   * <p>
   * If the record type is CREDIT or DEBIT, read off an additional field:
   * <ul>
   *    <li><b>Amount : </b>8 bytes, FLOAT64
   * </ul>
   * 
   * @param reader the input stream currently reading the file
   * @param recordList a linked list of RecordEntity POJOs
   * @return the record type indicator integer, or -1
   * @throws IOException thrown if an error occurs reading the bytes
   */
  private int readRow(InputStream reader, List<RecordEntity> recordList) throws IOException {
    RecordEntity record = new RecordEntity();

    // Read the record type
    record.setRecordType(ParserHelper.getSingleByte(reader));
    // If no more records exist or an incorrect record type is decoded, return -1
    if (record.getRecordType() == null) {
      return -1;
    }

    // Read the timestamp and user ID
    record.setTimestamp(ParserHelper.getArray(reader, 4));
    record.setUserId(ParserHelper.getArray(reader, 8));

    // If the record type has an amount attached to it, read the amount
    if (record.getRecordType().isHasAmount()) {
      record.setAmount(ParserHelper.getArray(reader, 8));
    }

    // Add the record to the list and return the record type indicator integer
    recordList.add(record);
    return record.getRecordType().getRecordIndicator();
  }

  /**
   * Calculate the following metrics for the full record list:
   * <ul>
   *    <li>Total amount in DEBITS
   *    <li>Total amount in CREDITS
   *    <li>Total number of autopays STARTED
   *    <li>Total number of autopays ENDED
   *    <li>Total balance of a user ID, assuming a $0.00 balance at the beginning of the given record list
   * </ul>
   * 
   * @param records a linked list of RecordEntity POJOs holding record information
   */
  private void calculateMetrics(List<RecordEntity> records) {
    // Sort the records according to the timestamp ascending
    Collections.sort(records);

    // Calculate the total amount in DEBITS
    ParserHelper.getAmountFromList(record -> record.getRecordType().equals(RecordType.DEBIT),
        record -> record.getAmount(), records, "Total DEBITS : ");

    // Calculate the total amount in CREDITS
    ParserHelper.getAmountFromList(record -> record.getRecordType().equals(RecordType.CREDIT),
        record -> record.getAmount(), records, "Total CREDITS : ");

    // Calculate the total number of autopays STARTED
    ParserHelper.getCountFromList(RecordType.START_AUTO, records, "Total autopays STARTED : ");

    // Calculate the total number of autopays ENDED
    ParserHelper.getCountFromList(RecordType.END_AUTO, records, "Total autopays ENDED : ");

    // Calculate the balance for a user ID, assuming a $0.00 balance at the beginning of the given record list
    ParserHelper.getAmountFromList(record -> record.getUserId().equals(getUserId()),
        record -> record.getRecordType().equals(RecordType.DEBIT) ? record.getAmount() * -1 : record.getAmount(),
        records, "Total for USER " + getUserId() + " : ");
  }

}
