package cy.ac.ouc.cognition.nlrg.service;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;


@Configuration
public class SpringDocConfig implements WebMvcConfigurer {

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("NESTOR-public")
                .packagesToScan("cy.ac.ouc.cognition.nlrg.service")
                .pathsToMatch("/*")
                .build();
    }


    @Bean
    public OpenAPI nlrgServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("NESTOR Service")
                .description("NESTOR Service API Info")
                .version("v1.0.0")
                .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                .externalDocs(new ExternalDocumentation()
                .description("NESTOR Documentation")
                .url("https://ttooulis.github.io/nestor/docs.html"));
    }

}