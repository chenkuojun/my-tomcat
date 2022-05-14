package com.chenkuojun.mytomcat.init.config;

import com.chenkuojun.mytomcat.init.webcontainer.MyServletWebServerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.web.embedded.undertow.UndertowBuilderCustomizer;
import org.springframework.boot.web.embedded.undertow.UndertowDeploymentInfoCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConditionalOnBean(ConfigMarker.class)
public class MyAutoConfiguration {
    static {
        log.info("my-tomcat is init....，please wait ......");
    }

    /**
     * 启动 my-tomcat
     * @param deploymentInfoCustomizers
     * @param builderCustomizers
     * @return
     */
    @Bean
    MyServletWebServerFactory myServletWebServerFactory(
            ObjectProvider<UndertowDeploymentInfoCustomizer> deploymentInfoCustomizers,
            ObjectProvider<UndertowBuilderCustomizer> builderCustomizers) {
        return new MyServletWebServerFactory();
    }
}
