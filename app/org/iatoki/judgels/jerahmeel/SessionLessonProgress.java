package org.iatoki.judgels.jerahmeel;

public class SessionLessonProgress {
    private final SessionLesson sessionLesson;
    private final LessonProgress lessonProgress;

    public SessionLessonProgress(SessionLesson sessionLesson, LessonProgress lessonProgress) {
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
