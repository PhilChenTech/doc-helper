package com.nicenpc.swagger;

import com.nicenpc.swagger.tool.YamlFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class YamlFilterTests {

  private YamlFilter yamlFilter;
  private final String yamlFilePath = "src/main/resources/openapi-generated-10mb.yaml";

  @BeforeEach
  void setUp() throws FileNotFoundException {
    yamlFilter = new YamlFilter(yamlFilePath);
  }

  @Test
  void print() throws IOException {
    // Arrange
    String apiPath = "/resource/0";
    String httpMethod = "get";

    // Act
    Map<String, Object> filteredData = yamlFilter.filterByApi(apiPath, httpMethod);
    yamlFilter.writeFilteredYaml(filteredData,"data.yaml");
  }

  @Test
  void testFilterByApi_Success() {
    // Arrange
    String apiPath = "/user/profile";
    String httpMethod = "get";

    // Act
    Map<String, Object> filteredData = yamlFilter.filterByApi(apiPath, httpMethod);
    String resultYaml = yamlFilter.toYaml(filteredData);
    System.out.println(resultYaml);

    // Assert
    assertNotNull(
        filteredData, "Filtered data should not be null for a valid API path and method.");
    assertFalse(resultYaml.isEmpty(), "Resulting YAML should not be empty.");

    // 驗證只保留了指定的 API
    assertTrue(
        resultYaml.contains("  /user/profile:"), "The YAML should contain the specified API path.");
    assertTrue(
        resultYaml.contains("    get:"), "The YAML should contain the specified HTTP method.");

    // 驗證其他 API 已被移除
    assertFalse(resultYaml.contains("/auth/register:"), "Other API paths should be removed.");
    assertFalse(resultYaml.contains("/auth/login:"), "Other API paths should be removed.");

    // 驗證同一路徑下的其他方法已被移除
    assertFalse(
        resultYaml.contains("    put:"),
        "Other HTTP methods under the same path should be removed.");
    assertFalse(
        resultYaml.contains("    delete:"),
        "Other HTTP methods under the same path should be removed.");

    // 驗證 components 等頂層元素仍然存在
    assertTrue(resultYaml.contains("components:"), "Components should be preserved.");
    assertTrue(resultYaml.contains("  schemas:"), "Schemas should be preserved.");
  }

  @Test
  void testFilterByApi_PathNotFound() {
    // Arrange
    String nonExistentPath = "/non/existent/path";
    String httpMethod = "get";

    // Act
    Map<String, Object> filteredData = yamlFilter.filterByApi(nonExistentPath, httpMethod);

    // Assert
    assertNull(filteredData, "Should return null when the API path is not found.");
  }

  @Test
  void testFilterByApi_MethodNotFound() {
    // Arrange
    String apiPath = "/auth/register"; // 此路徑只有 POST
    String nonExistentMethod = "get";

    // Act
    Map<String, Object> filteredData = yamlFilter.filterByApi(apiPath, nonExistentMethod);

    // Assert
    assertNull(
        filteredData, "Should return null when the HTTP method is not found for the given path.");
  }

  @Test
  void testToYaml_withNullData() {
    // Arrange
    Map<String, Object> nullData = null;

    // Act
    String resultYaml = yamlFilter.toYaml(nullData);

    // Assert
    assertEquals("", resultYaml, "toYaml should return an empty string for null input.");
  }
}
