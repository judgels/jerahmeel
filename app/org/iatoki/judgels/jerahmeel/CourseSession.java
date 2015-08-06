package org.iatoki.judgels.jerahmeel;

public final class CourseSession {

    private final long id;
    private final String courseJid;
    private final String sessionJid;
    private final String alias;
    private final String sessionName;
    private final boolean completeable;

    public CourseSession(long id, String courseJid, String sessionJid, String alias, String sessionName, boolean completeable) {
        this.id = id;
        this.courseJid = courseJid;
        this.sessionJid = sessionJid;
        this.alias = alias;
        this.sessionName = sessionName;
        this.completeable = completeable;
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

    public boolean isCompleteable() {
        return completeable;
    }
}
