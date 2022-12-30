package com.punchin.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * API Documentation Configuration To access the API doc hit the URL
 * {{host}}:{{port}}/api/swagger-ui.html.
 *
 * @author Satyam Goel.
 */
@EnableSwagger2
@Configuration
public class SwaggerConfig {

    public static final String AUTH_TOKEN = "X-Xsrf-Token";
    public static final String PUNCHIN = "Punchin";

    final List<ResponseMessage> globalResponses = Arrays.asList(
            createResponse(200, "The request has succeeded."),
            createResponse(400, "Invalid resource description."),
            createResponse(401, "You are not authorized to perform this operation"),
            //createResponse(404, "The requested resource could not be found."),
            createResponse(500, "The server encountered an unexpected condition which prevented it from fulfilling the request.")
    );

    @SuppressWarnings("rawtypes")
    private ApiInfo apiInfo() {
        return new ApiInfo(PUNCHIN, "Backend-API", "2.0", "",
                "", "licence",
                "");
    }

    private ResponseMessage createResponse(int code, String message) {
        return new ResponseMessageBuilder()
                .code(code)
                .message(message)
                .build();
    }

    @Bean
    public Docket api() {
        return intialize(new Docket(DocumentationType.SWAGGER_2)
                .select().apis(RequestHandlerSelectors.basePackage("com.punchin.controllers"))
                .build());
    }

    public Docket intialize(Docket docket) {
        List<Parameter> aParameters = new ArrayList<>();
        ParameterBuilder aParameterBuilder = new ParameterBuilder();
        aParameterBuilder.name(AUTH_TOKEN).modelRef(new ModelRef("string")).parameterType("header").required(true).description("This is a token which is generated while login and required for each and every request")
                .build();
        aParameters.add(aParameterBuilder.build());
        return docket
                .apiInfo(apiInfo()).globalOperationParameters(aParameters).enable(true).useDefaultResponseMessages(false)
                .globalResponseMessage(RequestMethod.GET, globalResponses)
                .globalResponseMessage(RequestMethod.POST, globalResponses)
                .globalResponseMessage(RequestMethod.PUT, globalResponses)
                .globalResponseMessage(RequestMethod.DELETE, globalResponses)
                .globalResponseMessage(RequestMethod.PATCH, globalResponses);
    }
}
