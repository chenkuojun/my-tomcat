package com.chenkuojun.mytomcat.connector.niohttp;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

/**
 * Convenience implementation of <b>ServletOutputStream</b> that works with
 * the standard ResponseBase implementation of <b>Response</b>.  If the content
 * length has been set on our associated Response, this implementation will
 * enforce not writing more than that many bytes on the underlying stream.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.6 $ $Date: 2002/03/18 07:15:39 $
 * @deprecated
 */

@Slf4j
public class NioResponseStream extends ServletOutputStream {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a servlet output stream associated with the specified Request.
     *
     * @param response The associated response
     */
    public NioResponseStream(NioHttpResponse response) {

        super();
        closed = false;
        commit = false;
        count = 0;
        this.response = response;
        this.selectionKey = response.getSelectionKey();

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
    protected NioHttpResponse response;

    protected SelectionKey selectionKey;


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
        //response.flushBuffer();
        closed = true;
    }


    /**
     * Flush any buffered data for this output stream, which also causes the
     * response to be committed.
     */
  public void flush() throws IOException {
    if (closed)
            throw new IOException("responseStream.flush.closed");
      commit = true;
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
        count++;

    }


    public void write(byte b[]) throws IOException {
        write(b, 0, b.length);

    }

    @Override
    public void write(byte b[], int off, int len) throws IOException {
        log.info("22222222222222222222222222222222222222222222");
        StringBuffer stringBuffer = this.response.sendHeaders();

        @Cleanup("flip") ByteBuffer head = ByteBuffer.wrap(stringBuffer.toString().getBytes());
        @Cleanup("flip") ByteBuffer bf = ByteBuffer.wrap(b);
        log.info("capcity:{}",bf.capacity());
        @Cleanup SocketChannel channel = (SocketChannel) selectionKey.channel(); //  从契约获取通道
        channel.close();
        if(channel.isConnected()){
            //向通道中写入数据
            int header = channel.write(head);
            log.info("header:{}",header);
            int body = channel.write(bf);
            log.info("body:{}",body);
            //if (write == -1) {
            //    selectionKey.cancel();
            //}
        }else {
            log.info("通道已经关闭了");
        }

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

