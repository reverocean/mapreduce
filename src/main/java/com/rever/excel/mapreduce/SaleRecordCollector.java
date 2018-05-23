package com.rever.excel.mapreduce;

import com.rever.excel.mapreduce.beans.AggregateResult;
import com.rever.excel.mapreduce.beans.SaleRecord;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class SaleRecordCollector implements Collector<SaleRecord, AggregateResult, AggregateResult> {
    @Override
    public Supplier<AggregateResult> supplier() {
        return AggregateResult::new;
    }

    @Override
    public BiConsumer<AggregateResult, SaleRecord> accumulator() {
        return (aggregateResult, saleRecord) -> aggregateResult.append(saleRecord);
    }

    @Override
    public BinaryOperator<AggregateResult> combiner() {
        return AggregateResult::combine;
    }

    @Override
    public Function<AggregateResult, AggregateResult> finisher() {
        return Function.identity();
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Collections.unmodifiableSet(EnumSet.of(Characteristics.CONCURRENT,Characteristics.IDENTITY_FINISH));
    }
}
