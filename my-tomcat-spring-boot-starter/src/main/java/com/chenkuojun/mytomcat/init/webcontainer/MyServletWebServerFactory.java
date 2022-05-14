package com.chenkuojun.mytomcat.init.webcontainer;

import com.chenkuojun.mytomcat.init.context.MyServletContext;
import com.chenkuojun.mytomcat.startup.Bootstrap;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.server.AbstractServletWebServerFactory;

import javax.servlet.ServletException;

public class MyServletWebServerFactory extends AbstractServletWebServerFactory {
    Bootstrap bootStrap = new Bootstrap();

    @Override
    public WebServer getWebServer(ServletContextInitializer... initializers) {
        MyServletContext servletContext = new MyServletContext("",null, bootStrap);
        if(initializers != null){
            try {
                for (ServletContextInitializer initializer : initializers) {
                    initializer.onStartup(servletContext);
                }
            } catch (ServletException e) {
                e.printStackTrace();
            }
        }
        bootStrap.setServletContext(servletContext);
        return new MyWebServer(bootStrap);
    }
}
