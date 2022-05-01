package com.chenkuojun.mytomcat.connector.nettyhttp.container;

import com.chenkuojun.mytomcat.connector.nettyhttp.config.NettyContainerConfig;
import com.chenkuojun.mytomcat.connector.nettyhttp.context.NettyEmbeddedContext;
import io.netty.bootstrap.Bootstrap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.server.AbstractServletWebServerFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.util.ClassUtils;

import javax.servlet.ServletException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author chenkuojun
 */
@Slf4j
public class NettyEmbeddedServletContainerFactory extends AbstractServletWebServerFactory implements ResourceLoaderAware {

    private static final String SERVER_INFO = "my-tomcat(netty)";
    private ResourceLoader resourceLoader;
    private NettyContainerConfig nettyContainerConfig;

    public NettyEmbeddedServletContainerFactory(NettyContainerConfig nettyContainerConfig) {
        super();
        this.nettyContainerConfig = nettyContainerConfig;
    }

    /**
     * Set the ResourceLoader that this object runs in.
     * <p>This might be a ResourcePatternResolver, which can be checked
     * through {@code instanceof ResourcePatternResolver}. See also the
     * {@code ResourcePatternUtils.getResourcePatternResolver} method.
     * <p>Invoked after population of normal bean properties but before an init callback
     * like InitializingBean's {@code afterPropertiesSet} or a custom init-method.
     * Invoked before ApplicationContextAware's {@code setApplicationContext}.
     *
     * @param resourceLoader ResourceLoader object to be used by this object
     * @see ResourcePatternResolver
     * @see ResourcePatternUtils#getResourcePatternResolver
     */
    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
            this.resourceLoader = resourceLoader;
    }

    @Override
    public WebServer getWebServer(ServletContextInitializer... initializers) {
        ClassLoader parentClassLoader = resourceLoader != null ? resourceLoader.getClassLoader() : ClassUtils.getDefaultClassLoader();
        Package nettyPackage = Bootstrap.class.getPackage();
        String title = nettyPackage.getImplementationTitle();
        String version = nettyPackage.getImplementationVersion();
        log.info("Running with " + title + " netty version is " + version);
        NettyEmbeddedContext context = new NettyEmbeddedContext(nettyContainerConfig,getContextPath(),
                new URLClassLoader(new URL[]{}, parentClassLoader),
                SERVER_INFO);
        if (isRegisterDefaultServlet()) {
            log.warn("This container does not support a default servlet");
        }
        for (ServletContextInitializer initializer : initializers) {
            try {
                initializer.onStartup(context);
            } catch (ServletException e) {
                throw new RuntimeException(e);
            }
        }
        log.info("{}",nettyContainerConfig);
        return new NettyEmbeddedServletContainer(nettyContainerConfig, context);
    }
}
