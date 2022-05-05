package com.chenkuojun.mytomcat.startup;


import com.chenkuojun.mytomcat.connector.http.HttpConnector;
import com.chenkuojun.mytomcat.connector.nettyhttp1.NettyHttpConnector;
import com.chenkuojun.mytomcat.connector.niohttp.NioHttpConnector;
import com.chenkuojun.mytomcat.utils.YamlParseUtil;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServlet;
import java.util.HashMap;
import java.util.Map;
@Slf4j
public final class Bootstrap {

  private final static  Map<String, HttpServlet> servletMap = new HashMap<>();
  // 默认NIO
  private static  String IO_TYPE = "NIO";
  public void registerServlet(String urlPattern, HttpServlet servlet) {
    servletMap.put(urlPattern, servlet);
  }
  public static void start() {
    // 获取配置文件
    loadIOType();
    if(IO_TYPE.equals("NIO")){
      //NioHttpConnector connector = new NioHttpConnector(servletMap);
      NettyHttpConnector connector = new NettyHttpConnector(servletMap);
      connector.run();
    }else {
      HttpConnector connector = new HttpConnector(servletMap);
      connector.start();
    }
  }

  private static void loadIOType() {
    IO_TYPE = YamlParseUtil.INSTANCE.getValueByKey("mytomcat.iotype") == null
            ? IO_TYPE : (String)YamlParseUtil.INSTANCE.getValueByKey("mytomcat.iotype");
    log.info("【my-tomcat starting IO TYPE is:{}】", IO_TYPE);
  }
}
