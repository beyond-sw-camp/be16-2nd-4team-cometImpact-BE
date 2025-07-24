package com.beyond.jellyorder.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;

public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("cometImpact_jellyorder")
                        .version("1.0.0")
                        .description("CometImpact JellyOrder API Documents"));
    }
}
