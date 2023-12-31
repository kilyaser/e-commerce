package com.arcadag.recommendationservice.model.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "recommendations")
@CompoundIndex(name = "prod-rec-id", unique = true, def = "{'product': 1, 'recommendationId': 1}")
@NoArgsConstructor
@Getter
@Setter
public class RecommendationEntity {
    @Id
    private String id;
    @Version
    private Integer version;
    private Long productId;
    private Long recommendationId;
    private String author;
    private int rating;
    private String content;

    public RecommendationEntity(Long productId, Long recommendationId, String author, int rating, String content) {
        this.productId = productId;
        this.recommendationId = recommendationId;
        this.author = author;
        this.rating = rating;
        this.content = content;
    }
}
