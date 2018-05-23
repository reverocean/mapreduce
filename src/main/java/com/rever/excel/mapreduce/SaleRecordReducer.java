package com.rever.excel.mapreduce;

import com.rever.commons.Pair;
import com.rever.excel.mapreduce.beans.AggregateResult;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class SaleRecordReducer implements Callable<List<Pair<String, AggregateResult>>> {
    private Map<String, ConcurrentLinkedQueue<AggregateResult>> reducerData;

    public SaleRecordReducer(Map<String, ConcurrentLinkedQueue<AggregateResult>> reducerData) {
        this.reducerData = reducerData;
    }

    @Override
    public List<Pair<String, AggregateResult>> call() throws Exception {
         return reducerData.keySet().stream().map(country ->{
            ConcurrentLinkedQueue<AggregateResult> aggregateResults = reducerData.get(country);
            AggregateResult aggregateResult = aggregateResults.stream().reduce(new AggregateResult(), AggregateResult::combine);

            return new Pair<>(country, aggregateResult);
        }).collect(Collectors.toList());
    }
}
