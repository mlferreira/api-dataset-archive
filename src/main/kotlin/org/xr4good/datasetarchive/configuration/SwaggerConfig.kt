package org.xr4good.datasetarchive.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.service.ApiKey
import springfox.documentation.service.AuthorizationScope
import springfox.documentation.service.SecurityReference
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spi.service.contexts.SecurityContext
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2


@Configuration
@EnableSwagger2
//@Profile("!production")
class SwaggerConfig {

    @Bean("Swagger")
    fun api(): Docket {
        return Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("org.xr4good.datasetarchive"))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(metaData())
                .useDefaultResponseMessages(false)
                .securitySchemes(defaultAuth())
                .securityContexts(securityContext())
    }

    private fun metaData(): ApiInfo {
        return ApiInfoBuilder()
                .title("Dataset Archive API")
                .license("")
                .build()
    }

    private fun securityContext(): List<SecurityContext> {
        val authorizationScopes = listOf(SecurityReference("Bearer Token", arrayOf(AuthorizationScope("global", "accessEverything"))))
        return listOf(
                SecurityContext.builder()
                        .securityReferences(authorizationScopes)
                        .forPaths(PathSelectors.regex("^(?!/api/user/login).*")) // excluir o endpoint de login
                        .forPaths(PathSelectors.regex("^(?!/api/user/signup).*")) // excluir o endpoint de signup
                        .build()
        )
    }

    private fun defaultAuth(): List<ApiKey> {
        return listOf(ApiKey("Bearer Token", "Authorization", "header"))
    }


}