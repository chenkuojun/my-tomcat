package com.chenkuojun.mytomcat.connector;

import com.chenkuojun.mytomcat.connector.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.Writer;

@Slf4j
public class OutputBuffer extends Writer {


    private HttpResponse response;


    /**
     * Number of bytes written.
     */
    private StringBuffer sb = new StringBuffer();


    /**
     * Flag which indicates if the output buffer is closed.
     */
    private volatile boolean closed = false;


    /**
     * Suspended flag. All output bytes will be swallowed if this is true.
     */
    private volatile boolean suspended = false;


    @Override
    public void write(char c[], int off, int len) throws IOException {
        if (c == null) {
            throw new NullPointerException("outputBuffer.writeNull");
        }
        sb.append(c);
    }


    /**
     * Append a string to the buffer
     */
    @Override
    public void write(String s, int off, int len) throws IOException {

        if (s == null) {
            throw new NullPointerException("outputBuffer.writeNull");
        }
        sb.append(s);
    }

    @Override
    public void flush() throws IOException {
        log.info("flush 开始输出");
        response.write(sb.toString().getBytes());
    }

    @Override
    public void close() throws IOException {
        System.out.println("close()");
    }
}
