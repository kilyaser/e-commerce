package com.arcadag.productservice.config;

import com.arcadag.productservice.event.Event;
import com.arcadag.productservice.exception.EventProcessingException;
import com.arcadag.productservice.model.Product;
import com.arcadag.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;
@Slf4j
@Configuration
@RequiredArgsConstructor
public class MessageProcessorConfig {

    private final ProductService productService;

    @Bean
    public Consumer<Event<Long, Product>> messageProcessor() {

        return event -> {
            switch (event.getEventType()) {

                case CREATE -> {
                    Product product = event.getData();
                    productService.createProduct(product).block();
                }
                case DELETE -> {
                    Long productId = event.getKey();
                    productService.deleteProduct(productId).block();
                }
                default -> {
                    String errorMessage = "Incorrect event type: " + event.getEventType()
                            + ", expected a CREATE or DELETE event";
                    throw new EventProcessingException(errorMessage);
                }
            }
            log.info("Message processing done!");
        };

    }
}
