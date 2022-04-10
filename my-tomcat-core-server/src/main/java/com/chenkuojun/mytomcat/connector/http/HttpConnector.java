package com.chenkuojun.mytomcat.connector.http;

import com.chenkuojun.mytomcat.processor.HttpProcessor;
import com.chenkuojun.mytomcat.utils.YamlParseUtil;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
public class HttpConnector implements Runnable {

  boolean stopped = false;

  private String scheme = "http";

  // default port
  private int port = 8090;

  // default host
  private String host = "127.0.0.1";
  private final Map<String, HttpServlet> servletMap;

  public HttpConnector(Map<String, HttpServlet> servletMap){
    this.servletMap = servletMap;
  }

  public void run() {
    ServerSocket serverSocket = null;
    // 加载配置文件中的主机和端口号
    loadProperties();
    ThreadPoolExecutor threadPoolExecutor = getThreadPoolExecutor();
    try {
      serverSocket =  new ServerSocket(port, 1, InetAddress.getByName(host));
      log.info("my-tomcat start up success,ip is:{},port is:{}" ,host,port);
    }
    catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
    while (!stopped) {
      // 接收到请求
      Socket socket;
      try {
        log.info("receiver new quest");
        socket = serverSocket.accept();
      }catch (Exception e) {
        log.info("request exception:{}",e);
        continue;
      }
      // Hand this socket off to an HttpProcessor
      HttpProcessor processor = new HttpProcessor(socket,this, servletMap);
      threadPoolExecutor.execute(processor);
    }
  }

  public void start() {
    Thread thread = new Thread(this);
    thread.start();
  }

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
