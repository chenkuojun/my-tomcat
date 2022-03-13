package com.chenkuojun.mytomcat.startup;


import com.chenkuojun.mytomcat.connector.http.HttpConnector;

import javax.servlet.http.HttpServlet;
import java.util.HashMap;
import java.util.Map;

public final class Bootstrap {

  private final static  Map<String, HttpServlet> servletMap = new HashMap<>();
  public void registerServlet(String urlPattern, HttpServlet servlet) {
    servletMap.put(urlPattern, servlet);
  }
  public static void main(String[] args) {
    start();
  }
  public static void start() {
    HttpConnector connector = new HttpConnector(servletMap);
    connector.start();
  }


}
