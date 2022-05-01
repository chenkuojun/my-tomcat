package com.chenkuojun.mytomcat.connector.http.webcontainer;

import com.chenkuojun.mytomcat.startup.Bootstrap;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.server.WebServerException;

public class MyWebServer implements WebServer {
    private final Bootstrap bootStrap;

    public MyWebServer(Bootstrap bootStrap) {
        this.bootStrap = bootStrap;
    }

    @Override
    public void start() throws WebServerException {
        try {
            bootStrap.start();
        } catch (Exception e) {
            throw new WebServerException("Failed to start web server", e);
        }
    }

    @Override
    public void stop() throws WebServerException {

    }

    @Override
    public int getPort() {
        return 0;
    }
}
