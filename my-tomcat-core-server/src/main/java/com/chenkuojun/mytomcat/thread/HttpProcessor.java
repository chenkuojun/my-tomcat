package com.chenkuojun.mytomcat.thread;


import com.chenkuojun.mytomcat.connector.http.*;
import com.chenkuojun.mytomcat.constant.Constants;
import com.chenkuojun.mytomcat.processor.StaticResourceProcessor;
import com.chenkuojun.mytomcat.utils.RequestUtil;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
public class HttpProcessor extends Thread{

  private final Map<String, HttpServlet> servletMap;
  /**
   * The HttpConnector with which this processor is associated.
   */
  // the default buffer size
  private static final int BUFFER_SIZE = 2048;
  private HttpConnector connector;
  private Socket socket;
  private HttpRequest request;
  private HttpRequestLine requestLine = new HttpRequestLine();
  private HttpResponse response;

  public HttpProcessor(Socket socket,HttpConnector connector, Map<String, HttpServlet> servletMap) {
    this.socket = socket;
    this.connector = connector;
    this.servletMap = servletMap;
  }
  @Override
  public void run() {
    SocketInputStream input;
    OutputStream output;
    try {
      input = new SocketInputStream(socket.getInputStream(), BUFFER_SIZE);
      output = socket.getOutputStream();

      // create HttpRequest object and parse
      request = new HttpRequest(input);
      // 解析请求信息
      //getByInputStreamInfo(input);

      // create HttpResponse object
      response = new HttpResponse(output);
      response.setRequest(request);
      parseRequest(input, output);
      parseHeaders(input);
      String requestURI = request.getRequestURI();
      log.info("requestURI:{}",requestURI);
      // 请求以 .html 或者 .htm 结尾，去static 下面去寻找对应的静态资源
      //if(requestURI.endsWith(Constants.HTML) ||
      //        requestURI.endsWith(Constants.HTM) ||
      //        requestURI.endsWith(Constants.JPEG) ||
      //        requestURI.endsWith(Constants.GIF) ||
      //        requestURI.endsWith(Constants.PNG)){
      //  StaticResourceProcessor processor = new StaticResourceProcessor();
      //  processor.process(request, response);
      //}else {
      //  // 动态 servlet 处理
      //  HttpServlet httpServlet = servletMap.get("/");
      //  httpServlet.service(request, response);
      //}
      // 动态 servlet 处理
      HttpServlet httpServlet = servletMap.get("/");
      httpServlet.service(request, response);
      socket.close();
    }
    catch (Exception e) {
      log.info("{}",e);
      e.printStackTrace();
    }
  }

  /**
   * This method is the simplified version of the similar method in
   * org.apache.catalina.connector.http.HttpProcessor.
   * However, this method only parses some "easy" headers, such as
   * "cookie", "content-length", and "content-type", and ignore other headers.
   * @param input The input stream connected to our socket
   *
   * @exception IOException if an input/output error occurs
   * @exception ServletException if a parsing error occurs
   * @return
   */
  private void parseHeaders(SocketInputStream input)
    throws IOException, ServletException {
    while (true) {
      HttpHeader header = new HttpHeader();;

      // Read the next header
      input.readHeader(header);
      if (header.nameEnd == 0) {
        if (header.valueEnd == 0) {
          return;
        }
        else {
          throw new ServletException
            ("Invalid HTTP header format");
        }
      }

      String name = new String(header.name, 0, header.nameEnd);
      String value = new String(header.value, 0, header.valueEnd);
      request.addHeader(name, value);
      // do something for some headers, ignore others.
      if (name.equals("cookie")) {
        Cookie cookies[] = RequestUtil.parseCookieHeader(value);
        for (int i = 0; i < cookies.length; i++) {
          if (cookies[i].getName().equals("jsessionid")) {
            // Override anything requested in the URL
            if (!request.isRequestedSessionIdFromCookie()) {
              // Accept only the first session id cookie
              request.setRequestedSessionId(cookies[i].getValue());
              request.setRequestedSessionCookie(true);
              request.setRequestedSessionURL(false);
            }
          }
          request.addCookie(cookies[i]);
        }
      }
      else if (name.equals("content-length")) {
        int n = -1;
        try {
          n = Integer.parseInt(value);
        }
        catch (Exception e) {
          throw new ServletException("Invalid 'Content-Length' header");
        }
        request.setContentLength(n);
      }
      else if (name.equals("content-type")) {
        request.setContentType(value);
      }
    } //end while
  }


  private void parseRequest(SocketInputStream input, OutputStream output)
    throws IOException, ServletException {

    // Parse the incoming request line
    input.readRequestLine(requestLine);
    String method =
      new String(requestLine.method, 0, requestLine.methodEnd);
    String uri = null;
    String protocol = new String(requestLine.protocol, 0, requestLine.protocolEnd);

    // Validate the incoming request line
    if (method.length() < 1) {
      throw new ServletException("Missing HTTP request method");
    }
    else if (requestLine.uriEnd < 1) {
      throw new ServletException("Missing HTTP request URI");
    }
    // Parse any query parameters out of the request URI
    int question = requestLine.indexOf("?");
    if (question >= 0) {
      request.setQueryString(new String(requestLine.uri, question + 1,
        requestLine.uriEnd - question - 1));
      uri = new String(requestLine.uri, 0, question);
    }
    else {
      request.setQueryString(null);
      uri = new String(requestLine.uri, 0, requestLine.uriEnd);
    }


    // Checking for an absolute URI (with the HTTP protocol)
    if (!uri.startsWith("/")) {
      int pos = uri.indexOf("://");
      // Parsing out protocol and host name
      if (pos != -1) {
        pos = uri.indexOf('/', pos + 3);
        if (pos == -1) {
          uri = "";
        }
        else {
          uri = uri.substring(pos);
        }
      }
    }

    // Parse any requested session ID out of the request URI
    String match = ";jsessionid=";
    int semicolon = uri.indexOf(match);
    if (semicolon >= 0) {
      String rest = uri.substring(semicolon + match.length());
      int semicolon2 = rest.indexOf(';');
      if (semicolon2 >= 0) {
        request.setRequestedSessionId(rest.substring(0, semicolon2));
        rest = rest.substring(semicolon2);
      }
      else {
        request.setRequestedSessionId(rest);
        rest = "";
      }
      request.setRequestedSessionURL(true);
      uri = uri.substring(0, semicolon) + rest;
    }
    else {
      request.setRequestedSessionId(null);
      request.setRequestedSessionURL(false);
    }

    // Normalize URI (using String operations at the moment)
    String normalizedUri = normalize(uri);

    // Set the corresponding request properties
    ((HttpRequest) request).setMethod(method);
    request.setProtocol(protocol);
    if (normalizedUri != null) {
      ((HttpRequest) request).setRequestURI(normalizedUri);
    }
    else {
      ((HttpRequest) request).setRequestURI(uri);
    }

    if (normalizedUri == null) {
      throw new ServletException("Invalid URI: " + uri + "'");
    }
  }

  /**
   * Return a context-relative path, beginning with a "/", that represents
   * the canonical version of the specified path after ".." and "." elements
   * are resolved out.  If the specified path attempts to go outside the
   * boundaries of the current context (i.e. too many ".." path elements
   * are present), return <code>null</code> instead.
   *
   * @param path Path to be normalized
   * @return String
   */
  protected String normalize(String path) {
    if (path == null)
      return null;
    // Create a place for the normalized path
    String normalized = path;

    // Normalize "/%7E" and "/%7e" at the beginning to "/~"
    if (normalized.startsWith("/%7E") || normalized.startsWith("/%7e"))
      normalized = "/~" + normalized.substring(4);

    // Prevent encoding '%', '/', '.' and '\', which are special reserved
    // characters
    if ((normalized.indexOf("%25") >= 0)
      || (normalized.indexOf("%2F") >= 0)
      || (normalized.indexOf("%2E") >= 0)
      || (normalized.indexOf("%5C") >= 0)
      || (normalized.indexOf("%2f") >= 0)
      || (normalized.indexOf("%2e") >= 0)
      || (normalized.indexOf("%5c") >= 0)) {
      return null;
    }

    if (normalized.equals("/."))
      return "/";

    // Normalize the slashes and add leading slash if necessary
    if (normalized.indexOf('\\') >= 0)
      normalized = normalized.replace('\\', '/');
    if (!normalized.startsWith("/"))
      normalized = "/" + normalized;

    // Resolve occurrences of "//" in the normalized path
    while (true) {
      int index = normalized.indexOf("//");
      if (index < 0)
        break;
      normalized = normalized.substring(0, index) +
        normalized.substring(index + 1);
    }

    // Resolve occurrences of "/./" in the normalized path
    while (true) {
      int index = normalized.indexOf("/./");
      if (index < 0)
        break;
      normalized = normalized.substring(0, index) +
        normalized.substring(index + 2);
    }

    // Resolve occurrences of "/../" in the normalized path
    while (true) {
      int index = normalized.indexOf("/../");
      if (index < 0)
        break;
      if (index == 0)
        return (null);  // Trying to go outside our context
      int index2 = normalized.lastIndexOf('/', index - 1);
      normalized = normalized.substring(0, index2) +
        normalized.substring(index + 3);
    }

    // Declare occurrences of "/..." (three or more dots) to be invalid
    // (on some Windows platforms this walks the directory tree!!!)
    if (normalized.indexOf("/...") >= 0)
      return (null);

    // Return the normalized path that we have completed
    return (normalized);

  }

  private void getByInputStreamInfo(InputStream input) throws IOException {
    byte[] bytes = new byte[1024];
    int len;
    StringBuilder requestRead = new StringBuilder();
    if ((len = input.read(bytes)) != -1) {
      requestRead.append(new String(bytes, 0, len, StandardCharsets.UTF_8));
    }
    String request = requestRead.toString();
    log.info("request: \r\n{}", request);
  }

}
