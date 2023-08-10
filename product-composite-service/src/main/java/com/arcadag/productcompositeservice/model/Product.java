package com.arcadag.productcompositeservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Product {
    private Long productId;
    private String name;
    private Integer weight;
    private String serviceAddress;


}
