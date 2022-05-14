package com.chenkuojun.mytomcat.processor;

import com.chenkuojun.mytomcat.connector.niohttp.NioHttpRequest;
import com.chenkuojun.mytomcat.connector.niohttp.NioHttpResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
@Slf4j
public class NioHttpProcessor{
    private final Map<String, HttpServlet> servletMap;
    private Selector selector;
    private NioHttpRequest nioHttpRequest;

    public NioHttpProcessor(Selector selector, Map<String, HttpServlet> servletMap) {
        this.selector = selector;
        this.servletMap = servletMap;
    }
    @SneakyThrows
    public void run() {
        //6.干活
        ConcurrentLinkedQueue<NioHttpRequest> requestList = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<NioHttpResponse> responseList = new ConcurrentLinkedQueue<>();
        while (true) {
            selector.select(5000);
            //等待数据,没有人发送请求连接可以做其他事情,这里也是nio比普通bio的优势所在，简单来说不用一直等待连接，可以做其他的工作。
            //if(selector.select(5000) == 0){
            //    log.info("【my-tomcat-server】: 暂时没有连接请求，等待中....");
            //    continue;
            //}
            //获取监听器的所有事件并进行业务处理
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            //迭代处理
            while (iterator.hasNext()) {
                SelectionKey next = iterator.next();
                try {
                    //连接事件
                    if (next.isAcceptable()) {
                        //开启读取监听
                        doRead(next);
                    } else if (next.isValid() && next.isReadable()) { //可读事件
                        requestList.add(getRequest(next));
                        //切换可写的状态
                        next.interestOps(SelectionKey.OP_WRITE);
                    } else if (next.isValid() && next.isWritable()) { //可写事件
                        responseList.add(getResponse(next));
                        //切换可读的状态
                        next.interestOps(SelectionKey.OP_READ);
                    }
                    //等待请求和响应都准备好的时候进行处理
                    if (!requestList.isEmpty() && !responseList.isEmpty()) {
                        // 处理过程中，先取消selector对应连接的注册，避免重复
                        next.cancel();
                        // 动态 servlet 处理
                        HttpServlet httpServlet = servletMap.get("/");
                        httpServlet.service(requestList.poll(), responseList.poll());
                    }
                    //删除事件，免得后面会重复处理该事件
                    iterator.remove();
                }catch (Exception e){
                    log.info("{}",e);
                }
            }
            // 检查过程就绪,清除之前的调用效果
            selector.selectNow();
        }
    }

    /**
     * 开启读取监听
     *
     * @param next
     */
    private void doRead(SelectionKey next) {
        ServerSocketChannel channel = (ServerSocketChannel) next.channel();
        SocketChannel socketChannel;
        try {
            socketChannel = channel.accept();
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从通道中获取请求并进行包装我们自己的NioHttpRequest
     *
     * @param selectionKey
     * @return
     * @throws IOException
     */
    private NioHttpRequest getRequest(SelectionKey selectionKey) throws IOException, ServletException {
        this.nioHttpRequest = new NioHttpRequest(selectionKey);
        return nioHttpRequest;
    }

    /**
     * 从通道中获取响应并进行包装NioHttpResponse
     *
     * @param selectionKey
     * @return
     */
    private NioHttpResponse getResponse(SelectionKey selectionKey) {
        NioHttpResponse nioHttpResponse = new NioHttpResponse(selectionKey);
        nioHttpResponse.setRequest(nioHttpRequest);
        return nioHttpResponse;
    }
}
