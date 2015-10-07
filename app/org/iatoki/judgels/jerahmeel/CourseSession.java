package org.iatoki.judgels.jerahmeel;

public final class CourseSession {

    private final long id;
    private final String courseJid;
    private final String sessionJid;
    private final String alias;

    public CourseSession(long id, String courseJid, String sessionJid, String alias) {
        this.id = id;
        this.courseJid = courseJid;
        this.sessionJid = sessionJid;
        this.alias = alias;
    }

    public long getId() {
        return id;
    }

    public String getCourseJid() {
        return courseJid;
    }

    public String getSessionJid() {
        return sessionJid;
    }

    public String getAlias() {
        return alias;
    }
}
