package com.nicenpc.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.core.util.Yaml;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OpenApiGenerator {

    // Target size in bytes (10 MB)
    private static final long TARGET_SIZE_BYTES = 4 * 1024 * 1024;

    public static void main(String[] args) {
        String outputPath = "openapi-generated-10mb.yaml";
        try {
            generate(outputPath, TARGET_SIZE_BYTES);
            System.out.println("Successfully generated " + outputPath + " with a size of approximately " + (TARGET_SIZE_BYTES / (1024 * 1024)) + " MB.");
        } catch (IOException e) {
            System.err.println("Error generating OpenAPI YAML file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void generate(String outputPath, long targetSize) throws IOException {
        OpenAPI openAPI = new OpenAPI();

        // Info
        Info info = new Info()
                .title("Large Generated OpenAPI")
                .version("1.0.0")
                .description("An auto-generated large OpenAPI specification for testing purposes.");
        openAPI.setInfo(info);

        // Servers
        Server server = new Server().url("http://api.example.com/v1");
        openAPI.setServers(Arrays.asList(server));

        // Paths and Components
        Map<String, PathItem> paths = new HashMap<>();
        Map<String, Schema> schemas = new HashMap<>();

        // Initial serialization to get a baseline size
        String initialYaml = Yaml.pretty().writeValueAsString(openAPI);
        long currentSize = initialYaml.getBytes(StandardCharsets.UTF_8).length;

        int i = 0;
        while (currentSize < targetSize) {
            // Create and add path
            String pathKey = "/resource/" + i;
            PathItem pathItem = createPathItem(i);
            paths.put(pathKey, pathItem);

            // Create and add schema
            String schemaName = "GeneratedModel" + i;
            Schema schema = createSchema(i);
            schemas.put(schemaName, schema);

            io.swagger.v3.oas.models.Paths openApiPaths = new io.swagger.v3.oas.models.Paths();
            openApiPaths.putAll(paths);
            openAPI.setPaths(openApiPaths);
            openAPI.setComponents(new io.swagger.v3.oas.models.Components().schemas(schemas));

            currentSize = Yaml.pretty().writeValueAsString(openAPI).getBytes(StandardCharsets.UTF_8).length;
            i++;
        }

        // Write the content to the file
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputPath), StandardCharsets.UTF_8)) {
            writer.write(Yaml.pretty().writeValueAsString(openAPI));
        }
    }

    private static PathItem createPathItem(int index) {
        Operation operation = new Operation()
                .summary("Get resource " + index)
                .description("Retrieve details for resource " + index + ". This is a generated endpoint to increase file size.")
                .addTagsItem("Generated Resources")
                .operationId("getResource" + index);

        Parameter parameter = new Parameter()
                .name("id")
                .in("path")
                .required(true)
                .schema(new Schema<>().type("integer").format("int64"));
        operation.addParametersItem(parameter);

        ApiResponse apiResponse = new ApiResponse()
                .description("Successful operation")
                .content(new Content().addMediaType("application/json", new MediaType().schema(new Schema<>().$ref("#/components/schemas/GeneratedModel" + index))));
        ApiResponses apiResponses = new ApiResponses().addApiResponse("200", apiResponse);
        operation.setResponses(apiResponses);

        return new PathItem().get(operation);
    }

    private static Schema createSchema(int index) {
        String uuid = UUID.randomUUID().toString();
        Schema<Object> schema = new Schema<>();
        schema.type("object");

        Map<String, Schema> properties = new HashMap<>();
        properties.put("id", new Schema<>().type("integer").format("int64").example(index));
        properties.put("name", new Schema<>().type("string").example("Resource Name " + index));
        properties.put("uuid", new Schema<>().type("string").format("uuid").example(uuid));
        properties.put("description", new Schema<>().type("string").example("This is a long description for the model to add more content. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed non risus."));
        properties.put("createdAt", new Schema<>().type("string").format("date-time"));
        schema.properties(properties);

        return schema;
    }
}