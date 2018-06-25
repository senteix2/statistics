package com.n26.domain;

import lombok.Builder;
import lombok.Data;

@Data
public class Transaction {
    private Double amount ;
    private Long timestamp;
}
