package com.chenkuojun.mytomcat.connector.nettyhttp.utils;

import io.netty.channel.Channel;


/**
 * @author chenkuojun
 */
public class ChannelThreadLocal {

    public static final ThreadLocal<Channel> channelThreadLocal = new ThreadLocal<>();

    public static void set(Channel channel) {
        channelThreadLocal.set(channel);
    }

    public static void unset() {
        channelThreadLocal.remove();
    }

    public static Channel get() {
        return channelThreadLocal.get();
    }
}
