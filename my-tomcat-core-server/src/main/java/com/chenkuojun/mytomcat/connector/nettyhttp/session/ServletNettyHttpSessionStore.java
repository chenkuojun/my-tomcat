package com.chenkuojun.mytomcat.connector.nettyhttp.session;



/**
 * @author chenkuojun
 */
public interface ServletNettyHttpSessionStore {

    NettyHttpSession findSession(String sessionId);

    NettyHttpSession createSession();

    void destroySession(String sessionId);

    void destroyInactiveSessions();
}
