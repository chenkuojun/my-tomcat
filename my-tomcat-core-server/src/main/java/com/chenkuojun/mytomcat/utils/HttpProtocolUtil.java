package com.chenkuojun.mytomcat.utils;

/**
 * Http protocol tool classes.
 *
 * @author wangguangwu
 */
public class HttpProtocolUtil {

    /**
     * provide response header information for response with response code 200.
     *
     * @param contentLength responseBody length
     * @return string
     */
    public static String getHttp200(long contentLength) {
        return "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html\r\n" +
                "Content-Length: " + contentLength + "\r\n" +
                "\r\n";
    }

    /**
     * provide response header information for response with response code 404.
     *
     * @return string
     */
    public static String getHttp404() {
        String str404 = "<h1>404 Not Found</h1>";
        return "HTTP/1.1 404 Not Found\r\n" +
                "Content-Type: text/html\r\n" +
                "Content-Length: " + str404.getBytes().length + "\r\n" +
                "\r\n" + str404;
    }

}
