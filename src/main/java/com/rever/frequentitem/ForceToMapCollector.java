package com.rever.frequentitem;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class ForceToMapCollector<T, K, V> implements Collector<T, Map<K, V>, Map<K, V>> {
    @Override
    public Supplier<Map<K, V>> supplier() {
        return HashMap::new;
    }

    @Override
    public BiConsumer<Map<K, V>, T> accumulator() {
        return null;
    }

    @Override
    public BinaryOperator<Map<K, V>> combiner() {
        return null;
    }

    @Override
    public Function finisher() {
        return null;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return null;
    }
}
