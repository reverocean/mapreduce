package com.rever.excel.mapreduce.beans;

public class AggregateResult {
    private int count;
    private double summing;

    public AggregateResult() {
    }

    public AggregateResult(int count, double summing) {
        this.count = count;
        this.summing = summing;
    }

    public void append(SaleRecord saleRecord) {
        count++;
        summing += saleRecord.getUnitCost();
    }

    public AggregateResult combine(AggregateResult aggregateResult) {
        return new AggregateResult(this.count + aggregateResult.count, this.summing + aggregateResult.summing);
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setSumming(double summing) {
        this.summing = summing;
    }


    public int getCount() {
        return count;
    }

    public double getSumming() {
        return summing;
    }

    public double getAvg() {
        return summing / count;
    }

    @Override
    public String toString() {
        return "AggregateResult{" +
                "count=" + count +
                ", summing=" + summing +
                ", avg=" + getAvg() +
                '}';
    }
}
