package com.rever.excel.mapreduce.beans;

public class SaleRecord {
    private String country;
    private double unitCost;

    public SaleRecord(String country, double unitCost) {
        this.country = country;
        this.unitCost = unitCost;
    }

    public String getCountry() {
        return country;
    }

    public double getUnitCost() {
        return unitCost;
    }
}
