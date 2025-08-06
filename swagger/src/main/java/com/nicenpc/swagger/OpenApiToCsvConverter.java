package com.nicenpc.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OpenApiToCsvConverter {
//
//  public static void convertRequest(Operation operation) throws IOException {
//    // Handle Request Body
//    RequestBody requestBody = operation.getRequestBody();
//    if (requestBody != null && requestBody.get$ref() != null) {
//      // Handle reference to request body
//      String requestBodyName =
//              requestBody.get$ref().substring("#/components/requestBodies/".length());
//      requestBody = openAPI.getComponents().getRequestBodies().get(requestBodyName);
//    }
//
//    if (requestBody != null && requestBody.getContent() != null) {
//      Schema requestSchema = getSchemaFromContent(requestBody.getContent());
//      if (requestSchema != null && requestSchema.get$ref() != null) {
//        String schemaName = getSchemaNameFromRef(requestSchema.get$ref());
//        Schema actualSchema = openAPI.getComponents().getSchemas().get(schemaName);
//        generateCsvForSchema(actualSchema, schemaName + "Request");
//      }
//    }
//
//    // Handle Response Body
//    ApiResponses responses = operation.getResponses();
//    ApiResponse apiResponse = responses.get("200"); // Prefer 200
//    if (apiResponse == null) {
//      apiResponse = responses.get("201"); // Fallback to 201
//    }
//
//    if (apiResponse != null && apiResponse.getContent() != null) {
//      Schema responseSchema = getSchemaFromContent(apiResponse.getContent());
//      if (responseSchema != null && responseSchema.get$ref() != null) {
//        String schemaName = getSchemaNameFromRef(responseSchema.get$ref());
//        Schema actualSchema = openAPI.getComponents().getSchemas().get(schemaName);
//        generateCsvForSchema(actualSchema, schemaName + "Response");
//      }
//    }
//  }

  public static void convertRequest(Operation operation,OpenAPI openAPI) throws IOException {
    // Handle Request Body
    RequestBody requestBody = operation.getRequestBody();
    if (requestBody != null && requestBody.get$ref() != null) {
      // Handle reference to request body
      String requestBodyName =
          requestBody.get$ref().substring("#/components/requestBodies/".length());
      requestBody = openAPI.getComponents().getRequestBodies().get(requestBodyName);
    }

    if (requestBody != null && requestBody.getContent() != null) {
      Schema requestSchema = getSchemaFromContent(requestBody.getContent());
      if (requestSchema != null && requestSchema.get$ref() != null) {
        String schemaName = getSchemaNameFromRef(requestSchema.get$ref());
        Schema actualSchema = openAPI.getComponents().getSchemas().get(schemaName);
        generateCsvForSchema(actualSchema, schemaName + "Request");
      }
    }
  }

  private static Operation getOperation(PathItem pathItem, String method) {
    switch (method) {
      case "POST":
        return pathItem.getPost();
      case "GET":
        return pathItem.getGet();
      case "PUT":
        return pathItem.getPut();
      case "DELETE":
        return pathItem.getDelete();
      default:
        return null;
    }
  }

  private static Schema getSchemaFromContent(Content content) {
    MediaType mediaType = content.get("application/json");
    return mediaType != null ? mediaType.getSchema() : null;
  }

  private static String getSchemaNameFromRef(String ref) {
    if (ref != null && ref.startsWith("#/components/schemas/")) {
      return ref.substring("#/components/schemas/".length());
    }
    return ref;
  }

  private static void generateCsvForSchema(Schema schema, String fileName) throws IOException {
    if (schema == null) {
      return;
    }
    List<String[]> csvData = new ArrayList<>();
    parseSchema(csvData, schema, fileName, "1", new Counter(), null);

    String outputCsvFile = fileName + ".csv";
    try (FileWriter out = new FileWriter(outputCsvFile);
        CSVPrinter printer =
            new CSVPrinter(
                out,
                CSVFormat.DEFAULT.withHeader(
                    "序號", "結構", "欄位", "名稱", "長度", "資料型態", "必要欄位 (勾選V)", "說明"))) {
      printer.printRecords(csvData);
    }
    System.out.println("Successfully converted " + fileName + " to " + outputCsvFile);
  }

  private static void parseSchema(
      List<String[]> csvData,
      Schema schema,
      String fieldName,
      String parentStructure,
      Counter counter,
      Schema parentSchema) {
    String type = schema.getType();
    String description = schema.getDescription() != null ? schema.getDescription() : "";
    if (schema.getFormat() != null) {
      description += (description.isEmpty() ? "" : ", ") + "format: " + schema.getFormat();
    }
    if (schema.getExample() != null) {
      description += (description.isEmpty() ? "" : ", ") + "example: " + schema.getExample();
    }

    boolean isRequired =
        parentSchema != null
            && parentSchema.getRequired() != null
            && parentSchema.getRequired().contains(fieldName);

    csvData.add(
        new String[] {
          String.valueOf(counter.increment()),
          parentStructure,
          fieldName,
          "", // Name
          "", // Length
          type,
          isRequired ? "V" : "",
          description
        });

    if ("object".equals(type) && schema.getProperties() != null) {
      int subCounter = 1;
      for (Map.Entry<String, Schema> entry :
          ((Map<String, Schema>) schema.getProperties()).entrySet()) {
        parseSchema(
            csvData,
            entry.getValue(),
            entry.getKey(),
            parentStructure + "." + subCounter,
            counter,
            schema);
        subCounter++;
      }
    }
  }

  static class Counter {
    private int value = 0;

    public int increment() {
      return ++value;
    }
  }
}
