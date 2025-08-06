package com.nicenpc.swagger;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.nicenpc.swagger.model.Payload;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SchemaConverterTests {

    private OpenApiUtil openApiUtil;
    private SchemaConverter schemaConverter;

    @BeforeEach
    void setUp() {
        openApiUtil = new OpenApiUtil("src/main/resources/member-system-openapi.yaml");
        schemaConverter = new SchemaConverter(openApiUtil.getOpenAPI());
    }

    @Test
    void testToPayloadFromRegisterRequest() {
        Schema schema = openApiUtil.getRequestSchema("/auth/register", PathItem.HttpMethod.POST);
        Payload payload = schemaConverter.toPayload(schema);

    System.out.println("payload = " +payload);
        assertNotNull(payload);
        assertEquals(3, payload.getFields().size());

        Payload.Field usernameField = payload.getFields().stream()
                .filter(f -> f.getName().equals("username"))
                .findFirst().orElse(null);

        assertNotNull(usernameField);
        assertEquals("使用者名稱", usernameField.getDescription());
        assertEquals("string", usernameField.getType());
        assertEquals("johndoe", usernameField.getExample());
        assertTrue(usernameField.isRequired());
    }

    @Test
    void testToPayloadFromLoginResponse() {
        Schema schema = openApiUtil.getResponseSchema("/auth/login", PathItem.HttpMethod.POST, "200");
        Payload payload = schemaConverter.toPayload(schema);

        assertNotNull(payload);
        assertEquals(1, payload.getFields().size());

        Payload.Field tokenField = payload.getFields().get(0);

        assertEquals("token", tokenField.getName());
        assertEquals("JWT token", tokenField.getDescription());
        assertEquals("string", tokenField.getType());
        assertFalse(tokenField.isRequired());
    }
}
