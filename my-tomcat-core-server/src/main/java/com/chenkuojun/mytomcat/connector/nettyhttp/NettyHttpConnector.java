package com.chenkuojun.mytomcat.connector.nettyhttp;

import com.chenkuojun.mytomcat.connector.nettyhttp.handler.NettyRequestDispatcherHandler;
import com.chenkuojun.mytomcat.connector.nettyhttp.handler.NettyServletContentHandler;
import com.chenkuojun.mytomcat.utils.YamlParseUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import javax.servlet.ServletContext;
import java.util.Date;

@Slf4j
@Data
public class NettyHttpConnector {

    // default port
    private int port = 8090;
    // default host
    private String host = "127.0.0.1";
    private final ServletContext servletContext;

    public NettyHttpConnector(ServletContext servletContext){
        this.servletContext = servletContext;
    }

    @SneakyThrows
    public void run() {
        // 1.加载配置文件中的主机和端口号
        loadProperties();

        NioEventLoopGroup boosGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        final ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap
                .group(boosGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    protected void initChannel(NioSocketChannel ch) {
                        ch.pipeline().addLast(new HttpServerCodec());
                        ch.pipeline().addLast(new HttpObjectAggregator(1024 * 1024 * 64));
                        ch.pipeline().addLast(new ChunkedWriteHandler());
                        // 构建request和response
                        ch.pipeline().addLast(new NettyServletContentHandler());
                        // 负责处理具体的servlet
                        ch.pipeline().addLast(new NettyRequestDispatcherHandler(servletContext));
                    }
                });
        bind(serverBootstrap, port);
        log.info("my-tomcat start up by netty success,ip is:{},port is:{}" ,host,port);
    }

    private void loadProperties() {
        log.info("my-tomcat is load properties");port = YamlParseUtil.INSTANCE.getValueByKey("server.port") == null
                ? port : (int) YamlParseUtil.INSTANCE.getValueByKey("server.port");
        log.info("【my-tomcat try starting with ip:{},and the port is:{}】", host,port);
        host = YamlParseUtil.INSTANCE.getValueByKey("server.host") == null
                ? host : (String) YamlParseUtil.INSTANCE.getValueByKey("server.host");
    }

    private static void bind(final ServerBootstrap serverBootstrap, final int port) {
        serverBootstrap.bind(port).addListener(future -> {
            if (future.isSuccess()) {
                System.out.println(new Date() + ": 端口[" + port + "]绑定成功!");
            } else {
                System.err.println("端口[" + port + "]绑定失败!");
            }
        });
    }
}
