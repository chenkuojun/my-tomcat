package com.chen.mytomcat.processor;

import com.chen.mytomcat.connector.http.HttpRequest;
import com.chen.mytomcat.connector.http.HttpResponse;

import java.io.IOException;

public class StaticResourceProcessor {

  public void process(HttpRequest request, HttpResponse response) {
    try {
      response.sendStaticResource();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

}
