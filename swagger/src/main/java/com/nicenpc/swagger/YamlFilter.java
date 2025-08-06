package com.nicenpc.swagger;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

public class YamlFilter {

    private final Map<String, Object> data;

    public YamlFilter(String yamlFilePath) throws FileNotFoundException {
        InputStream inputStream = new FileInputStream(yamlFilePath);
        Yaml yaml = new Yaml();
        this.data = yaml.load(inputStream);
    }

    public Map<String, Object> filterByApi(String apiPath, String httpMethod) {
        if (data == null || !data.containsKey("paths")) {
            return null;
        }

        Map<String, Object> paths = (Map<String, Object>) data.get("paths");
        if (!paths.containsKey(apiPath)) {
            return null;
        }

        Map<String, Object> pathItem = (Map<String, Object>) paths.get(apiPath);
        String methodKey = httpMethod.toLowerCase();
        if (!pathItem.containsKey(methodKey)) {
            return null;
        }

        Object operation = pathItem.get(methodKey);

        // 建立新的過濾後的資料結構
        Map<String, Object> filteredData = new LinkedHashMap<>();
        // 複製 metadata
        filteredData.put("openapi", data.get("openapi"));
        filteredData.put("info", data.get("info"));
        if (data.containsKey("servers")) {
            filteredData.put("servers", data.get("servers"));
        }
        if (data.containsKey("components")) {
            filteredData.put("components", data.get("components")); // 為了確保 $ref 能正常運作，保留所有 components
        }


        // 建立只包含指定 API 的 paths
        Map<String, Object> newPathItem = new LinkedHashMap<>();
        newPathItem.put(methodKey, operation);

        Map<String, Object> newPaths = new LinkedHashMap<>();
        newPaths.put(apiPath, newPathItem);

        filteredData.put("paths", newPaths);

        return filteredData;
    }

    public String toYaml(Map<String, Object> filteredData) {
        if (filteredData == null) {
            return "";
        }
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(2);

        Yaml yaml = new Yaml(options);
        return yaml.dump(filteredData);
    }
}
