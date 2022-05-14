package com.chenkuojun.mytomcat.connector.nettyhttp.handler;

import com.chenkuojun.mytomcat.connector.nettyhttp.utils.ChannelThreadLocal;
import com.chenkuojun.mytomcat.init.dispatcher.MyRequestDispatcher;
import com.chenkuojun.mytomcat.connector.nettyhttp.http.NettyHttpRequest;
import com.chenkuojun.mytomcat.connector.nettyhttp.http.NettyHttpResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletContext;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@ChannelHandler.Sharable
@Slf4j
public class NettyRequestDispatcherHandler extends SimpleChannelInboundHandler<NettyHttpResponse> {
    private ServletContext servletContext;

    public NettyRequestDispatcherHandler(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NettyHttpResponse response1) throws Exception {
        // NettyHttpResponse1上一个处理器传递过来的对象
        NettyHttpRequest request = response1.getRequest();
        String requestURI = request.getRequestURI();
        try {
            MyRequestDispatcher dispatcher = (MyRequestDispatcher)
                    servletContext.getRequestDispatcher(requestURI);
            if (dispatcher == null) {
                sendError(ctx, HttpResponseStatus.BAD_GATEWAY);
                return;
            }
            dispatcher.dispatch(request, response1);
        }
        finally {
            ChannelThreadLocal.unset();
            if (!request.isAsyncStarted()) {
                try {
                    response1.getOutputStream().close();
                } catch (Exception e) {
                }
                try {
                    response1.getWriter().close();
                } catch (Exception e) {

                }
            }
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
        fullHttpResponse.headers().add(CONTENT_TYPE, "text/plain; charset=UTF-8");
        ctx.writeAndFlush(fullHttpResponse).addListener(ChannelFutureListener.CLOSE);
    }
}
