package com.parser;

import com.opencsv.bean.CsvBindByName;

/**
 * A POJO to hold a record from the plans.csv file. Annotated to be mapped automatically.
 *
 */
public class PlanEntity {
	
	@CsvBindByName
	private String plan_id;
	@CsvBindByName(required = true)
	private String state;
	@CsvBindByName(required = true)
	private String metal_level;
	@CsvBindByName(required = true)
	private double rate;
	@CsvBindByName(required = true)
	private int rate_area;

	public PlanEntity() { }

	public String getPlan_id() {
		return plan_id;
	}

	public void setPlan_id(String plan_id) {
		this.plan_id = plan_id;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getMetal_level() {
		return metal_level;
	}

	public void setMetal_level(String metal_level) {
		this.metal_level = metal_level;
	}

	public double getRate() {
		return rate;
	}

	public void setRate(double rate) {
		this.rate = rate;
	}

	public int getRate_area() {
		return rate_area;
	}

	public void setRate_area(int rate_area) {
		this.rate_area = rate_area;
	}

	@Override
	public String toString() {
		return "PlanEntity [plan_id=" + plan_id + ", state=" + state + ", metal_level=" + metal_level + ", rate=" + rate
				+ ", rate_area=" + rate_area + "]";
	}
	
	/**
	 * @return the state - rateArea combination string
	 */
	public String getStateRateArea() {
		return getState() + "-" + getRate_area();
	}
	
	

}
