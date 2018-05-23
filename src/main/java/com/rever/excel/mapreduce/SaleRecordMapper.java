package com.rever.excel.mapreduce;

import com.rever.excel.mapreduce.beans.AggregateResult;
import com.rever.excel.mapreduce.beans.SaleRecord;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SaleRecordMapper implements Runnable {
    private SaleRecordShuffler shuffler;
    private final List<SaleRecord> saleRecords;
    private final CountDownLatch countDownLatch;

    public SaleRecordMapper(SaleRecordShuffler shuffler, List<SaleRecord> saleRecords, CountDownLatch countDownLatch) {
        this.shuffler = shuffler;
        this.saleRecords = saleRecords;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {

        Map<String, AggregateResult> countryAggregate = saleRecords.stream()
                .collect(Collectors.groupingBy(SaleRecord::getCountry, new SaleRecordCollector()));

        countDownLatch.countDown();
        shuffler.doShuffle(countryAggregate);
    }
}
