package org.iatoki.judgels.jerahmeel;

public final class CourseSession {

    private final long id;
    private final String courseJid;
    private final String sessionJid;
    private final String alias;
    private final String sessionName;

    public CourseSession(long id, String courseJid, String sessionJid, String alias, String sessionName) {
        this.id = id;
        this.courseJid = courseJid;
        this.sessionJid = sessionJid;
        this.alias = alias;
        this.sessionName = sessionName;
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

    public String getSessionName() {
        return sessionName;
    }
}
