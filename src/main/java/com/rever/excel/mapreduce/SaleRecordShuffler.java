package com.rever.excel.mapreduce;

import com.rever.excel.mapreduce.beans.AggregateResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.IntStream;

public class SaleRecordShuffler {
    private ConcurrentHashMap<String, ConcurrentLinkedQueue<AggregateResult>> aggregates = new ConcurrentHashMap<>();

    public void doShuffle(Map<String, AggregateResult> countryAggregate) {
        countryAggregate.keySet().stream().forEach(country -> {
            AggregateResult aggregateResult = countryAggregate.get(country);
            ConcurrentLinkedQueue<AggregateResult> sameCountryResults = aggregates.get(country);
            if (!aggregates.containsKey(country)) {
                sameCountryResults = new ConcurrentLinkedQueue<>();
                aggregates.put(country, sameCountryResults);
            }
            sameCountryResults.add(aggregateResult);
        });
    }

    public List<Map<String, ConcurrentLinkedQueue<AggregateResult>>> doSplit(int reducerCount) {
        int sizeofReducer = getSizeofReducer(reducerCount);

        List<Map<String, ConcurrentLinkedQueue<AggregateResult>>> reducerDatas = new ArrayList<>(reducerCount);
        Map<String, ConcurrentLinkedQueue<AggregateResult>> reducerData = new HashMap<>();
        reducerDatas.add(reducerData);
        for (String country : aggregates.keySet()) {
            if (reducerData.size() == sizeofReducer) {
                reducerData = new HashMap<>();
                reducerDatas.add(reducerData);
            }
            reducerData.put(country, aggregates.get(country));
        }
        return reducerDatas;
    }

    private int getSizeofReducer(int reducerCount) {
        return (int)Math.ceil((double) aggregates.size() / reducerCount);
    }
}
