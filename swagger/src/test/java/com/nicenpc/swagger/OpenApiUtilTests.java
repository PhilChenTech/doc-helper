package com.nicenpc.swagger;

import com.nicenpc.swagger.model.Payload;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class OpenApiUtilTests {

    private OpenApiUtil openApiUtil;

    @BeforeEach
    void setUp() {
        openApiUtil = new OpenApiUtil("src/main/resources/5level.yaml");
    }

    @Test
    void test() {
        Schema schema = openApiUtil.getResponseSchema("/user/profile", PathItem.HttpMethod.GET,"200");
    System.out.println(schema);
//        assertNotNull(schema);
    }


    @Test
    void testGetRequestSchema() {
        Schema schema = openApiUtil.getRequestSchema("/auth/register", PathItem.HttpMethod.POST);
        assertNotNull(schema);
        assertNotNull(schema.getProperties().get("username"));
        assertNotNull(schema.getProperties().get("email"));
        assertNotNull(schema.getProperties().get("password"));
    }

    @Test
    void testGetResponseSchema() {
        Schema schema = openApiUtil.getResponseSchema("/auth/login", PathItem.HttpMethod.POST, "200");
        assertNotNull(schema);
        assertNotNull(schema.getProperties().get("token"));
    }
}
