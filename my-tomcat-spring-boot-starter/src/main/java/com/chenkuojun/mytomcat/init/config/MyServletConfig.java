package com.chenkuojun.mytomcat.init.config;

import com.chenkuojun.mytomcat.init.context.MyServletContext;

import javax.servlet.*;
import java.util.Enumeration;

/**
 * @author wangguangwu
 */
public class MyServletConfig implements Servlet, ServletConfig, java.io.Serializable {

    private final MyServletContext context;

    public MyServletConfig(MyServletContext context) {
        this.context = context;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        this.init();
    }

    public void init() throws ServletException {

    }

    @Override
    public ServletConfig getServletConfig() {
        return this;
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) {

    }

    @Override
    public String getServletInfo() {
        return null;
    }

    @Override
    public void destroy() {

    }

    @Override
    public String getServletName() {
        return "my-tomcat";
    }

    @Override
    public ServletContext getServletContext() {
        return context;
    }

    @Override
    public String getInitParameter(String name) {
        return null;
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return new Enumeration<>() {
            @Override
            public boolean hasMoreElements() {
                return false;
            }

            @Override
            public String nextElement() {
                return null;
            }
        };
    }
}
