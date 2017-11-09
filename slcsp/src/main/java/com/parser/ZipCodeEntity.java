package com.parser;

import com.opencsv.bean.CsvBindByName;

/**
 * A POJO to hold a record from the zips.csv file. Annotated to be mapped automatically.
 *
 */
public class ZipCodeEntity {
	
	@CsvBindByName(required = true)
	private String zipcode;
	@CsvBindByName(required = true)
	private String state;
	@CsvBindByName
	private String county_code;
	@CsvBindByName
	private String name;
	@CsvBindByName(required = true)
	private int rate_area;
	
	public ZipCodeEntity() {
		
	}

	public String getZipcode() {
		return zipcode;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getCounty_code() {
		return county_code;
	}

	public void setCounty_code(String county_code) {
		this.county_code = county_code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getRate_area() {
		return rate_area;
	}

	public void setRate_area(int rate_area) {
		this.rate_area = rate_area;
	}

	@Override
	public String toString() {
		return "ZipCodeEntity [zipcode=" + zipcode + ", state=" + state + ", county_code=" + county_code + ", name="
				+ name + ", rate_area=" + rate_area + "]";
	}
	
	/**
	 * @return the state - rateArea combination string
	 */
	public String getStateRateArea() {
		return getState() + "-" + getRate_area();
	}

}
