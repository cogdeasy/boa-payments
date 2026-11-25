package com.northbridge.payments.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket paymentsApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(new ApiInfoBuilder()
                        .title("Northbridge Payments Hub")
                        .description("Bill pay, P2P transfers and payee management")
                        .version("v1")
                        .build())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.northbridge.payments.web"))
                .paths(PathSelectors.ant("/api/v1/payments/**"))
                .build();
    }
}
