package com.chenkuojun.mytomcatuseexample.person;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.web.embedded.undertow.UndertowBuilderCustomizer;
import org.springframework.boot.web.embedded.undertow.UndertowDeploymentInfoCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class MyWebServerConfiguration {
    @Bean
    MyServletWebServerFactory myServletWebServerFactory(
            ObjectProvider<UndertowDeploymentInfoCustomizer> deploymentInfoCustomizers,
            ObjectProvider<UndertowBuilderCustomizer> builderCustomizers) {
        return new MyServletWebServerFactory();
    }
}
