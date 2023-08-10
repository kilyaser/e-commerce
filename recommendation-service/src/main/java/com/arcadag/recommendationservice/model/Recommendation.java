package com.arcadag.recommendationservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Recommendation {
    private Long productId;
    private Long recommendationId;
    private String author;
    private int rate;
    private String content;
    private String serviceAddress;

    public Recommendation() {
        productId = 0L;
        recommendationId = 0L;
        author = null;
        rate = 0;
        content = null;
        serviceAddress = null;
    }
}
