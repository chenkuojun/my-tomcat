package com.chenkuojun.mytomcat.connector.http;

import com.chenkuojun.mytomcat.utils.YamlParseUtil;

import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

public class HttpConnector implements Runnable {

  boolean stopped;
  private String scheme = "http";

  private int port = 8080;

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
    // 加载配置文件中的主机和端口号 todo 此处未生效
    loadProperties();
    try {
      serverSocket =  new ServerSocket(port, 1, InetAddress.getByName(host));
      System.out.println("my-tomcat 启动成功,ip为:" + host + "----端口号为:"+port);
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
        System.out.println("接收到新的请求");
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
    port = YamlParseUtil.INSTANCE.getValueByKey("server.port") == null
            ? port : (int) YamlParseUtil.INSTANCE.getValueByKey("server.port");
    System.out.println("my-tomcat 正在启动,ip为:" + host + "----端口号为:"+port);
    host = YamlParseUtil.INSTANCE.getValueByKey("server.host") == null
            ? host : (String) YamlParseUtil.INSTANCE.getValueByKey("server.host");
  }
}
