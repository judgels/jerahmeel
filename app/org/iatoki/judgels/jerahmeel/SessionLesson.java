package org.iatoki.judgels.jerahmeel;

public final class SessionLesson {

    private final long id;
    private final String sessionJid;
    private final String lessonJid;
    private final String lessonSecret;
    private final String alias;
    private final SessionLessonStatus status;

    public SessionLesson(long id, String sessionJid, String lessonJid, String lessonSecret, String alias, SessionLessonStatus status) {
        this.id = id;
        this.sessionJid = sessionJid;
        this.lessonJid = lessonJid;
        this.lessonSecret = lessonSecret;
        this.alias = alias;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public String getSessionJid() {
        return sessionJid;
    }

    public String getLessonJid() {
        return lessonJid;
    }

    public String getLessonSecret() {
        return lessonSecret;
    }

    public String getAlias() {
        return alias;
    }

    public SessionLessonStatus getStatus() {
        return status;
    }
}
