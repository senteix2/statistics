package com.n26.service;

import com.n26.domain.Statistics;
import com.n26.domain.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;

@Slf4j
@Service
public class StatisticServiceImpl implements StatisticService{

    private volatile static ConcurrentHashMap<Long,Statistics>  data = new ConcurrentHashMap() ;
    private static final int LIMIT_SECONDS = 60 ;
    private static final int REFRESH_SECONDS = 1 ;

    public ConcurrentHashMap<Long,Statistics> getData(){
        return data;
    }


    @Override
    @Scheduled(fixedRate = REFRESH_SECONDS*1000)
    public void cleanOldStatistics(){
        DateTime current = new DateTime(DateTimeZone.UTC).withMillisOfSecond(0);
        log.info(String.format("cleaning time: %s   ---- %d  ",current,current.getMillis()  )); //3600000
        DateTime limit = new DateTime(DateTimeZone.UTC);
        limit.withMillisOfSecond(0);
        limit.minusSeconds(LIMIT_SECONDS+5);
        data.forEach((key,value)-> {
               if(key<limit.getMillis()){
                   data.remove(key);
               }
        });

    }

    @Override
    public Statistics getStatistics() {
        DateTime current = (new DateTime(DateTimeZone.UTC)).withMillisOfSecond(0);
        DateTime limit =current.minusSeconds(LIMIT_SECONDS);
        Statistics result = getNewStatistic();

        data.forEach((key,value) -> {
                    if(key.doubleValue()<=current.getMillis()
                            && key.doubleValue()>=limit.getMillis()){
                        result.getCount().add(value.getCount().longValue());

                        result.getSum().add(value.getSum().doubleValue());

                        result.getAvg().set(result.getSum().doubleValue()/result.getCount().doubleValue());

                        if(value.getMin().get()<result.getMin().get()){
                            result.getMin().set(value.getMin().get());
                        }
                        if(value.getMax().get()>result.getMax().get()){
                            result.getMax().set(value.getMax().get());
                        }
                    }
        });
        result.getCount().doubleValue();
        result.getSum().doubleValue();
        result.getAvg().get();
        result.getCount().doubleValue();
        result.getMin().get();
        result.getMax().get();
        return result;
    }

    @Override
    public Boolean addTrasaction(Transaction transaction) {
        DateTime current = new DateTime(DateTimeZone.UTC).withMillisOfSecond(0);
        DateTime currenttransaction =  new DateTime(transaction.getTimestamp(),DateTimeZone.UTC).withMillisOfSecond(0);

        long diff =  current.getMillis() - currenttransaction.getMillis();

        //no future date is accepted so transaction is no added
        if(currenttransaction.getMillis()>current.getMillis()){
            return false;
        }
        //if diff is most than the limit stated then no add the trasaction
        if(diff>(LIMIT_SECONDS*1000)){
            return false;
        }

        data.putIfAbsent(currenttransaction.getMillis(),getNewStatistic());
        Statistics secondStatistic =data.get(currenttransaction.getMillis());
        secondStatistic.getCount().increment();
        secondStatistic.getSum().add(transaction.getAmount());
        secondStatistic.getMin().getAndUpdate( x -> transaction.getAmount()<x ? transaction.getAmount() : x);
        secondStatistic.getMax().getAndUpdate( x -> transaction.getAmount()>x ? transaction.getAmount() : x);
        Double avg = secondStatistic.getSum().doubleValue()/secondStatistic.getCount().longValue();
        secondStatistic.getAvg().set(avg);
        return true;
    }

    public Statistics getNewStatistic(){
        Statistics result = Statistics.builder()
                .avg(new AtomicReference<Double>())
                .sum(new DoubleAdder())
                .min(new AtomicReference<Double>())
                .max(new AtomicReference<Double>())
                .count(new LongAdder())
                .build();
        result.getAvg().set(0d);
        result.getMin().set(Double.MAX_VALUE);
        result.getMax().set(Double.MIN_VALUE);
        return  result;
    }




}
