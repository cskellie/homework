package com.parser;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.opencsv.bean.CsvBindByName;

/**
 * A POJO to hold a record from the given .csv file mapping zipcodes to rates. Annotated to be mapped automatically.
 * This object will then be mapped back to a .csv file.
 *
 */
public class SlcspEntity {
	
	@CsvBindByName(required = true)
	private String zipcode;
	@CsvBindByName
	private Double rate;

	public SlcspEntity() {
	}

	public String getZipcode() {
		return zipcode;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}

	public Double getRate() {
		return rate;
	}

	public void setRate(Double rate) {
		this.rate = rate;
	}

	@Override
	public String toString() {
		return "SlcspEntity [zipcode=" + zipcode + ", rate=" + rate + "]";
	}
	
	/**
	 * Gets this class's field names as required headers.
	 * 
	 * @return an array list of string containing the required headers
	 */
	public static List<String> requiredHeaders() {
		return Arrays.asList(SlcspEntity.class.getDeclaredFields())
				.stream()
				.map(field -> field.getName())
				.collect(Collectors.toList());
	}
}
