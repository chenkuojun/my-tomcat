package com.chenkuojun.mytomcat.constant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Constants {
  public static final String WEB_ROOT =
          Constants.class.getClassLoader().getResource("").getPath();
  public static final String Package = "ex03.ex04.pyrmont.connector.http";
  public static final String STATIC_PATH = "static";

  public static final String HTML = ".html";
  public static final List<String> IS_NOT_SERVLET_LIST = new ArrayList<>(Arrays.asList(".html", ".htm", ".jpeg",".png",".gif"));
  public static final String HTM = ".htm";
  public static final String JPEG = ".jpeg";
  public static final String PNG = ".png";
  public static final String GIF = ".gif";
  public static final String SERVLET_PATH = "/servlet";
  public static final int DEFAULT_CONNECTION_TIMEOUT = 60000;
  public static final int PROCESSOR_IDLE = 0;
  public static final int PROCESSOR_ACTIVE = 1;

}
