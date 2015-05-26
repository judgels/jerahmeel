package org.iatoki.judgels.jerahmeel;

public final class CourseSession {

    private final long id;
    private final String courseJid;
    private final String sessionJid;
    private final String sessionName;

    public CourseSession(long id, String courseJid, String sessionJid, String sessionName) {
        this.id = id;
        this.courseJid = courseJid;
        this.sessionJid = sessionJid;
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

    public String getSessionName() {
        return sessionName;
    }
}
