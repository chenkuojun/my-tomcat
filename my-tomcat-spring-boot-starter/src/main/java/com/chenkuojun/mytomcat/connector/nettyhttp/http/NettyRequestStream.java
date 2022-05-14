package com.chenkuojun.mytomcat.connector.nettyhttp.http;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import java.io.IOException;
import java.io.InputStream;

public class NettyRequestStream extends ServletInputStream {

    public NettyRequestStream(NettyHttpRequest request) {

        super();
        closed = false;
        count = 0;
        length = request.getContentLength();
        stream = request.getStream();

    }

    protected boolean closed = false;
    protected int count = 0;
    protected int length = -1;
    protected InputStream stream = null;

    public void close() throws IOException {

        if (closed)
            throw new IOException("Request stream has already been closed");

        if (length > 0) {
            while (count < length) {
                int b = read();
                if (b < 0)
                    break;
            }
        }

        closed = true;

    }

    public int read() throws IOException {

        // Has this stream been closed?
        if (closed)
            throw new IOException("Unable to read from a closed stream");

        // Have we read the specified content length already?
        if ((length >= 0) && (count >= length))
            return (-1);        // End of file indicator

        // Read and count the next byte, then return it
        int b = stream.read();
        if (b >= 0)
            count++;
        return (b);

    }

    public int read(byte b[]) throws IOException {

        return (read(b, 0, b.length));

    }


    public int read(byte b[], int off, int len) throws IOException {

        int toRead = len;
        if (length > 0) {
            if (count >= length)
                return (-1);
            if ((count + len) > length)
                toRead = length - count;
        }
        int actuallyRead = super.read(b, off, toRead);
        return (actuallyRead);

    }


    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public boolean isReady() {
        return false;
    }

    @Override
    public void setReadListener(ReadListener readListener) {

    }
}
