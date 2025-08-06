package com.nicenpc.swagger;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class YamlFilter {

    private final Map<String, Object> data;

    public YamlFilter(String yamlFilePath) throws FileNotFoundException {
        InputStream inputStream = new FileInputStream(yamlFilePath);
        LoaderOptions options = new LoaderOptions();
        options.setCodePointLimit(20 * 1024 * 1024); // 10MB
        Yaml yaml = new Yaml(options);
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

    /**
     * 根據多個 API 路徑與方法過濾，產生新的 YAML 結構
     * @param apiMethods 欲保留的 API 路徑與方法，格式為 Map<apiPath, List<httpMethod>>
     * @return 過濾後的 YAML 結構
     */
    public Map<String, Object> filterByApis(Map<String, List<String>> apiMethods) {
        if (data == null || !data.containsKey("paths")) {
            return null;
        }

        Map<String, Object> paths = (Map<String, Object>) data.get("paths");
        Map<String, Object> newPaths = new LinkedHashMap<>();

        for (Map.Entry<String, java.util.List<String>> entry : apiMethods.entrySet()) {
            String apiPath = entry.getKey();
            if (!paths.containsKey(apiPath)) continue;
            Map<String, Object> pathItem = (Map<String, Object>) paths.get(apiPath);

            Map<String, Object> newPathItem = new LinkedHashMap<>();
            for (String method : entry.getValue()) {
                String methodKey = method.toLowerCase();
                if (pathItem.containsKey(methodKey)) {
                    newPathItem.put(methodKey, pathItem.get(methodKey));
                }
            }
            if (!newPathItem.isEmpty()) {
                newPaths.put(apiPath, newPathItem);
            }
        }

        if (newPaths.isEmpty()) return null;

        Map<String, Object> filteredData = new LinkedHashMap<>();
        filteredData.put("openapi", data.get("openapi"));
        filteredData.put("info", data.get("info"));
        if (data.containsKey("servers")) {
            filteredData.put("servers", data.get("servers"));
        }
        if (data.containsKey("components")) {
            filteredData.put("components", data.get("components"));
        }
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

    /**
     * 將過濾後的 YAML 結構寫入指定檔案
     * @param filteredData 過濾後的 YAML 結構
     * @param outputFilePath 輸出檔案路徑
     * @throws IOException 寫檔失敗時拋出
     */
    public void writeFilteredYaml(Map<String, Object> filteredData, String outputFilePath) throws IOException {
        if (filteredData == null) return;
        String yamlStr = toYaml(filteredData);
        try (FileWriter writer = new FileWriter(outputFilePath)) {
            writer.write(yamlStr);
        }
    }
}
