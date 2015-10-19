package org.iatoki.judgels.jerahmeel;

public final class SessionLessonWithProgress {

    private final SessionLesson sessionLesson;
    private final LessonProgress lessonProgress;

    public SessionLessonWithProgress(SessionLesson sessionLesson, LessonProgress lessonProgress) {
        this.sessionLesson = sessionLesson;
        this.lessonProgress = lessonProgress;
    }

    public SessionLesson getSessionLesson() {
        return sessionLesson;
    }

    public LessonProgress getLessonProgress() {
        return lessonProgress;
    }
}
