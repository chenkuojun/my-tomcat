package com.chenkuojun.mytomcat.init.context;

import com.chenkuojun.mytomcat.connector.nettyhttp.utils.Utils;
import com.chenkuojun.mytomcat.init.dispatcher.MyRequestDispatcher;
import com.chenkuojun.mytomcat.init.dispatcher.SimpleFilterChain;
import com.chenkuojun.mytomcat.init.registration.MyFilterRegistration;
import com.chenkuojun.mytomcat.init.registration.MyServletRegistration;
import com.chenkuojun.mytomcat.startup.Bootstrap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.web.util.WebUtils;
import javax.servlet.*;
import javax.servlet.descriptor.JspConfigDescriptor;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.Collectors.toList;

@Slf4j
public class MyServletContext implements ServletContext {
    private final Map<String, String> initParameters = new LinkedHashMap<>();

    private final Map<String, Object> attributes = new LinkedHashMap<>();

    private final Map<String, MyServletRegistration> servlets = Maps.newConcurrentMap();
    private final Map<String, String> servletMappings =  Maps.newConcurrentMap();
    private final Map<String, MyFilterRegistration> filters =  Maps.newConcurrentMap();

    private static final String COMMON_DEFAULT_SERVLET_NAME = "default";

    private String defaultServletName = COMMON_DEFAULT_SERVLET_NAME;

    private ResourceLoader resourceLoader;

    private String resourceBasePath;

    private String contextPath = "";

    private static final String TEMP_DIR_SYSTEM_PROPERTY = "java.io.tmpdir";

    public Bootstrap bootStrap;

    private volatile boolean initialised;

    /**
     * 构造函数
     * @param resourceBasePath resourceBasePath
     * @param resourceLoader resourceLoader
     * @param bootStrap bootStrap
     */
    public MyServletContext(String resourceBasePath, @Nullable ResourceLoader resourceLoader, Bootstrap bootStrap) {
        this.resourceLoader = (resourceLoader != null ? resourceLoader : new DefaultResourceLoader());
        this.resourceBasePath = resourceBasePath;
        this.bootStrap = bootStrap;
        String tempDir = System.getProperty(TEMP_DIR_SYSTEM_PROPERTY);
        if (tempDir != null) {
            this.attributes.put(WebUtils.TEMP_DIR_CONTEXT_ATTRIBUTE, new File(tempDir));
        }
    }

    @Override
    public String getContextPath() {
        return this.contextPath;
    }

    public void setInitialised(boolean initialised) {
        this.initialised = initialised;
    }

    public boolean isInitialised() {
        return initialised;
    }

    public void checkNotInitialised() {
        checkState(!isInitialised(), "This method may not be called after the context has been initialised");
    }

    public void addServletMapping(String urlPattern, String name) {
        checkNotInitialised();
        servletMappings.put(urlPattern, checkNotNull(name));
    }

    public void addFilterMapping(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String urlPattern) {
        checkNotInitialised();
        // TODO
    }

    @Override
    public ServletContext getContext(String uripath) {
        return this;
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(new LinkedHashSet<>(this.attributes.keySet()));
    }

    @Override
    public int getMajorVersion() {
        return 0;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public int getEffectiveMajorVersion() {
        return 0;
    }

    @Override
    public int getEffectiveMinorVersion() {
        return 0;
    }

    @Override
    public String getMimeType(String file) {
        return Utils.getMimeType(file);
    }

    @Override
    public Set<String> getResourcePaths(String path) {
        return null;
    }

    @Override
    public URL getResource(String path) {
        return null;
    }

    @Override
    public InputStream getResourceAsStream(String path) {
        return null;
    }

    public String getMatchingUrlPattern(String uri) {
        int indx = uri.indexOf('?');

        String path = indx != -1 ? uri.substring(0, indx) : uri.substring(0);
        String _path = path;
        if (!path.endsWith("/")) _path += "/";

        for (Map.Entry<String, String> entry : servletMappings.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            Pattern pattern = Pattern.compile(key);
            if (pattern.matcher(_path).matches()) {
                String sanitizedUrlPattern = key.replaceAll("\\*", ".*");
                if (sanitizedUrlPattern.endsWith("/")) {
                    sanitizedUrlPattern = sanitizedUrlPattern.substring(0, sanitizedUrlPattern.length() - 1);
                }
                return sanitizedUrlPattern;
            }
        }
        return path;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        String servletName = servletMappings.get(path);
        if (servletName == null) {
            servletName = servletMappings.get("/");
        }
        Servlet servlet;
        try {
            servlet = null == servletName ? null : servlets.get(servletName).getServlet();
            if (servlet == null) {
                return null;
            }
            final List<Filter> filters = this.filters.values().stream()
                    .map(nettyFilterRegistration -> {
                        try {
                            return nettyFilterRegistration.getFilter();
                        } catch (ServletException e) {
                            return null;
                        }
                    }).collect(toList());
            FilterChain filterChain = new SimpleFilterChain(servlet, filters);
            return new MyRequestDispatcher(this, filterChain);
        } catch (ServletException e) {
            log.info("{}",e);
            throw new RuntimeException();
        }
    }

    @Override
    public RequestDispatcher getNamedDispatcher(String name) {
        return null;
    }

    @Override
    public Servlet getServlet(String name) throws ServletException {
        return null;
    }

    @Override
    public Enumeration<Servlet> getServlets() {
        return null;
    }

    @Override
    public Enumeration<String> getServletNames() {
        return null;
    }

    @Override
    public void log(String msg) {

    }

    @Override
    public void log(Exception exception, String msg) {

    }

    @Override
    public void log(String message, Throwable throwable) {

    }

    @Override
    public String getRealPath(String path) {
        return null;
    }

    @Override
    public String getServerInfo() {
        return null;
    }

    @Override
    public String getInitParameter(String name) {
        return null;
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(this.initParameters.keySet());
    }

    @Override
    public boolean setInitParameter(String name, String value) {
        Assert.notNull(name, "Parameter name must not be null");
        if (this.initParameters.containsKey(name)) {
            return false;
        }
        this.initParameters.put(name, value);
        return true;
    }

    public void addInitParameter(String name, String value) {
        Assert.notNull(name, "Parameter name must not be null");
        this.initParameters.put(name, value);
    }

    @Override
    @Nullable
    public Object getAttribute(String name) {
        Assert.notNull(name, "Attribute name must not be null");
        return this.attributes.get(name);
    }

    @Override
    public void setAttribute(String name, @Nullable Object value) {
        Assert.notNull(name, "Attribute name must not be null");
        if (value != null) {
            this.attributes.put(name, value);
        }
        else {
            this.attributes.remove(name);
        }
    }

    @Override
    public void removeAttribute(String name) {

    }

    @Override
    public String getServletContextName() {
        return null;
    }

    // 注册servlet
    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, String className) {
        return addServlet(servletName, className, null);
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
        return addServlet(servletName, servlet.getClass().getName(), servlet);
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
        return addServlet(servletName, servletClass.getName());
    }

    /**
     * 自动装配servlet
     * @param servletName
     * @param className
     * @param servlet
     * @return
     */
    private ServletRegistration.Dynamic addServlet(String servletName, String className, Servlet servlet) {
        MyServletRegistration servletRegistration = new MyServletRegistration(this, servletName, className,
                servlet);
        servlets.put(servletName, servletRegistration);
        return servletRegistration;
    }


    @Override
    public ServletRegistration.Dynamic addJspFile(String servletName, String jspFile) {
        return null;
    }

    @Override
    public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
        return null;
    }

    @Override
    public ServletRegistration getServletRegistration(String servletName) {
        return null;
    }

    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        return null;
    }

    // 注册Filter
    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, String className) {
        return addFilter(filterName, className, null);
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
        return addFilter(filterName, filter.getClass().getName(), filter);
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
        return addFilter(filterName, filterClass.getName());
    }


    private FilterRegistration.Dynamic addFilter(String filterName, String className, Filter filter) {
        MyFilterRegistration filterRegistration = new MyFilterRegistration(this, filterName, className, filter);
        filters.put(filterName, filterRegistration);
        return filterRegistration;
    }
    @Override
    public <T extends Filter> T createFilter(Class<T> clazz) {
        return null;
    }

    @Override
    public FilterRegistration getFilterRegistration(String filterName) {
        return filters.get(filterName);
    }

    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        return ImmutableMap.copyOf(filters);
    }

    @Override
    public SessionCookieConfig getSessionCookieConfig() {
        return null;
    }

    @Override
    public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {

    }

    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        return null;
    }

    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        return null;
    }

    // 注册Listener
    @Override
    public void addListener(String className) {

    }

    @Override
    public <T extends EventListener> void addListener(T t) {

    }

    @Override
    public void addListener(Class<? extends EventListener> listenerClass) {

    }

    @Override
    public <T extends EventListener> T createListener(Class<T> clazz) {
        return null;
    }

    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
        return null;
    }

    @Override
    public ClassLoader getClassLoader() {
        return this.resourceLoader.getClassLoader();
    }

    @Override
    public void declareRoles(String... roleNames) {

    }

    @Override
    public String getVirtualServerName() {
        return null;
    }

    @Override
    public int getSessionTimeout() {
        return 0;
    }

    @Override
    public void setSessionTimeout(int sessionTimeout) {

    }

    @Override
    public String getRequestCharacterEncoding() {
        return null;
    }

    @Override
    public void setRequestCharacterEncoding(String encoding) {

    }

    @Override
    public String getResponseCharacterEncoding() {
        return null;
    }

    @Override
    public void setResponseCharacterEncoding(String encoding) {

    }
}
