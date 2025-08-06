
package com.nicenpc.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OpenApiToCsvConverter {

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: java OpenApiToCsvConverter <path-to-openapi-yaml> <schema-name>");
            return;
        }

        String openApiFilePath = args[0];
        String schemaName = args[1];

        OpenAPI openAPI = new OpenAPIV3Parser().read(openApiFilePath);
        if (openAPI == null) {
            System.err.println("Error parsing OpenAPI file: " + openApiFilePath);
            return;
        }

        Schema schema = openAPI.getComponents().getSchemas().get(schemaName);
        if (schema == null) {
            System.err.println("Schema not found: " + schemaName);
            return;
        }

        List<String[]> csvData = new ArrayList<>();
        csvData.add(new String[]{"序號", "結構", "欄位", "名稱", "長度", "資料型態", "必要欄位 (勾選V)", "說明"});
        
        parseSchema(csvData, schema, schemaName, "1", new Counter());

        String outputCsvFile = schemaName + ".csv";
        try (FileWriter out = new FileWriter(outputCsvFile);
             CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withHeader("序號", "結構", "欄位", "名稱", "長度", "資料型態", "必要欄位 (勾選V)", "說明"))) {
            for (String[] record : csvData) {
                printer.printRecord(record);
            }
        }

        System.out.println("Successfully converted " + schemaName + " to " + outputCsvFile);
    }

    private static void parseSchema(List<String[]> csvData, Schema schema, String fieldName, String parentStructure, Counter counter) {
        String type = schema.getType();
        String description = schema.getDescription() != null ? schema.getDescription() : "";
        if (schema.getFormat() != null) {
            description = "format: " + schema.getFormat() + (description.isEmpty() ? "" : ", " + description);
        }
        if (schema.getExample() != null) {
            description = "example: " + schema.getExample() + (description.isEmpty() ? "" : ", " + description);
        }


        csvData.add(new String[]{
                String.valueOf(counter.increment()),
                parentStructure,
                fieldName,
                "", // Name
                "", // Length
                type,
                schema.getRequired() != null && schema.getRequired().contains(fieldName) ? "V" : "",
                description
        });

        if ("object".equals(type)) {
            if (schema.getProperties() != null) {
                int subCounter = 1;
                for (Map.Entry<String, Schema> entry : ((Map<String, Schema>) schema.getProperties()).entrySet()) {
                    parseSchema(csvData, entry.getValue(), entry.getKey(), parentStructure + "." + subCounter, counter);
                    subCounter++;
                }
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
