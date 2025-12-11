package com.hpims.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Swagger配置类
 * 配置API文档信息
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("医院药房智能管理系统 API")
                        .version("1.0.0")
                        .description("医院药房智能管理系统后端API文档\n\n" +
                                "### 功能模块：\n" +
                                "- 用户认证与管理\n" +
                                "- 药品信息管理\n" +
                                "- 库存管理（入库/出库）\n" +
                                "- 处方管理\n" +
                                "- 处方审核（集成Python服务）\n" +
                                "- 审核记录管理\n\n" +
                                "### 认证方式：\n" +
                                "使用JWT Token进行认证，在请求头中添加：\n" +
                                "`Authorization: Bearer {token}`")
                        .contact(new Contact()
                                .name("开发团队")
                                .email("hpims@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(Arrays.asList(
                        new Server()
                                .url("http://localhost:8080/api")
                                .description("本地开发环境"),
                        new Server()
                                .url("https://api.example.com")
                                .description("生产环境")
                ));
    }
}

