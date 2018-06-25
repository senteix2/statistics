package com.n26.controller;

import com.n26.domain.Statistics;
import com.n26.domain.Transaction;
import com.n26.service.StatisticService;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController()
public class StatisticsController  {
    private StatisticService statisticService ;

    @Autowired
    public void setStatisticService(StatisticService statisticService) {
        this.statisticService = statisticService;
    }

    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "success"),
            @ApiResponse(code = 204, message = "transaction is older than 60 seconds")
    })
    @PostMapping("/transactions")
    public ResponseEntity<Void> postTransaction(@RequestBody Transaction transaction) {
        log.debug( "transaction:", transaction );
        Boolean response = statisticService.addTrasaction(transaction);
        HttpStatus status = response?HttpStatus.CREATED:HttpStatus.NO_CONTENT;
        return new ResponseEntity<Void>(status);
    }
    @GetMapping("/statistics")
    public ResponseEntity<Statistics> getStatistics() {
        Statistics statistics = statisticService.getStatistics();
        return ResponseEntity.ok(statistics);
    }


}
