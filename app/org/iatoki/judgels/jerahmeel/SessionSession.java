package org.iatoki.judgels.jerahmeel;

public final class SessionSession {

    private final long id;
    private final String sessionJid;
    private final String dependedSessionJid;
    private final String dependedSessionName;

    public SessionSession(long id, String sessionJid, String dependedSessionJid, String dependedSessionName) {
        this.id = id;
        this.sessionJid = sessionJid;
        this.dependedSessionJid = dependedSessionJid;
        this.dependedSessionName = dependedSessionName;
    }

    public long getId() {
        return id;
    }

    public String getSessionJid() {
        return sessionJid;
    }

    public String getDependedSessionJid() {
        return dependedSessionJid;
    }

    public String getDependedSessionName() {
        return dependedSessionName;
    }
}
