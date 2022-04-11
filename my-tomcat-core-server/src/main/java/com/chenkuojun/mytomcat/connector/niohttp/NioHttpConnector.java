package com.chenkuojun.mytomcat.connector.niohttp;

import com.chenkuojun.mytomcat.processor.NioHttpProcessor;
import com.chenkuojun.mytomcat.utils.YamlParseUtil;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServlet;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Data
//public class NioHttpConnector implements Runnable {
public class NioHttpConnector {

    // default port
    private int port = 8090;

    private Selector selector;

    // default host
    private String host = "127.0.0.1";
    private final Map<String, HttpServlet> servletMap;

    private NioHttpRequest nioHttpRequest;

    public NioHttpConnector(Map<String, HttpServlet> servletMap){
        this.servletMap = servletMap;
    }

    @SneakyThrows
    public void run() {
        // 1.加载配置文件中的主机和端口号
        loadProperties();
        // 2.开启线程池
        ThreadPoolExecutor threadPoolExecutor = getThreadPoolExecutor();
        // 3.启动selector，和Channel通道
        selector = SelectorProvider.provider().openSelector();
        ServerSocketChannel channel = ServerSocketChannel.open();
        //3.配置通道非阻塞
        channel.configureBlocking(false);
        //4.监听端口
        InetSocketAddress address = new InetSocketAddress(port);
        channel.socket().bind(address);
        //5.将通道注册给监听器
        channel.register(selector, SelectionKey.OP_ACCEPT);
        log.info("my-tomcat start up by NIO success,ip is:{},port is:{}" ,host,port);
        NioHttpProcessor nioHttpProcessor = new NioHttpProcessor(selector, servletMap);
        nioHttpProcessor.run();
    }
    //public void start() {
    //    Thread thread = new Thread(this);
    //    thread.start();
    //}

    private void loadProperties() {
        log.info("my-tomcat is load properties");port = YamlParseUtil.INSTANCE.getValueByKey("server.port") == null
                ? port : (int) YamlParseUtil.INSTANCE.getValueByKey("server.port");
        log.info("【my-tomcat try starting with ip:{},and the port is:{}】", host,port);
        host = YamlParseUtil.INSTANCE.getValueByKey("server.host") == null
                ? host : (String) YamlParseUtil.INSTANCE.getValueByKey("server.host");
    }

    /**
     * create a threadPoolExecutor.
     *
     * @return threadPoolExecutor threadPoolExecutor
     */
    private ThreadPoolExecutor getThreadPoolExecutor() {
        // core parameters
        int corePoolSize = 10;
        int maximumPoolSize = 50;
        long keepAliveTime = 100L;
        TimeUnit unit = TimeUnit.SECONDS;
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(50);
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        RejectedExecutionHandler handler = new ThreadPoolExecutor.AbortPolicy();
        return new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                unit,
                workQueue,
                threadFactory,
                handler
        );
    }
}
