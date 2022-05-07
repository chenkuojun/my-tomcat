package com.chenkuojun.mytomcat.init.dispatcher;

import com.chenkuojun.mytomcat.connector.nettyhttp.http.NettyHttpRequest;

import javax.servlet.*;
import java.io.IOException;


/**
 * @author chenkuojun
 */
public class MyRequestDispatcher implements RequestDispatcher {

    @SuppressWarnings("unused")
    private final ServletContext context;
    private final FilterChain filterChain;

    public MyRequestDispatcher(ServletContext context, FilterChain filterChain) {
        this.context = context;
        this.filterChain = filterChain;
    }

    @Override
    public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        request.setAttribute(NettyHttpRequest.DISPATCHER_TYPE, DispatcherType.FORWARD);
        // TODO implement
    }

    @Override
    public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        request.setAttribute(NettyHttpRequest.DISPATCHER_TYPE, DispatcherType.INCLUDE);
        // TODO implement
    }

    public void dispatch(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        request.setAttribute(NettyHttpRequest.DISPATCHER_TYPE, DispatcherType.ASYNC);
        filterChain.doFilter(request, response);
    }
}
