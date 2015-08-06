package org.iatoki.judgels.jerahmeel;

import org.iatoki.judgels.play.EntityNotFoundException;

public final class SessionLessonNotFoundException extends EntityNotFoundException {

    public SessionLessonNotFoundException() {
        super();
    }

    public SessionLessonNotFoundException(String s) {
        super(s);
    }

    public SessionLessonNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public SessionLessonNotFoundException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getEntityName() {
        return "Session Lesson";
    }
}
