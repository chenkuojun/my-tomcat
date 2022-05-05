package com.chenkuojun.mytomcat.connector.nettyhttp1.handler;

import com.chenkuojun.mytomcat.connector.nettyhttp.context.NettyEmbeddedContext;
import com.chenkuojun.mytomcat.connector.nettyhttp.http.HttpContentInputStream;
import com.chenkuojun.mytomcat.connector.nettyhttp.http.NettyHttpServletRequest;
import com.chenkuojun.mytomcat.connector.nettyhttp.http.NettyHttpServletResponse;
import com.chenkuojun.mytomcat.connector.nettyhttp1.http.NettyHttpServletRequest1;
import com.chenkuojun.mytomcat.connector.nettyhttp1.http.NettyHttpServletResponse1;
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

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @author chenkuojun
 */
@ChannelHandler.Sharable
@Slf4j
public class NettyServletContentHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    //private final NettyEmbeddedContext servletContext;
    private HttpContentInputStream inputStream;

    //public NettyServletContentHandler(NettyEmbeddedContext servletContext) {
    //    this.servletContext = servletContext;
    //}

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        inputStream = new HttpContentInputStream(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        if (request != null) {
            log.info("{}",request);
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
            NettyHttpServletResponse1 servletResponse = new NettyHttpServletResponse1(ctx, response);
            NettyHttpServletRequest servletRequest = new NettyHttpServletRequest1(ctx, request,
                    servletResponse, inputStream);
            //if (HttpHeaders.is100ContinueExpected(request)) {
            //    ctx.write(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE),
            //            ctx.voidPromise());
            //}
            //ctx.fireChannelRead(servletRequest);
        }
        if (request != null) {
            inputStream.addContent(request);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        inputStream.close();
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
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
