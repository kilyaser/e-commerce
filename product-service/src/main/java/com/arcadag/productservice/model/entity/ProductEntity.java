package com.arcadag.productservice.model.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "products")
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ProductEntity {
    @Id
    private String id;
    @Version
    private Integer version;
    @Indexed(unique = true)
    private Long productId;
    private String name;
    private int weight;

    public ProductEntity(Long productId, String name, int weight) {
        this.productId = productId;
        this.name = name;
        this.weight = weight;
    }

}
