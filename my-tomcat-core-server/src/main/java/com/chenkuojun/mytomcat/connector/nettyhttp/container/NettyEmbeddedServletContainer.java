package com.chenkuojun.mytomcat.connector.nettyhttp.container;

import com.chenkuojun.mytomcat.connector.nettyhttp.config.NettyContainerConfig;
import com.chenkuojun.mytomcat.connector.nettyhttp.context.NettyEmbeddedContext;
import com.google.common.base.StandardSystemProperty;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.server.WebServerException;

import javax.net.ssl.KeyManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;


@Slf4j
public class NettyEmbeddedServletContainer implements WebServer {
    private final NettyEmbeddedContext context;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private DefaultEventExecutorGroup servletExecutor;

    private NettyContainerConfig nettyContainerConfig;

    public NettyEmbeddedServletContainer(NettyContainerConfig nettyContainerConfig, NettyEmbeddedContext context) {
        this.nettyContainerConfig = nettyContainerConfig;
        this.context = context;
    }

    @Override
    public void start() throws WebServerException {
        SslContext sslcontext = null;
        if (nettyContainerConfig.isSsl()) {//如果启用https访问方式
            KeyManagerFactory kmf;
            InputStream in = null;

            try {
                KeyStore ks = KeyStore.getInstance("JKS");
                in = new FileInputStream(nettyContainerConfig.getCaPath());
                ks.load(in, nettyContainerConfig.getCaPassWord().toCharArray());
                kmf = KeyManagerFactory.getInstance("SunX509");
                kmf.init(ks, nettyContainerConfig.getCaPassWord().toCharArray());
                sslcontext = SslContextBuilder.forServer(kmf).build();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(0);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e1) {

                    }
                }
            }
        }
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        groups(serverBootstrap);
        servletExecutor = new DefaultEventExecutorGroup(nettyContainerConfig.getMaxThreads() * 2);
        serverBootstrap.childHandler(new NettyEmbeddedServletInitializer(servletExecutor, context,sslcontext));

        // Don't yet need the complexity of lifecycle state, listeners etc, so tell the context it's initialised here
        context.setInitialised(true);

        ChannelFuture future = serverBootstrap.bind(nettyContainerConfig.getPORT()).awaitUninterruptibly();
        // noinspection ThrowableResultOfMethodCallIgnored
        Throwable cause = future.cause();
        if (null != cause) {
            throw new WebServerException("Could not start my-tomcat(Netty) server", cause);
        }
        log.info( "{} started on port: {}",context.getServerInfo(), getPort());
    }

    private void groups(ServerBootstrap b) {
        if (StandardSystemProperty.OS_NAME.value().equals("Linux")) {
            bossGroup = new EpollEventLoopGroup(1);
            workerGroup = new EpollEventLoopGroup(nettyContainerConfig.getMaxThreads() * 2);
            b.channel(EpollServerSocketChannel.class).group(bossGroup, workerGroup).option(EpollChannelOption.TCP_CORK,
                    true);
        } else {
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup(nettyContainerConfig.getMaxThreads() * 2);
            b.channel(NioServerSocketChannel.class).group(bossGroup, workerGroup);
        }
        b.option(ChannelOption.TCP_NODELAY, true).option(ChannelOption.SO_REUSEADDR, true).option(ChannelOption.SO_BACKLOG,
                1024);
        log.info("{}" ,b);
    }

    @Override
    public void stop() throws WebServerException {
        try {
            if (null != bossGroup) {
                bossGroup.shutdownGracefully().await();
            }
            if (null != workerGroup) {
                workerGroup.shutdownGracefully().await();
            }
            if (null != servletExecutor) {
                servletExecutor.shutdownGracefully().await();
            }
        } catch (InterruptedException e) {
            throw new WebServerException("Container stop interrupted", e);
        }
    }

    @Override
    public int getPort() {
        return nettyContainerConfig.getPORT();
    }
}
