package com.chenkuojun.mytomcat.connector.http;

import com.chenkuojun.mytomcat.connector.CharChunk;
import com.chenkuojun.mytomcat.connector.OutputBuffer;
import com.chenkuojun.mytomcat.connector.ResponseStream;
import com.chenkuojun.mytomcat.connector.ResponseWriter;
import com.chenkuojun.mytomcat.constant.Constants;
import com.chenkuojun.mytomcat.utils.CookieTools;
import com.chenkuojun.mytomcat.utils.UriUtil;
import lombok.extern.slf4j.Slf4j;

import javax.naming.Context;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class HttpResponse implements HttpServletResponse {

  // the default buffer size
  private static final int BUFFER_SIZE = 1024;
  HttpRequest request;
  OutputStream output;
  PrintWriter writer;
  protected byte[] buffer = new byte[BUFFER_SIZE];
  protected int bufferCount = 0;
  /**
   * Has this response been committed yet?
   */
  protected boolean committed = false;
  /**
   * The actual number of bytes written to this Response.
   */
  protected int contentCount = 0;
  /**
   * The content length associated with this Response.
   */
  protected int contentLength = -1;
  /**
   * The content type associated with this Response.
   */
  protected String contentType = null;
  /**
   * The character encoding associated with this Response.
   */
  protected String encoding = null;

  /**
   * The set of Cookies associated with this Response.
   */
  protected ArrayList cookies = new ArrayList();
  /**
   * The HTTP headers explicitly added via addHeader(), but not including
   * those to be added with setContentLength(), setContentType(), and so on.
   * This collection is keyed by the header name, and the elements are
   * ArrayLists containing the associated values that have been set.
   */
  protected HashMap headers = new HashMap();
  /**
   * The date format we will use for creating date headers.
   */
  protected final SimpleDateFormat format =
    new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz",Locale.US);
  /**
   * The error message set by <code>sendError()</code>.
   */
  protected String message = getStatusMessage(HttpServletResponse.SC_OK);
  /**
   * The HTTP status code associated with this Response.
   */
  protected int status = HttpServletResponse.SC_OK;

  protected static final int SC_FOUND = 302;


  /**
   * Recyclable buffer to hold the redirect URL.
   */
  protected final CharChunk redirectURLCC = new CharChunk();

  /**
   * The included flag.
   */
  protected boolean included = false;

  protected boolean appCommitted = false;

  public void setAppCommitted(boolean appCommitted) {
    this.appCommitted = appCommitted;
  }

  public boolean isAppCommitted() {
    return this.appCommitted || isCommitted() || ((getContentLength() > 0));
  }



  public HttpResponse(OutputStream output) {
    this.output = output;
  }

  /**
   * call this method to send headers and response to the output
   */
  public void finishResponse() {
    // sendHeaders();
    // Flush and close the appropriate output mechanism
    if (writer != null) {
      writer.flush();
      writer.close();
    }
  }

  public int getContentLength() {
    return contentLength;
  }

  public String getContentType() {
    return contentType;
  }


  protected String getProtocol() {
    return request.getProtocol();
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

  public OutputStream getStream() {
    return this.output;
  }
  /**
   * Send the HTTP response headers, if this has not already occurred.
   *  @throws IOException ioException
   */
  public void sendHeaders() throws IOException {
    if (isCommitted())
      return;
    // Prepare a suitable output writer
    OutputStreamWriter osr = null;
    try {
      osr = new OutputStreamWriter(getStream(), getCharacterEncoding());
    }
    catch (UnsupportedEncodingException e) {
      osr = new OutputStreamWriter(getStream());
    }
    final PrintWriter outputWriter = new PrintWriter(osr);
    // Send the "Status:" header
    outputWriter.print(this.getProtocol());
    outputWriter.print(" ");
    outputWriter.print(status);
    if (message != null) {
      outputWriter.print(" ");
      outputWriter.print(message);
    }
    outputWriter.print("\r\n");
    // Send the content-length and content-type headers (if any)
    if (getContentType() != null) {
      outputWriter.print("Content-Type: " + getContentType() + "\r\n");
    }
    if (getContentLength() >= 0) {
      outputWriter.print("Content-Length: " + getContentLength() + "\r\n");
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
          outputWriter.print(name);
          outputWriter.print(": ");
          outputWriter.print(value);
          outputWriter.print("\r\n");
        }
      }
    }
    // Send all specified cookies (if any)
    synchronized (cookies) {
      Iterator items = cookies.iterator();
      while (items.hasNext()) {
        Cookie cookie = (Cookie) items.next();
        outputWriter.print(CookieTools.getCookieHeaderName(cookie));
        outputWriter.print(": ");
        outputWriter.print(CookieTools.getCookieHeaderValue(cookie));
        outputWriter.print("\r\n");
      }
    }

    // Send a terminating blank line to mark the end of the headers
    outputWriter.print("\r\n");
    outputWriter.flush();

    committed = true;
  }

  public void setRequest(HttpRequest request) {
    this.request = request;
  }

  /* This method is used to serve a static page */
  public void sendStaticResource() throws IOException {
    byte[] bytes = new byte[BUFFER_SIZE];
    FileInputStream fis = null;
    try {
      /* request.getUri has been replaced by request.getRequestURI */
      File file = new File(Constants.WEB_ROOT + Constants.STATIC_PATH, request.getRequestURI());
      fis = new FileInputStream(file);
      // 请求头一定要添加，不然浏览器加收不到响应
      // PrintStream 用来操作字节流，PrintWriter 用来操作字符流  此处有图片，所以用字节流处理，网上有说可以PrintWriter也可以，回头研究
      PrintStream printStream = new PrintStream(output);
      printStream.println("HTTP/1.0 200 OK");// 返回应答消息,并结束应答
      printStream.println("Content-Length:" + file.length());// 返回内容字节数
      printStream.println();// 根据 HTTP 协议, 空行将结束头信息
      int ch = fis.read(bytes, 0, BUFFER_SIZE);
      while (ch!=-1) {
        printStream.write(bytes);
        ch = fis.read(bytes, 0, BUFFER_SIZE);
      }
      printStream.close();
    }catch (FileNotFoundException e) {

      String errorMessage = "HTTP/1.1 404 File Not Found\r\n" +
        "Content-Type: text/html\r\n" +
        "Content-Length: 23\r\n" +
        "\r\n" +
        "<h1>File Not Found</h1>";
      output.write(errorMessage.getBytes());
    }
    finally {
      if (fis!=null)
        fis.close();
    }
  }

  public void write(int b) throws IOException {
    if (bufferCount >= buffer.length)
      flushBuffer();
    buffer[bufferCount++] = (byte) b;
    contentCount++;
  }

  public void write(byte b[]) throws IOException {
    write(b, 0, b.length);
  }

  public void write(byte b[], int off, int len) throws IOException {
    // If the whole thing fits in the buffer, just put it there
    if (len == 0)
      return;
    if (len <= (buffer.length - bufferCount)) {
      System.arraycopy(b, off, buffer, bufferCount, len);
      bufferCount += len;
      contentCount += len;
      return;
    }

    // Flush the buffer and start writing full-buffer-size chunks
    flushBuffer();
    int iterations = len / buffer.length;
    int leftoverStart = iterations * buffer.length;
    int leftoverLen = len - leftoverStart;
    for (int i = 0; i < iterations; i++)
      write(b, off + (i * buffer.length), buffer.length);

    // Write the remainder (guaranteed to fit in the buffer)
    if (leftoverLen > 0)
      write(b, off + leftoverStart, leftoverLen);
  }

  /** implementation of HttpServletResponse  */

  public void addCookie(Cookie cookie) {
    if (isCommitted())
      return;
  //  if (included)
    //        return;     // Ignore any call from an included servlet
    synchronized (cookies) {
      cookies.add(cookie);
    }
  }

  public void addDateHeader(String name, long value) {
    if (isCommitted())
      return;
//    if (included)
  //          return;     // Ignore any call from an included servlet
    addHeader(name, format.format(new Date(value)));
  }

  public void addHeader(String name, String value) {
    if (isCommitted())
      return;
//        if (included)
  //          return;     // Ignore any call from an included servlet
    synchronized (headers) {
      ArrayList values = (ArrayList) headers.get(name);
      if (values == null) {
        values = new ArrayList();
        headers.put(name, values);
      }

      values.add(value);
    }
  }

  public void addIntHeader(String name, int value) {
    if (isCommitted())
      return;
//    if (included)
  //    return;     // Ignore any call from an included servlet
    addHeader(name, "" + value);
  }

  public boolean containsHeader(String name) {
    synchronized (headers) {
      return (headers.get(name)!=null);
    }
  }

  public String encodeRedirectURL(String url) {
    return null;
  }

  public String encodeRedirectUrl(String url) {
    return encodeRedirectURL(url);
  }

  public String encodeUrl(String url) {
    return encodeURL(url);
  }

  public String encodeURL(String url) {
    return null;
  }

  public void flushBuffer() throws IOException {
    //committed = true;
    if (bufferCount > 0) {
      try {
        output.write(buffer, 0, bufferCount);
      }
      finally {
        bufferCount = 0;
      }
    }
  }

  public int getBufferSize() {
    return 0;
  }

  public String getCharacterEncoding() {
    if (encoding == null)
      return ("UTF-8");
    else
      return (encoding);
  }

  public Locale getLocale() {
    return Locale.SIMPLIFIED_CHINESE;
  }

  public ServletOutputStream getOutputStream() throws IOException {
    return new ResponseStream(this);
  }

  public PrintWriter getWriter() throws IOException {
    ResponseStream newStream = new ResponseStream(this);
    newStream.setCommit(false);
    OutputStreamWriter osr =
      new OutputStreamWriter(newStream, getCharacterEncoding());
    //OutputBuffer outputBuffer = new OutputBuffer();
    writer = new ResponseWriter(osr,newStream);
    return writer;
  }

  @Override
  public void setCharacterEncoding(String s) {

  }

  /**
   * Has the output of this response already been committed?
   */
  public boolean isCommitted() {
    return (committed);
  }

  public void reset() {
  }

  public void resetBuffer() {
  }

  public void sendError(int sc) throws IOException {
  }

  public void sendError(int sc, String message) throws IOException {
  }

  public void sendRedirect(String location) throws IOException {
    setAppCommitted(true);
    sendRedirect(location, SC_FOUND);
  }

  /**
   * Internal method that allows a redirect to be sent with a status other
   * than {@link HttpServletResponse#SC_FOUND} (302). No attempt is made to
   * validate the status code.
   * @param location location
   * @param status status
   */
  public void sendRedirect(String location, int status) {
    if (isCommitted()) {
      throw new IllegalStateException("coyoteResponse.sendRedirect.ise");
    }
    if (included) {
      return;
    }
    String locationUri = toAbsolute(location);
    setStatus(status);
    setHeader("Location", locationUri);
    this.committed = true;
  }

  public void setBufferSize(int size) {
  }

  public void setContentLength(int length) {
    if (isCommitted())
      return;
//    if (included)
  //     return;     // Ignore any call from an included servlet
    this.contentLength = length;
  }

  @Override
  public void setContentLengthLong(long l) {

  }

  public void setContentType(String type) {
  }

  public void setDateHeader(String name, long value) {
    if (isCommitted())
      return;
//    if (included)
  //    return;     // Ignore any call from an included servlet
    setHeader(name, format.format(new Date(value)));
  }

  public void setHeader(String name, String value) {
    if (isCommitted())
      return;
//    if (included)
  //    return;     // Ignore any call from an included servlet
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

  public void setIntHeader(String name, int value) {
    if (isCommitted())
      return;
    //if (included)
      //return;     // Ignore any call from an included servlet
    setHeader(name, "" + value);
  }

  public void setLocale(Locale locale) {
    if (isCommitted())
      return;
    //if (included)
      //return;     // Ignore any call from an included servlet

   // super.setLocale(locale);
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

  public void setStatus(int sc) {
    setStatus(sc, null);
  }

  public void setStatus(int sc, String message) {
    if (isCommitted()) {
      return;
    }

    // Ignore any call from an included servlet
    if (included) {
      return;
    }
    this.status = sc;
    this.message = message;
  }

  @Override
  public int getStatus() {
    return this.status;
  }

  @Override
  public String getHeader(String s) {
    if(this.headers.get(s) !=null){
      String s1 = this.headers.get(s).toString();
      String substring = s1.substring(1);
      String result = substring.substring(0, substring.length() - 1);
      return result;
    }
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


  /**
   * Convert (if necessary) and return the absolute URL that represents the
   * resource referenced by this possibly relative URL.  If this URL is
   * already absolute, return it unchanged.
   *
   * @param location URL to be (possibly) converted and then returned
   * @return the encoded URL
   *
   * @exception IllegalArgumentException if a MalformedURLException is
   *  thrown when converting the relative URL to an absolute one
   */
  protected String toAbsolute(String location) {

    if (location == null) {
      return location;
    }

    boolean leadingSlash = location.startsWith("/");
  //  StringBuffer sb = new StringBuffer();
  //  if (location.startsWith("//")) {
  //    // Add the scheme
  //    String scheme = request.getScheme();
  //    sb.append(scheme, 0, scheme.length());
  //    sb.append(':');
  //    sb.append(location, 0, location.length());
  //    return sb.toString();
  //  } else if (leadingSlash) {
  //    String host = request.getHeader("host");
  //    sb.append("http://", 0, "http://".length());
  //    sb.append(host, 0, host.length());
  //    sb.append(location, 0, location.length());
  //    return sb.toString();
  //
  //  } else {
  //
  //    return location;
  //
  //  }
  //
  //}

    if (location.startsWith("//")) {
      // Scheme relative
      redirectURLCC.recycle();
      // Add the scheme
      String scheme = request.getScheme();
      try {
        redirectURLCC.append(scheme, 0, scheme.length());
        redirectURLCC.append(':');
        redirectURLCC.append(location, 0, location.length());
        return redirectURLCC.toString();
      } catch (IOException e) {
        throw new IllegalArgumentException(location, e);
      }

    } else if (leadingSlash || !UriUtil.hasScheme(location)) {

      redirectURLCC.recycle();

      int port = 8090;
      String host = request.getHeader("host");
      try {

        redirectURLCC.append("http://", 0, "http://".length());
        redirectURLCC.append(host, 0, host.length());
        //if (port != 80 || port != 443) {
        //  redirectURLCC.append(':');
        //  String portS = port + "";
        //  redirectURLCC.append(portS, 0, portS.length());
        //}
        //if (!leadingSlash) {
        //  String relativePath = request.getDecodedRequestURI();
        //  int pos = relativePath.lastIndexOf('/');
        //  CharChunk encodedURI = null;
        //  if (SecurityUtil.isPackageProtectionEnabled() ){
        //    try{
        //      encodedURI = AccessController.doPrivileged(
        //              new PrivilegedEncodeUrl(urlEncoder, relativePath, pos));
        //    } catch (PrivilegedActionException pae){
        //      throw new IllegalArgumentException(location, pae.getException());
        //    }
        //  } else {
        //    encodedURI = urlEncoder.encodeURL(relativePath, 0, pos);
        //  }
        //  redirectURLCC.append(encodedURI);
        //  encodedURI.recycle();
        //  redirectURLCC.append('/');
        //}
        redirectURLCC.append(location, 0, location.length());

        normalize(redirectURLCC);
      } catch (IOException e) {
        throw new IllegalArgumentException(location, e);
      }

      return redirectURLCC.toString();

    } else {

      return location;

    }
  }


  /**
   * Removes /./ and /../ sequences from absolute URLs.
   * Code borrowed heavily from CoyoteAdapter.normalize()
   *
   * @param cc the char chunk containing the chars to normalize
   */
  private void normalize(CharChunk cc) {
    // Strip query string and/or fragment first as doing it this way makes
    // the normalization logic a lot simpler
    int truncate = cc.indexOf('?');
    if (truncate == -1) {
      truncate = cc.indexOf('#');
    }
    char[] truncateCC = null;
    if (truncate > -1) {
      truncateCC = Arrays.copyOfRange(cc.getBuffer(),
              cc.getStart() + truncate, cc.getEnd());
      cc.setEnd(cc.getStart() + truncate);
    }

    if (cc.endsWith("/.") || cc.endsWith("/..")) {
      try {
        cc.append('/');
      } catch (IOException e) {
        throw new IllegalArgumentException(cc.toString(), e);
      }
    }

    char[] c = cc.getChars();
    int start = cc.getStart();
    int end = cc.getEnd();
    int index = 0;
    int startIndex = 0;

    // Advance past the first three / characters (should place index just
    // scheme://host[:port]

    for (int i = 0; i < 3; i++) {
      startIndex = cc.indexOf('/', startIndex + 1);
    }

    // Remove /./
    index = startIndex;
    while (true) {
      index = cc.indexOf("/./", 0, 3, index);
      if (index < 0) {
        break;
      }
      copyChars(c, start + index, start + index + 2,
              end - start - index - 2);
      end = end - 2;
      cc.setEnd(end);
    }

    // Remove /../
    index = startIndex;
    int pos;
    while (true) {
      index = cc.indexOf("/../", 0, 4, index);
      if (index < 0) {
        break;
      }
      // Can't go above the server root
      if (index == startIndex) {
        throw new IllegalArgumentException();
      }
      int index2 = -1;
      for (pos = start + index - 1; (pos >= 0) && (index2 < 0); pos --) {
        if (c[pos] == (byte) '/') {
          index2 = pos;
        }
      }
      copyChars(c, start + index2, start + index + 3,
              end - start - index - 3);
      end = end + index2 - index - 3;
      cc.setEnd(end);
      index = index2;
    }

    // Add the query string and/or fragment (if present) back in
    if (truncateCC != null) {
      try {
        cc.append(truncateCC, 0, truncateCC.length);
      } catch (IOException ioe) {
        throw new IllegalArgumentException(ioe);
      }
    }
  }

  private void copyChars(char[] c, int dest, int src, int len) {
    System.arraycopy(c, src, c, dest, len);
  }


  public void setCommitted(boolean committed) {

    this.committed = committed;

  }
}
