package com.n26.domain;

import lombok.Builder;
import lombok.Data;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;

@Data
@Builder
public class Statistics {
    private DoubleAdder sum ;
    private AtomicReference<Double> avg ;
    private AtomicReference<Double> max ;
    private AtomicReference<Double> min ;
    private LongAdder count ;
}
