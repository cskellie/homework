package com.parser;

import java.math.BigInteger;
import java.sql.Timestamp;

import com.parser.ParserHelper.RecordType;

/**
 * A POJO holding the pieces of a payment record:
 * <ul>
 *    <li><b>Record Type : </b>The type of payment record:
 *    <ul>
 *       <li>Debit
 *       <li>Credit
 *       <li>StartAutopay
 *       <li>EndAutopay
 *    </ul>
 *    <li><b>Timestamp : </b>The timestamp on the payment record
 *    <li><b>User ID : </b>The user ID attached to the payment record
 *    <li><b>Amount : </b>The amount of the payment record; only filled in for CREDIT or DEBIT record types
 * </ul>
 */
public class RecordEntity implements Comparable<RecordEntity> {

  private RecordType recordType;
  private Timestamp timestamp;
  private BigInteger userId;
  private Double amount;

  /**
   * Get the record type.
   * 
   * @return the record type as a {@link RecordType}
   */
  public RecordType getRecordType() {
    return recordType;
  }

  /**
   * Sets the record type from a single byte.
   * 
   * @param recordIndicator a byte to be parsed into a record type
   */
  public void setRecordType(byte recordIndicator) {
    this.recordType = RecordType.getRecordTypeFromRecordIndicator(recordIndicator);
  }

  /**
   * Get the timestamp.
   * 
   * @return the timestamp as a {@link Timestamp}
   */
  public Timestamp getTimestamp() {
    return timestamp;
  }

  /**
   * Sets the timestamp from a byte array; set to 4 bytes.
   * 
   * @param array a byte array to be parsed into a {@link Timestamp}
   */
  public void setTimestamp(byte[] array) {
    this.timestamp = new Timestamp(ParserHelper.getUnsignedInt(array) * 1000);
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
   * Sets the user ID from a byte array; set to 8 bytes.
   * 
   * @param array a byte array to be parsed into a {@link BigInteger}
   */
  public void setUserId(byte[] array) {
    this.userId = new BigInteger(1, array);
  }

  /**
   * Get the amount.
   * 
   * @return the amount as a {@link Double}
   */
  public Double getAmount() {
    return amount;
  }

  /**
   * Sets the amount from a byte array; set to 8 bytes.
   * 
   * @param array a byte array to be parsed into a {@link Double}
   */
  public void setAmount(byte[] array) {
    this.amount = ParserHelper.getDouble(array);
  }

  @Override
  public String toString() {
    return "RecordEntity [recordType=" + recordType + ", timestamp=" + timestamp + ", userId=" + userId + ", amount="
        + amount + "]";
  }

  /**
   * Compares the records by timestamp.
   * 
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(RecordEntity o) {
    Timestamp otherRecord = o.getTimestamp();
    Timestamp thisRecord = this.getTimestamp();
    return thisRecord.compareTo(otherRecord);
  }

}
