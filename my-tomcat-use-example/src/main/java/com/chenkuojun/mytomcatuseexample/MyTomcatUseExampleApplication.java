package com.chenkuojun.mytomcatuseexample;

import com.chenkuojun.mytomcat.connector.http.webcontainer.MyServletWebServerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.undertow.UndertowBuilderCustomizer;
import org.springframework.boot.web.embedded.undertow.UndertowDeploymentInfoCustomizer;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MyTomcatUseExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyTomcatUseExampleApplication.class, args);
    }

    //@Bean
    //public NettyEmbeddedServletContainerFactory servletContainer(){
    //    NettyContainerConfig nettyContainerConfig = NettyContainerConfig.builder().build();
    //    NettyEmbeddedServletContainerFactory factory = new NettyEmbeddedServletContainerFactory(nettyContainerConfig);
    //    return factory;
    //}

    @Bean
    MyServletWebServerFactory myServletWebServerFactory(
            ObjectProvider<UndertowDeploymentInfoCustomizer> deploymentInfoCustomizers,
            ObjectProvider<UndertowBuilderCustomizer> builderCustomizers) {
        return new MyServletWebServerFactory();
    }

}
