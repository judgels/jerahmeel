package org.iatoki.judgels.jerahmeel;

public final class SessionProblem {

    private final long id;
    private final String sessionJid;
    private final String problemJid;
    private final String problemSecret;
    private final String alias;
    private final SessionProblemType type;
    private final SessionProblemStatus status;

    public SessionProblem(long id, String sessionJid, String problemJid, String problemSecret, String alias, SessionProblemType type, SessionProblemStatus status) {
        this.id = id;
        this.sessionJid = sessionJid;
        this.problemJid = problemJid;
        this.problemSecret = problemSecret;
        this.alias = alias;
        this.type = type;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public String getSessionJid() {
        return sessionJid;
    }

    public String getProblemJid() {
        return problemJid;
    }

    public String getProblemSecret() {
        return problemSecret;
    }

    public String getAlias() {
        return alias;
    }

    public SessionProblemType getType() {
        return type;
    }

    public SessionProblemStatus getStatus() {
        return status;
    }
}
