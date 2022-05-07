package com.chenkuojun.mytomcat.startup;


import com.chenkuojun.mytomcat.connector.http.HttpConnector;
import com.chenkuojun.mytomcat.init.context.MyServletContext;
import com.chenkuojun.mytomcat.connector.nettyhttp.NettyHttpConnector;
import com.chenkuojun.mytomcat.utils.YamlParseUtil;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletContext;

@Slf4j
public final class Bootstrap {
  private static ServletContext servletContext;
  // 默认NIO
  private static  String IO_TYPE = "NIO";
  public static void start() {
    // 获取配置文件
    String type = loadIOType();
    if(IO_TYPE.equals(type)){
      //NioHttpConnector connector = new NioHttpConnector(servletMap);
      NettyHttpConnector connector = new NettyHttpConnector(servletContext);
      connector.run();
    }else {
      HttpConnector connector = new HttpConnector(servletContext);
      connector.start();
    }
  }

  private static String loadIOType() {
    return YamlParseUtil.INSTANCE.getValueByKey("my-tomcat.io-type") == null
            ? IO_TYPE : (String)YamlParseUtil.INSTANCE.getValueByKey("my-tomcat.io-type");
  }

  public ServletContext getServletContext() {
    return servletContext;
  }

  public void setServletContext(MyServletContext servletContext) {
    this.servletContext = servletContext;
  }
}
