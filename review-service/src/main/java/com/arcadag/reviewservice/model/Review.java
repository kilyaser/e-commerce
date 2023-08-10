package com.arcadag.reviewservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Review {
    private Long productId;
    private Long reviewId;
    private String author;
    private String subject;
    private String content;
    private String serviceAddress;

    public Review() {
        productId = 0L;
        reviewId = 0L;
        author = null;
        subject = null;
        content = null;
        serviceAddress = null;
    }
}
