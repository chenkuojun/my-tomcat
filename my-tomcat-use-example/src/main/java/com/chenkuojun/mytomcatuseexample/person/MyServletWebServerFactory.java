package com.chenkuojun.mytomcatuseexample.person;

import com.chenkuojun.mytomcat.startup.Bootstrap;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.server.AbstractServletWebServerFactory;

import javax.servlet.ServletException;

public class MyServletWebServerFactory extends AbstractServletWebServerFactory {
    Bootstrap bootStrap = new Bootstrap();

    @Override
    public WebServer getWebServer(ServletContextInitializer... initializers) {
        if(initializers != null){
            MyServletContext myServletContext = new MyServletContext("",null, bootStrap);
            try {
                for (ServletContextInitializer initializer : initializers) {
                    initializer.onStartup(myServletContext);
                }
            } catch (ServletException e) {
                e.printStackTrace();
            }
        }
        return new MyWebServer(bootStrap);
    }
}
