package com.chenkuojun.mytomcat.init.registration;


import com.chenkuojun.mytomcat.init.context.MyServletContext;

import javax.servlet.FilterConfig;
import javax.servlet.Registration;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;


/**
 * @author chenkuojun
 */
public abstract class AbstractRegistration implements Registration, Registration.Dynamic, ServletConfig, FilterConfig {

    private final String               name;
    private final String               className;
    private final MyServletContext context;
    protected boolean                  asyncSupported;

    protected AbstractRegistration(String name, String className, MyServletContext context) {
        this.name = checkNotNull(name);
        this.className = checkNotNull(className);
        this.context = checkNotNull(context);
    }

    @Override
    public void setAsyncSupported(boolean isAsyncSupported) {
        asyncSupported = isAsyncSupported;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public boolean setInitParameter(String name, String value) {
        checkArgument(name != null, "name may not be null");
        checkArgument(value != null, "value may not be null");
        return false;
    }

    @Override
    public String getFilterName() {
        return name;
    }

    @Override
    public String getServletName() {
        return name;
    }

    @Override
    public ServletContext getServletContext() {
        return context;
    }

    protected MyServletContext getNettyContext() {
        return context;
    }

    @Override
    public String getInitParameter(String name) {
        return null;
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.emptyEnumeration();
    }

    @Override
    public Set<String> setInitParameters(Map<String, String> initParameters) {
        return Collections.emptySet();
    }

    @Override
    public Map<String, String> getInitParameters() {
        return Collections.emptyMap();
    }
}
