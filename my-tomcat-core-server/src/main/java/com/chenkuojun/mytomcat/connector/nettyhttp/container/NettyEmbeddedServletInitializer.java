package com.chenkuojun.mytomcat.connector.nettyhttp.container;

import com.chenkuojun.mytomcat.connector.nettyhttp.context.NettyEmbeddedContext;
import com.chenkuojun.mytomcat.connector.nettyhttp.handler.RequestDispatcherHandler;
import com.chenkuojun.mytomcat.connector.nettyhttp.handler.ServletContentHandler;
import com.chenkuojun.mytomcat.connector.nettyhttp.utils.ChannelThreadLocal;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.EventExecutorGroup;

import javax.net.ssl.SSLEngine;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author chenkuojun
 */
public class NettyEmbeddedServletInitializer extends ChannelInitializer<SocketChannel> {

    private final EventExecutorGroup servletExecutor;
    private final RequestDispatcherHandler requestDispatcherHandler;
    private final NettyEmbeddedContext servletContext;
    private SslContext sslContext;

    public NettyEmbeddedServletInitializer(EventExecutorGroup servletExecutor, NettyEmbeddedContext servletContext, SslContext sslContext) {
        this.servletContext = servletContext;
        this.servletExecutor = checkNotNull(servletExecutor);
        this.sslContext=sslContext;
        requestDispatcherHandler = new RequestDispatcherHandler(servletContext);

    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        if (sslContext != null) {
            SSLEngine sslEngine = sslContext.newEngine(ch.alloc());
            sslEngine.setUseClientMode(false);
            pipeline.addLast("ssl", new SslHandler(sslEngine));
        }
        pipeline.addLast("codec", new HttpServerCodec());
        pipeline.addLast("aggregator", new HttpObjectAggregator(1024 * 1024 * 64));
        pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
        // 构建request和response
        pipeline.addLast("handler", new ServletContentHandler(servletContext));
        pipeline.addLast(servletExecutor, "filterChain", requestDispatcherHandler);
        ChannelThreadLocal.set(ch);
    }
}
