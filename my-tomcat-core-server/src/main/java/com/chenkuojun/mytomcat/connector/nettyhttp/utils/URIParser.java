package com.chenkuojun.mytomcat.connector.nettyhttp.utils;

import com.chenkuojun.mytomcat.connector.nettyhttp.context.NettyEmbeddedContext;
import org.apache.commons.lang3.StringUtils;


/**
 * @author chenkuojun
 */
public class URIParser {

    private NettyEmbeddedContext context;

    private String servletPath;

    private String requestUri;

    private String pathInfo;

    private String queryString;

    public URIParser(NettyEmbeddedContext context) {
        this.context = context;
    }

    public void parse(String uri) {
        /**
         * 如果有contextPath 需要去掉
         */
        if (StringUtils.isNoneBlank(context.getContextPath())) {
            uri=uri.replaceAll(context.getContextPath(),"");
        }
        int index = uri.indexOf('?');
        this.servletPath = this.context.getMatchingUrlPattern(uri);
        if (!this.servletPath.startsWith("/")) {
            this.servletPath = "/" + this.servletPath;
        }
        if (index != -1) {
            this.pathInfo = uri.substring(servletPath.length(), index);
            this.queryString = uri.substring(index + 1);
            this.requestUri = uri.substring(0, index);
        } else {
            this.pathInfo = uri.substring(servletPath.length());
            this.requestUri = uri;
        }

        if (this.requestUri.endsWith("/")) {
            this.requestUri.substring(0, this.requestUri.length() - 1);
        }

        if (this.pathInfo.equals("")) {
            this.pathInfo = null;
        } else if (!this.pathInfo.startsWith("/")) {
            this.pathInfo = "/" + this.pathInfo;
        }
    }

    public String getServletPath() {
        return servletPath;
    }

    public String getQueryString() {
        return queryString;
    }

    public String getPathInfo() {
        return this.pathInfo;
    }

    public String getRequestUri() {
        return requestUri;
    }
}
