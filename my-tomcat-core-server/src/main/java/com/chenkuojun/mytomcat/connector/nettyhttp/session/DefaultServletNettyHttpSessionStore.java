package com.chenkuojun.mytomcat.connector.nettyhttp.session;

import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author chenkuojun
 */
@Slf4j
public class DefaultServletNettyHttpSessionStore implements ServletNettyHttpSessionStore {

    public static ConcurrentHashMap<String, NettyHttpSession> sessions = new ConcurrentHashMap<String, NettyHttpSession>();

    @Override
    public NettyHttpSession createSession() {
        String sessionId = this.generateNewSessionId();
        log.debug("Creating new session with id {}", sessionId);

        NettyHttpSession session = new NettyHttpSession(sessionId);
        sessions.put(sessionId, session);
        return session;
    }

    @Override
    public void destroySession(String sessionId) {
        log.debug("Destroying session with id {}", sessionId);
        sessions.remove(sessionId);
    }

    @Override
    public NettyHttpSession findSession(String sessionId) {
        if (sessionId == null) return null;
        return sessions.get(sessionId);
    }

    protected String generateNewSessionId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public void destroyInactiveSessions() {
        for (Map.Entry<String, NettyHttpSession> entry : sessions.entrySet()) {
            NettyHttpSession session = entry.getValue();
            if (session.getMaxInactiveInterval() < 0) continue;

            long currentMillis = System.currentTimeMillis();

            if (currentMillis - session.getLastAccessedTime() > session.getMaxInactiveInterval() * 1000) {
                destroySession(entry.getKey());
            }
        }
    }
}
