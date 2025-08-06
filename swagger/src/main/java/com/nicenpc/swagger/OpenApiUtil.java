package com.nicenpc.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.OpenAPIV3Parser;

import java.util.Map;

public class OpenApiUtil {

    private final OpenAPI openAPI;

    public OpenApiUtil(String yamlFilePath) {
        System.setProperty("maxYamlAliasesForCollections", "200");
        System.setProperty("maxYamlDepth", "4000");
      System.setProperty("maxYamlCodePoints", 1024*1024*20 + ""); // 10MB

        this.openAPI = new OpenAPIV3Parser().read(yamlFilePath);
    }

    public OpenAPI getOpenAPI() {
        return openAPI;
    }

    public Schema getRequestSchema(String apiPath, PathItem.HttpMethod httpMethod) {
        PathItem pathItem = openAPI.getPaths().get(apiPath);
        if (pathItem == null) {
            return null;
        }

        Operation operation = getOperation(pathItem, httpMethod);
        if (operation == null || operation.getRequestBody() == null) {
            return null;
        }

        Schema schema = operation.getRequestBody().getContent().values().iterator().next().getSchema();
        return resolveSchema(schema);
    }

    public Schema getResponseSchema(String apiPath, PathItem.HttpMethod httpMethod, String statusCode) {
        PathItem pathItem = openAPI.getPaths().get(apiPath);
        if (pathItem == null) {
            return null;
        }

        Operation operation = getOperation(pathItem, httpMethod);
        if (operation == null || operation.getResponses() == null || operation.getResponses().get(statusCode) == null) {
            return null;
        }

        Schema schema = operation.getResponses().get(statusCode).getContent().values().iterator().next().getSchema();
        return resolveSchema(schema);
    }

    private Schema resolveSchema(Schema schema) {
        if (schema != null && schema.get$ref() != null) {
            String ref = schema.get$ref();
            String[] parts = ref.split("/");
            String schemaName = parts[parts.length - 1];
            return openAPI.getComponents().getSchemas().get(schemaName);
        }
        return schema;
    }

    private Operation getOperation(PathItem pathItem, PathItem.HttpMethod httpMethod) {
        switch (httpMethod) {
            case POST:
                return pathItem.getPost();
            case GET:
                return pathItem.getGet();
            case PUT:
                return pathItem.getPut();
            case DELETE:
                return pathItem.getDelete();
            default:
                return null;
        }
    }
}
