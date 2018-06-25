package com.n26.service;

import com.n26.domain.Statistics;
import com.n26.domain.Transaction;

public interface StatisticService {

    void cleanOldStatistics();

    Statistics getStatistics();

    Boolean addTrasaction(Transaction transaction);
}
