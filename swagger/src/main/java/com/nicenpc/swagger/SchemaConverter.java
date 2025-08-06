package com.nicenpc.swagger;

import com.nicenpc.swagger.model.Payload;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class SchemaConverter {

  private final OpenAPI openAPI;

  public SchemaConverter(OpenAPI openAPI) {
    this.openAPI = openAPI;
  }

  public Payload toPayload(Schema schema) {
    if (schema.get$ref() != null) {
      schema = getRefSchema(schema.get$ref());
    }

    List<Payload.Field> fields = new ArrayList<>();
    for (Map.Entry<String, Schema> entry :
        (Iterable<Map.Entry<String, Schema>>) schema.getProperties().entrySet()) {
      String name = entry.getKey();
      Schema propertySchema = entry.getValue();

      Payload.Field field =
          Payload.Field.builder()
              .name(name)
              .type(propertySchema.getType())
              .description(propertySchema.getDescription())
              .example(propertySchema.getExample())
              .required(schema.getRequired() != null && schema.getRequired().contains(name))
              .build();

      fields.add(field);
    }

    return Payload.builder().fields(fields).build();
  }

  private Schema getRefSchema(String ref) {
    String[] parts = ref.split("/");
    String schemaName = parts[parts.length - 1];
    return openAPI.getComponents().getSchemas().get(schemaName);
  }
}
