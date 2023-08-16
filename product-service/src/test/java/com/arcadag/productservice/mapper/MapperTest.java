package com.arcadag.productservice.mapper;

import com.arcadag.productservice.model.Product;
import com.arcadag.productservice.model.entity.ProductEntity;
import com.arcadag.productservice.service.ProductMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class MapperTest {

    private ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

    @Test
    void mapperTest() {
        assertNotNull(mapper);

        Product api = new Product(1L, "n", 1, "sa");

        ProductEntity entity = mapper.apiToEntity(api);

        assertEquals(api.getProductId(), entity.getProductId());
        assertEquals(api.getProductId(), entity.getProductId());
        assertEquals(api.getName(), entity.getName());
        assertEquals(api.getWeight(), entity.getWeight());

        Product api2 = mapper.entityToApi(entity);

        assertEquals(api.getProductId(), api2.getProductId());
        assertEquals(api.getProductId(), api2.getProductId());
        assertEquals(api.getName(),      api2.getName());
        assertEquals(api.getWeight(),    api2.getWeight());
        assertNull(api2.getServiceAddress());
    }

}
