package com.chenkuojun.mytomcat.connector.nettyhttp.dispatcher;

import com.chenkuojun.mytomcat.connector.nettyhttp.http.NettyHttpServletRequest;

import javax.servlet.*;
import java.io.IOException;


/**
 * @author chenkuojun
 */
public class NettyRequestDispatcher implements RequestDispatcher {

    @SuppressWarnings("unused")
    private final ServletContext context;
    private final FilterChain filterChain;

    public NettyRequestDispatcher(ServletContext context, FilterChain filterChain) {
        this.context = context;
        this.filterChain = filterChain;
    }

    @Override
    public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        request.setAttribute(NettyHttpServletRequest.DISPATCHER_TYPE, DispatcherType.FORWARD);
        // TODO implement
    }

    @Override
    public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        request.setAttribute(NettyHttpServletRequest.DISPATCHER_TYPE, DispatcherType.INCLUDE);
        // TODO implement
    }

    public void dispatch(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        request.setAttribute(NettyHttpServletRequest.DISPATCHER_TYPE, DispatcherType.ASYNC);
        filterChain.doFilter(request, response);
    }
}
