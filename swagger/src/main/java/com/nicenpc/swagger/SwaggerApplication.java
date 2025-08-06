package com.nicenpc.swagger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class SwaggerApplication {

    public static void main(String[] args) throws IOException {
        // SpringApplication.run(SwaggerApplication.class, args);
        String[] converterArgs = {
                "src/main/resources/member-system-openapi.yaml",
                "RegisterRequest"
        };
        OpenApiToCsvConverter.main(converterArgs);

        String[] converterArgs2 = {
                "src/main/resources/member-system-openapi.yaml",
                "UserProfile"
        };
        OpenApiToCsvConverter.main(converterArgs2);
    }

}
