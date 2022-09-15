package com.github.davidfantasy.mybatisplus.generatorui;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.*;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import java.util.Map;


/**
 * Do not use @SpringBootApplication and @EnableAutoConfiguration
 * Annotation to avoid being interfered by the automatic configuration of the host system at startup, directly inject the required configuration class
 */
@SpringBootConfiguration
@Import({
        DispatcherServletAutoConfiguration.class,
        ServletWebServerFactoryAutoConfiguration.class,
        HttpEncodingAutoConfiguration.class,
        HttpMessageConvertersAutoConfiguration.class,
        MultipartAutoConfiguration.class,
        ErrorMvcAutoConfiguration.class,
        WebMvcAutoConfiguration.class})
@ComponentScan("com.github.davidfantasy.mybatisplus.generatorui")
@Slf4j
public class MybatisPlusToolsApplication {

    private static GeneratorConfig generatorConfig;

    public static void run(GeneratorConfig generatorConfig) {
        if (Strings.isNullOrEmpty(generatorConfig.getJdbcUrl())) {
            throw new IllegalArgumentException("jdbcUrl必须要设置");
        }
        MybatisPlusToolsApplication.generatorConfig = generatorConfig;
        Map<String, Object> props = Maps.newHashMap();
        props.put("spring.resources.static-locations", "classpath:/generator-ui/");
        new SpringApplicationBuilder()
                .properties(props)
                .sources(MybatisPlusToolsApplication.class)
                .run(new String[0]);
    }

    @Bean
    public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> containerConfig(GeneratorConfig config) {
        return factory -> {
            if (config.getPort() != null) {
                factory.setPort(MybatisPlusToolsApplication.generatorConfig.getPort());
            } else {
                factory.setPort(8080);
            }
            factory.setContextPath("");
        };
    }

    @Bean
    public GeneratorConfig generatorConfig() {
        return MybatisPlusToolsApplication.generatorConfig;
    }


}

