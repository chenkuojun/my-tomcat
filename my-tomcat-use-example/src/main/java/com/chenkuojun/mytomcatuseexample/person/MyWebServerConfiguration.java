package com.chenkuojun.mytomcatuseexample.person;

import com.chenkuojun.mytomcat.connector.nettyhttp.config.NettyContainerConfig;
import com.chenkuojun.mytomcat.connector.nettyhttp.container.NettyEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class MyWebServerConfiguration {
    //@Bean
    //MyServletWebServerFactory myServletWebServerFactory(
    //        ObjectProvider<UndertowDeploymentInfoCustomizer> deploymentInfoCustomizers,
    //        ObjectProvider<UndertowBuilderCustomizer> builderCustomizers) {
    //    return new MyServletWebServerFactory();
    //}

    @Bean
    public NettyEmbeddedServletContainerFactory servletContainer(){
        NettyContainerConfig nettyContainerConfig = NettyContainerConfig.builder().build();
        NettyEmbeddedServletContainerFactory factory = new NettyEmbeddedServletContainerFactory(nettyContainerConfig);
        return factory;
    }
}
