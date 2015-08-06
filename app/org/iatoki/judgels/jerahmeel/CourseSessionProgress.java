package org.iatoki.judgels.jerahmeel;

public class CourseSessionProgress {
    private final CourseSession courseSession;
    private final SessionProgress sessionProgress;

    public CourseSessionProgress(CourseSession courseSession, SessionProgress sessionProgress) {
        this.courseSession = courseSession;
        this.sessionProgress = sessionProgress;
    }

    public CourseSession getCourseSession() {
        return courseSession;
    }

    public SessionProgress getSessionProgress() {
        return sessionProgress;
    }
}
