package com.example.nettydemo.demo5.server.handle;

import com.example.nettydemo.demo5.protocol.Packet;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.HashMap;
import java.util.Map;
import static com.example.nettydemo.demo5.protocol.command.Command.*;
@ChannelHandler.Sharable
public class IMCommandHandler extends SimpleChannelInboundHandler<Packet> {
    public static final IMCommandHandler INSTANCE = new IMCommandHandler();

    private Map<Byte, SimpleChannelInboundHandler<? extends Packet>> handlerMap;

    private IMCommandHandler(){
        handlerMap = new HashMap<>();
        handlerMap.put(MESSAGE_REQUEST,MessageRequestHandler.INSTANCE);
        handlerMap.put(CREATE_GROUP_REQUEST,CreateGroupRequestHandler.INSTANCE);
        handlerMap.put(JOIN_GROUP_REQUEST,JoinGroupRequestHandler.INSTANCE);
        handlerMap.put(QUIT_GROUP_REQUEST,QuitGroupRequestHandler.INSTANCE);
        handlerMap.put(LIST_GROUP_MEMBERS_REQUEST,ListGroupMembersRequestHandler.INSTANCE);
        handlerMap.put(GROUP_MESSAGE_REQUEST,GroupMessageRequestHandler.INSTANCE);
        handlerMap.put(LOGOUT_REQUEST, LogoutRequestHandler.INSTANCE);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) throws Exception {
        handlerMap.get(packet.getCommand()).channelRead(ctx, packet);
    }

}
