package com.n26.controller;

import com.n26.domain.Statistics;
import com.n26.domain.Transaction;
import com.n26.service.StatisticService;
import com.n26.util.JSONUtil;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.BDDMockito.given;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.CoreMatchers.is;



@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class StatisticsControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private StatisticService service;

    @Test
    public void getStatistics() throws Exception{
        given(service.getStatistics()).willReturn(Statistics.builder().build());

        mvc.perform(get("/statistics")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("min")));
    }

    @Test
    public void setTransactionsSuccessfulAdded() throws Exception{
        Transaction transaction =  new Transaction();
        given(service.addTrasaction(Mockito.any(Transaction.class))).willReturn(Boolean.TRUE);
        mvc.perform(post("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONUtil.toJson(transaction)))
                .andExpect(status().is(201));

    }

    @Test
    public void setTransactionsSkipped() throws Exception{
        Transaction transaction =  new Transaction();
        given(service.addTrasaction(Mockito.any(Transaction.class))).willReturn(Boolean.FALSE);
        mvc.perform(post("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONUtil.toJson(transaction)))
                .andExpect(status().is(204));

    }
}
