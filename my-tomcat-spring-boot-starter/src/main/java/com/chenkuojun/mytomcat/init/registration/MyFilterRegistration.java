package com.chenkuojun.mytomcat.init.registration;

import com.chenkuojun.mytomcat.init.context.MyServletContext;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletException;
import java.util.Collection;
import java.util.EnumSet;

/**
 * @author chenkuojun
 */
public class MyFilterRegistration extends AbstractRegistration implements FilterRegistration.Dynamic {

    private volatile boolean initialised;
    private Filter filter;

    public MyFilterRegistration(MyServletContext context, String filterName, String className, Filter filter) {
        super(filterName, className, context);
        this.filter = filter;
    }

    public Filter getFilter() throws ServletException {
        if (!initialised) {
            synchronized (this) {
                if (!initialised) {
                    if (null == filter) {
                        try {
                            filter = (Filter) Class.forName(getClassName()).newInstance();
                        } catch (Exception e) {
                            throw new ServletException(e);
                        }
                    }
                    filter.init(this);
                    initialised = true;
                }
            }
        }
        return filter;
    }

    @Override
    public void addMappingForServletNames(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter,
                                          String... servletNames) {

    }

    @Override
    public Collection<String> getServletNameMappings() {
        return null;
    }

    @Override
    public void addMappingForUrlPatterns(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter,
                                         String... urlPatterns) {
        MyServletContext context = getNettyContext();
        for (String urlPattern : urlPatterns) {
            context.addFilterMapping(dispatcherTypes, isMatchAfter, urlPattern);
        }
    }

    @Override
    public Collection<String> getUrlPatternMappings() {
        return null;
    }
}
