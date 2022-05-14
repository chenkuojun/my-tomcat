package com.chenkuojun.mytomcat.connector.niohttp;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class NioHttpResponse implements HttpServletResponse {

  private SelectionKey selectionKey;
  private PrintWriter writer;

  /**
   * The character encoding associated with this Response.
   */
  protected String encoding = null;

  protected HashMap headers = new HashMap();

  protected boolean committed = false;

  /**
   * The date format we will use for creating date headers.
   */
  protected final SimpleDateFormat format =
          new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz",Locale.US);

  private NioHttpRequest request;

  /**
   * The content type associated with this Response.
   */
  protected String contentType = null;

  /**
   * The set of Cookies associated with this Response.
   */
  protected ArrayList cookies = new ArrayList();

  /**
   * The actual number of bytes written to this Response.
   */
  protected int contentCount = 0;
  /**
   * The content length associated with this Response.
   */
  protected int contentLength = -1;

  /**
   * The error message set by <code>sendError()</code>.
   */
  protected String message = getStatusMessage(HttpServletResponse.SC_OK);
  /**
   * The HTTP status code associated with this Response.
   */
  protected int status = HttpServletResponse.SC_OK;




  public NioHttpResponse(SelectionKey selectionKey) {
    this.selectionKey = selectionKey;
  }

  SelectionKey getSelectionKey(){
    return this.selectionKey;
  }

  /**
   * Returns a default status message for the specified HTTP status code.
   *
   * @param status The status code for which a message is desired
   * @return  String
   */
  protected String getStatusMessage(int status) {
    switch (status) {
      case SC_OK:
        return ("OK");
      case SC_ACCEPTED:
        return ("Accepted");
      case SC_BAD_GATEWAY:
        return ("Bad Gateway");
      case SC_BAD_REQUEST:
        return ("Bad Request");
      case SC_CONFLICT:
        return ("Conflict");
      case SC_CONTINUE:
        return ("Continue");
      case SC_CREATED:
        return ("Created");
      case SC_EXPECTATION_FAILED:
        return ("Expectation Failed");
      case SC_FORBIDDEN:
        return ("Forbidden");
      case SC_GATEWAY_TIMEOUT:
        return ("Gateway Timeout");
      case SC_GONE:
        return ("Gone");
      case SC_HTTP_VERSION_NOT_SUPPORTED:
        return ("HTTP Version Not Supported");
      case SC_INTERNAL_SERVER_ERROR:
        return ("Internal Server Error");
      case SC_LENGTH_REQUIRED:
        return ("Length Required");
      case SC_METHOD_NOT_ALLOWED:
        return ("Method Not Allowed");
      case SC_MOVED_PERMANENTLY:
        return ("Moved Permanently");
      case SC_MOVED_TEMPORARILY:
        return ("Moved Temporarily");
      case SC_MULTIPLE_CHOICES:
        return ("Multiple Choices");
      case SC_NO_CONTENT:
        return ("No Content");
      case SC_NON_AUTHORITATIVE_INFORMATION:
        return ("Non-Authoritative Information");
      case SC_NOT_ACCEPTABLE:
        return ("Not Acceptable");
      case SC_NOT_FOUND:
        return ("Not Found");
      case SC_NOT_IMPLEMENTED:
        return ("Not Implemented");
      case SC_NOT_MODIFIED:
        return ("Not Modified");
      case SC_PARTIAL_CONTENT:
        return ("Partial Content");
      case SC_PAYMENT_REQUIRED:
        return ("Payment Required");
      case SC_PRECONDITION_FAILED:
        return ("Precondition Failed");
      case SC_PROXY_AUTHENTICATION_REQUIRED:
        return ("Proxy Authentication Required");
      case SC_REQUEST_ENTITY_TOO_LARGE:
        return ("Request Entity Too Large");
      case SC_REQUEST_TIMEOUT:
        return ("Request Timeout");
      case SC_REQUEST_URI_TOO_LONG:
        return ("Request URI Too Long");
      case SC_REQUESTED_RANGE_NOT_SATISFIABLE:
        return ("Requested Range Not Satisfiable");
      case SC_RESET_CONTENT:
        return ("Reset Content");
      case SC_SEE_OTHER:
        return ("See Other");
      case SC_SERVICE_UNAVAILABLE:
        return ("Service Unavailable");
      case SC_SWITCHING_PROTOCOLS:
        return ("Switching Protocols");
      case SC_UNAUTHORIZED:
        return ("Unauthorized");
      case SC_UNSUPPORTED_MEDIA_TYPE:
        return ("Unsupported Media Type");
      case SC_USE_PROXY:
        return ("Use Proxy");
      case 207:       // WebDAV
        return ("Multi-Status");
      case 422:       // WebDAV
        return ("Unprocessable Entity");
      case 423:       // WebDAV
        return ("Locked");
      case 507:       // WebDAV
        return ("Insufficient Storage");
      default:
        return ("HTTP Response Status " + status);
    }
  }

  @Override
  public void addCookie(Cookie cookie) {

  }

  @Override
  public boolean containsHeader(String name) {
    return false;
  }

  @Override
  public String encodeURL(String url) {
    return null;
  }

  @Override
  public String encodeRedirectURL(String url) {
    return null;
  }

  @Override
  public String encodeUrl(String url) {
    return null;
  }

  @Override
  public String encodeRedirectUrl(String url) {
    return null;
  }

  @Override
  public void sendError(int sc, String msg) throws IOException {

  }

  @Override
  public void sendError(int sc) throws IOException {

  }

  @Override
  public void sendRedirect(String location) throws IOException {
    //setAppCommitted(true);
    //sendRedirect(location, SC_FOUND);
  }

  @Override
  public void setDateHeader(String name, long date) {
    if (isCommitted())
      return;
    setHeader(name, format.format(new Date(date)));
  }

  @Override
  public void addDateHeader(String name, long date) {
    if (isCommitted())
      return;
    addHeader(name, format.format(new Date(date)));
  }

  @Override
  public void setHeader(String name, String value) {
    if (isCommitted())
      return;
    ArrayList values = new ArrayList();
    values.add(value);
    synchronized (headers) {
      headers.put(name, values);
    }
    String match = name.toLowerCase();
    if (match.equals("content-length")) {
      int contentLength = -1;
      try {
        contentLength = Integer.parseInt(value);
      }
      catch (NumberFormatException e) {
        ;
      }
      if (contentLength >= 0)
        setContentLength(contentLength);
    }
    else if (match.equals("content-type")) {
      setContentType(value);
    }
  }

  @Override
  public void addHeader(String name, String value) {
    if (isCommitted())
      return;
    synchronized (headers) {
      ArrayList values = (ArrayList) headers.get(name);
      if (values == null) {
        values = new ArrayList();
        headers.put(name, values);
      }
      values.add(value);
    }
  }

  @Override
  public void setIntHeader(String name, int value) {

  }

  @Override
  public void addIntHeader(String name, int value) {
    if (isCommitted())
      return;
    addHeader(name, "" + value);
  }

  @Override
  public void setStatus(int sc) {

  }

  @Override
  public void setStatus(int sc, String sm) {

  }

  @Override
  public int getStatus() {
    return 0;
  }

  @Override
  public String getHeader(String name) {
    return null;
  }

  @Override
  public Collection<String> getHeaders(String name) {
    Object header = this.headers.get(name);
    List<String> headers = Collections.emptyList();
    if (header != null) {
      headers.add(header.toString());
    }
    return headers;
  }

  @Override
  public Collection<String> getHeaderNames() {
    return null;
  }

  @Override
  public String getCharacterEncoding() {
    if (encoding == null)
      return ("UTF-8");
    else
      return (encoding);
  }

  public int getContentLength() {
    return contentLength;
  }

  @Override
  public String getContentType() {
    return contentType;
  }

  @Override
  public ServletOutputStream getOutputStream() throws IOException {
    return new NioResponseStream(this);
  }

  @Override
  public PrintWriter getWriter(){
    NioResponseStream newStream = new NioResponseStream(this);
    newStream.setCommit(false);
    try {
      OutputStreamWriter osr =
              new OutputStreamWriter(newStream, getCharacterEncoding());
      writer = new NioResponseWriter(osr,newStream);
      return writer;
    }catch (Exception e){
      log.info("{}",e);
    }
    return writer;
  }

  @Override
  public void setCharacterEncoding(String charset) {

  }

  @Override
  public void setContentLength(int length) {
    if (isCommitted())
      return;
    this.contentLength = length;
  }

  @Override
  public void setContentLengthLong(long len) {

  }

  @Override
  public void setContentType(String type) {

  }

  @Override
  public void setBufferSize(int size) {

  }

  @Override
  public int getBufferSize() {
    return 0;
  }

  @Override
  public void flushBuffer() throws IOException {
    //log.info("111111111111111111111111111111111111111");
    //StringBuffer stringBuffer = sendHeaders();
    //@Cleanup("flip") ByteBuffer head = ByteBuffer.wrap(stringBuffer.toString().getBytes());
    //@Cleanup SocketChannel channel = (SocketChannel) selectionKey.channel(); //  从契约获取通道
    //int body = channel.write(head);
  }

  @Override
  public void resetBuffer() {

  }

  @Override
  public boolean isCommitted() {
    return (committed);
  }

  @Override
  public void reset() {

  }

  @Override
  public void setLocale(Locale locale) {
    if (isCommitted())
      return;
    if(null != locale){
      String language = locale.getLanguage();
      if ((language != null) && (language.length() > 0)) {
        String country = locale.getCountry();
        StringBuffer value = new StringBuffer(language);
        if ((country != null) && (country.length() > 0)) {
          value.append('-');
          value.append(country);
        }
        setHeader("Content-Language", value.toString());
      }
    }
  }

  @Override
  public Locale getLocale() {
    return null;
  }

  /**
   * Send the HTTP response headers, if this has not already occurred.
   *  @throws IOException ioException
   */
  public StringBuffer sendHeaders(){
    if (isCommitted())
      return null;
    StringBuffer httpResponse = new StringBuffer();
    httpResponse.append(this.getProtocol());
    httpResponse.append(" ");
    httpResponse.append(status);
    if (message != null) {
      httpResponse.append(" ");
      httpResponse.append(message);
    }
    httpResponse.append("\r\n");

    if (getContentType() != null) {
      httpResponse.append("Content-Type: " + getContentType() + "\r\n");
    }
    if (getContentLength() >= 0) {
      httpResponse.append("Content-Length: " + getContentLength() + "\r\n");
    }
    // Send all specified headers (if any)
    synchronized (headers) {
      Iterator names = headers.keySet().iterator();
      while (names.hasNext()) {
        String name = (String) names.next();
        ArrayList values = (ArrayList) headers.get(name);
        Iterator items = values.iterator();
        while (items.hasNext()) {
          String value = (String) items.next();
          httpResponse.append(name);
          httpResponse.append(": ");
          httpResponse.append(value);
          httpResponse.append("\r\n");
        }
      }
    }
    httpResponse.append("\r\n");
    return httpResponse;
  }

  protected String getProtocol() {
    return request.getProtocol();
  }

  public void setRequest(NioHttpRequest request) {
    this.request = request;
  }

  public NioHttpRequest getRequest() {
    return request;
  }
}
