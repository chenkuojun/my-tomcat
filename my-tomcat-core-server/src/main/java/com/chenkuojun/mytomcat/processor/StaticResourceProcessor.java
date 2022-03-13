package com.chenkuojun.mytomcat.processor;

import com.chenkuojun.mytomcat.connector.http.HttpRequest;
import com.chenkuojun.mytomcat.connector.http.HttpResponse;

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
