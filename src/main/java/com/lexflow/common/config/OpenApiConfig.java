package com.lexflow.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Metadata for the generated OpenAPI spec (/v3/api-docs) and Swagger UI (/swagger-ui.html).
 *
 * The "basicAuth" scheme adds an "Authorize" button to Swagger UI, so a reviewer can log in
 * as user/user123 (read-only) or admin/admin123 (read and write) and try every endpoint.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "LexFlow API",
                version = "v1",
                description = "REST API for legal case management: cases, deadlines and notes."
        ),
        security = @SecurityRequirement(name = "basicAuth")
)
@SecurityScheme(name = "basicAuth", type = SecuritySchemeType.HTTP, scheme = "basic")
public class OpenApiConfig {
}
