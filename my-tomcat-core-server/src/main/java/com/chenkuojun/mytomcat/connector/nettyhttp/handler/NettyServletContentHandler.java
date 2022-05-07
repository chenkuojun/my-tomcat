package com.chenkuojun.mytomcat.connector.nettyhttp.handler;

import com.chenkuojun.mytomcat.connector.nettyhttp.http.NettyHttpRequest;
import com.chenkuojun.mytomcat.connector.nettyhttp.http.NettyHttpResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import java.net.InetSocketAddress;
import java.util.Optional;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @author chenkuojun
 */
@ChannelHandler.Sharable
@Slf4j
public class NettyServletContentHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        if (request != null) {
            //X-Real-IP
            Optional<String> option = Optional.ofNullable(request.headers().get("X-Forwarded-For"));
            String real = option.orElseGet(() -> request.headers().get("X-Real-IP"));
            //X-Forwarded-For
            if (StringUtils.isBlank(real)) {
                InetSocketAddress socketAddr = (InetSocketAddress) ctx.channel().remoteAddress();
                real = socketAddr.getAddress().getHostAddress();
            }
            /**
             * 如果发送生错误
             */
            if (!request.decoderResult().isSuccess()) {
                sendError(ctx, HttpResponseStatus.BAD_GATEWAY);
                return;
            }

            HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, true);
            // 获取request
            NettyHttpRequest request1 = new NettyHttpRequest(request);
            // 获取response
            NettyHttpResponse response1 = new NettyHttpResponse(request1,ctx,response);
            //// 动态 servlet 处理
            //HttpServlet httpServlet = servletMap.get("/");
            //httpServlet.service(request1, response1);
            // 将处理好的response转发到下一个处理器处理,其实就是两个处理器之间传递对象
            ctx.fireChannelRead(response1);
            // 请求结束,输出内容到页面，因为netty所有的操作都是异步的，所以可能会出现数据没有写出之前就关闭了连接
            //Channel ch = ...;
            //ch.writeAndFlush(message);
            //ch.close();
            // 添加一个监听器，数据写出结束就关闭连接channel ChannelFuture channelFuture = ctx.writeAndFlush(response1);
            // 返回的是一个future
            //ctx.writeAndFlush(response1).addListener(ChannelFutureListener.CLOSE);
            //if (request != null) {
            //    inputStream.addContent(request);
            //}
        }
    }

    /**
     * 发送错误信息
     *
     * @param ctx
     * @param status
     */
    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        ByteBuf content = Unpooled.copiedBuffer(
                "Failure: " + status.toString() + "\r\n",
                CharsetUtil.UTF_8);
        FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(
                HTTP_1_1,
                status,
                content
        );
        fullHttpResponse.headers().add(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        ctx.writeAndFlush(fullHttpResponse).addListener(ChannelFutureListener.CLOSE);
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
