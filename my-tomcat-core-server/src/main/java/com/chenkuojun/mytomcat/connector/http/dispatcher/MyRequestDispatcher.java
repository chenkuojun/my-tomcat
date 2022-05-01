package com.chenkuojun.mytomcat.connector.http.dispatcher;

import org.springframework.util.Assert;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

public class MyRequestDispatcher implements RequestDispatcher {

    private final String resource;


    /**
     * Create a new CthulhuRequestDispatcher for the given resource.
     * @param resource the server resource to dispatch to, located at a
     * particular path or given by a particular name
     */
    public MyRequestDispatcher(String resource) {
        Assert.notNull(resource, "Resource must not be null");
        this.resource = resource;
    }

    @Override
    public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException {

    }

    @Override
    public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException {

    }
}
