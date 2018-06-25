package com.n26.service;

import com.n26.service.StatisticService;
import com.n26.service.StatisticServiceImpl;
import com.n26.domain.Statistics;
import com.n26.domain.Transaction;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class StatisticServiceImplConcurrentTest {
    private StatisticService statisticService ;

    public StatisticServiceImplConcurrentTest() {
        statisticService =  new StatisticServiceImpl();
    }


    @Test
    public void testTransactionConcurrency(){
        DateTime current = new DateTime(DateTimeZone.UTC).withMillisOfSecond(0);
        List<Transaction> transactionList = new ArrayList<>();
        IntStream.range(0, 5)
                .forEach(i -> {
                       int change = i*10 + 5;
                       DateTime time = current.minusSeconds(change);
                       Transaction transaction =  new Transaction();
                       transaction.setTimestamp(time.getMillis());
                       transaction.setAmount(change/2d);
                       transactionList.add(transaction);

                });

        ExecutorService executor = Executors.newFixedThreadPool(2);

        IntStream.range(0, 5)
                .forEach(i -> {
                    Runnable task = () ->
                            statisticService.addTrasaction(transactionList.get(i));

                    executor.submit(task);
                });

        stop(executor);
        Statistics stats =  statisticService.getStatistics();
        System.out.println();

        assertEquals(new Double(stats.getSum().doubleValue()),new Double(62.5));
        assertEquals(stats.getAvg().get(),new Double(12.5));
        assertEquals(stats.getMin().get(),new Double(2.5));
        assertEquals(stats.getMax().get(),new Double(22.5));
        assertEquals(stats.getCount().intValue(),5);
    }

    @Test
    public void testTransactionConcurrency2(){
        DateTime current = new DateTime(DateTimeZone.UTC).withMillisOfSecond(0);
        List<Transaction> transactionList = new ArrayList<>();
        List<List<Transaction>> toplist = new ArrayList<>();

        IntStream.range(0, 5)
                .forEach(i -> {
                    int change = i*10 + 5;
                    DateTime time = current.minusSeconds(change);
                    Transaction transaction =  new Transaction();
                    transaction.setTimestamp(time.getMillis());
                    transaction.setAmount(change/2d);
                    transactionList.add(transaction);

                });

        IntStream.range(0, 5)
                .forEach(i -> {
                    List<Transaction> list =  new ArrayList<Transaction>(transactionList) ;
                    toplist.add(list);
                });

        ExecutorService executor = Executors.newFixedThreadPool(2);

        IntStream.range(0, 5)
                .forEach(i -> {
                    Runnable task = () ->{
                        toplist.get(i).forEach( transaction -> {
                            statisticService.addTrasaction(transaction);
                        });
                    };
                    executor.submit(task);
                });

        stop(executor);
        Statistics stats =  statisticService.getStatistics();
        System.out.println();

        assertEquals(new Double(stats.getSum().doubleValue()),new Double(375.0));
        assertEquals(stats.getAvg().get(),new Double(12.5));
        assertEquals(stats.getMin().get(),new Double(2.5));
        assertEquals(stats.getMax().get(),new Double(22.5));
        assertEquals(stats.getCount().intValue(),30);
    }


    public static void stop(ExecutorService executor) {
        try {
            executor.shutdown();
            executor.awaitTermination(60, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            System.err.println("termination interrupted");
        }
        finally {
            if (!executor.isTerminated()) {
                System.err.println("killing non-finished tasks");
            }
            executor.shutdownNow();
        }
    }

    public static void sleep(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

}
