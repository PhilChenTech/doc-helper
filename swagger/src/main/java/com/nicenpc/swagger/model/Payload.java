package com.nicenpc.swagger.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Payload {

  private List<Field> fields;

  @Data
  @Builder
  public static class Field {
    private String name;
    private String type;
    private String description;
    private Object example;
    private boolean required;
  }
}
