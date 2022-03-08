package com.chen.mytomcat.constant;

public final class Constants {

  public static final String WEB_ROOT =
          Constants.class.getClassLoader().getResource("").getPath();
  public static final String Package = "ex03.ex04.pyrmont.connector.http";
  public static final String STATIC_PATH = "/static";
  public static final String SERVLET_PATH = "/servlet";
  public static final int DEFAULT_CONNECTION_TIMEOUT = 60000;
  public static final int PROCESSOR_IDLE = 0;
  public static final int PROCESSOR_ACTIVE = 1;
}
