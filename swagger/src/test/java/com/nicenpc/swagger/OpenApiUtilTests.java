package com.nicenpc.swagger;

import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class OpenApiUtilTests {

    private OpenApiUtil openApiUtil;

    @BeforeEach
    void setUp() {
        openApiUtil = new OpenApiUtil("src/main/resources/member-system-openapi.yaml");
    }

    @Test
    void testGetRequestSchema() {
        Schema schema = openApiUtil.getRequestSchema("/auth/register", PathItem.HttpMethod.POST);
        assertNotNull(schema);
        assertEquals("#/components/schemas/RegisterRequest", schema.get$ref());
    }

    @Test
    void testGetResponseSchema() {
        Schema schema = openApiUtil.getResponseSchema("/auth/login", PathItem.HttpMethod.POST, "200");
        assertNotNull(schema);
        assertEquals("#/components/schemas/LoginResponse", schema.get$ref());
    }
}
