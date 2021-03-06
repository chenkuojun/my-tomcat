package com.chenkuojun.mytomcat.connector;

import com.chenkuojun.mytomcat.connector.http.HttpResponse;
import com.chenkuojun.mytomcat.utils.HttpProtocolUtil;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;


@Slf4j
public class ResponseStream extends ServletOutputStream {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a servlet output stream associated with the specified Request.
     *
     * @param response The associated response
     */
    public ResponseStream(HttpResponse response) {

        super();
        closed = false;
        commit = false;
        count = 0;
        this.response = response;
        this.stream = response.getStream();

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * Has this stream been closed?
     */
    protected boolean closed = false;


    /**
     * Should we commit the response when we are flushed?
     */
    protected boolean commit = false;


    /**
     * The number of bytes which have already been written to this stream.
     */
    protected int count = 0;


    /**
     * The content length past which we will not write, or -1 if there is
     * no defined content length.
     */
    protected int length = -1;


    /**
     * The Response with which this input stream is associated.
     */
    protected HttpResponse response = null;


    /**
     * The underlying output stream to which we should write data.
     */
    protected OutputStream stream;


    // ------------------------------------------------------------- Properties

    /**
     * [Package Private] Return the "commit response on flush" flag.
     * @return boolean
     */
    public boolean getCommit() {

        return (this.commit);

    }


    /**
     * [Package Private] Set the "commit response on flush" flag.
     *
     * @param commit The new commit flag
     */
    public void setCommit(boolean commit) {

        this.commit = commit;

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Close this output stream, causing any buffered data to be flushed and
     * any further output data to throw an IOException.
     */
    public void close() throws IOException {
        if (closed)
            throw new IOException("responseStream.close.closed");
        response.flushBuffer();
        closed = true;
    }


    /**
     * Flush any buffered data for this output stream, which also causes the
     * response to be committed.
     */
  public void flush() throws IOException {
    if (closed)
            throw new IOException("responseStream.flush.closed");
       if (commit)
            response.flushBuffer();

    }


    /**
     * Write the specified byte to our output stream.
     *
     * @param b The byte to be written
     *
     * @exception IOException if an input/output error occurs
     */
    public void write(int b) throws IOException {

        if (closed)
            throw new IOException("responseStream.write.closed");

        if ((length > 0) && (count >= length))
            throw new IOException("responseStream.write.count");

        stream.write(b);
        count++;

    }


    public void write(byte b[]) throws IOException {
        write(b, 0, b.length);

    }

    @Override
    public void write(byte b[], int off, int len) throws IOException {
        //sb.append(b);
        //log.info("{}",getChars(b));
        this.response.sendHeaders();
        //stream.write(HttpProtocolUtil.getHttp200(len).getBytes());
        stream.write(b, off, len);


//        if (closed)
//            throw new IOException("responseStream.write.closed");
//
//        int actual = len;
//        if ((length > 0) && ((count + len) >= length))
//            actual = length - count;
//        response.write(b, off, actual);
//        count += actual;
//        if (actual < len)
//            throw new IOException("responseStream.write.count");

    }


    // -------------------------------------------------------- Package Methods


    /**
     * Has this response stream been closed?
     */
    boolean closed() {
        return (this.closed);

    }


    /**
     * Reset the count of bytes written to this stream to zero.
     */
    void reset() {

        count = 0;

    }


    @Override
    public boolean isReady() {
        return false;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {

    }

    public byte[] getBytes(char[] chars) {
        Charset cs = Charset.forName("UTF-8");
        CharBuffer cb = CharBuffer.allocate(chars.length);
        cb.put(chars);
        cb.flip();
        ByteBuffer bb = cs.encode(cb);
        return bb.array();
    }
    public char[] getChars(byte[] bytes) {
        Charset cs = Charset.forName("UTF-8");
        ByteBuffer bb = ByteBuffer.allocate(bytes.length);
        bb.put(bytes);
        bb.flip();
        CharBuffer cb = cs.decode(bb);
        return cb.array();
    }
}

