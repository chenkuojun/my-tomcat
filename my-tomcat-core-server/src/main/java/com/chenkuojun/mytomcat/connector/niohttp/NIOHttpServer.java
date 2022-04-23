package com.chenkuojun.mytomcat.connector.niohttp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class NIOHttpServer {
    public static void main(String[] args) throws IOException {
        // 1.ServerSocketChannel 绑定端口
        ServerSocketChannel socket = ServerSocketChannel.open();
        socket.configureBlocking(false); // no-Blocking
        socket.bind(new InetSocketAddress(8080));
        System.out.println("NIO服务器启动，端口："+8080);
        // 2.获取新连接
        // selector 获取不同操作系统下不同的tcp连接动态
        Selector selector = Selector.open();
        // 选择器，根据条件查询符合情况地TCP连接
        socket.register(selector, SelectionKey.OP_ACCEPT);
        while(true){
            selector.select(1000); //如果没有新连接，就等待
            // 3. 处理查询结果
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();
            while(iterator.hasNext()){
                SelectionKey result = iterator.next();
                //根据不同的类型，分别进行处理
                if(result.isAcceptable()){
                    //3.1 拿到新连接对象
                    // nio体现，accept 不阻塞，没有连接则返回null
                    SocketChannel accept = socket.accept();
                    if(accept!=null){
                        // 注册连接对象，进行关注
                        accept.configureBlocking(false);// no-Blocking
                        accept.register(selector, SelectionKey.OP_READ);
                    }
                }
                if(result.isReadable()){
                    //3.2 有数据请求的连接
                    SocketChannel channel = (SocketChannel) result.channel();
                    // 处理过程中，先取消selector对应连接的注册，避免重复
                    result.cancel();
                    // NIO的读写方式： 字节缓冲区
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                    channel.read(byteBuffer);
                    byteBuffer.flip(); //模式转换
                    byte[] b = byteBuffer.array();
                    String request = new String(b);
                    //处理请求...
                    System.out.println(request);
                    //数据响应：NIO的写数据
                    String response = "HTTP/1.1 200 ok\r\nContent-Length: 11\r\n\r\nHello World\r\n";
                    channel.write(ByteBuffer.wrap(response.getBytes()));
                    // 处理完成，重新注册，继续接收处理新的连接
                     // channel.register(selector, SelectionKey.OP_READ);
                }
                // 删除处理过的结果(事件)
                iterator.remove();
            }
            // 检查过程就绪,清除之前的调用效果
            selector.selectNow();
        }
    }

}
