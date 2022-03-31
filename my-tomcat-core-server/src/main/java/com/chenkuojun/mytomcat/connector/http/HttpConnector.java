package com.chenkuojun.mytomcat.connector.http;

import com.chenkuojun.mytomcat.utils.YamlParseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

public class HttpConnector implements Runnable {

  static Logger log = LoggerFactory.getLogger(HttpConnector.class);

  boolean stopped;

  private String scheme = "http";

  // default port
  private int port = 8090;

  // default host
  private String host = "127.0.0.1";
  private final Map<String, HttpServlet> servletMap;

  public String getScheme() {
    return scheme;
  }
  public HttpConnector(Map<String, HttpServlet> servletMap){
    this.servletMap = servletMap;
  }

  public void run() {
    ServerSocket serverSocket = null;
    // 加载配置文件中的主机和端口号
    loadProperties();
    try {
      serverSocket =  new ServerSocket(port, 1, InetAddress.getByName(host));
      log.info("my-tomcat start up success,ip is :{},port is:{}" ,host,port);
    }
    catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
    while (!stopped) {
      // Accept the next incoming connection from the server socket
      Socket socket = null;
      try {
        socket = serverSocket.accept();
        log.info("receiver new quest");
        log.info("接收到新的请求");
      }
      catch (Exception e) {
        continue;
      }
      // Hand this socket off to an HttpProcessor
      HttpProcessor processor = new HttpProcessor(this, servletMap);
      processor.process(socket);
    }
  }

  public void start() {
    Thread thread = new Thread(this);
    thread.start();
  }

  private void loadProperties() {
    log.info("my-tomcat is load properties");
    port = YamlParseUtil.INSTANCE.getValueByKey("server.port") == null
            ? port : (int) YamlParseUtil.INSTANCE.getValueByKey("server.port");
    log.info("【my-tomcat try starting with ip :{},and the port is:{}】", host,port);
    host = YamlParseUtil.INSTANCE.getValueByKey("server.host") == null
            ? host : (String) YamlParseUtil.INSTANCE.getValueByKey("server.host");
  }
}
