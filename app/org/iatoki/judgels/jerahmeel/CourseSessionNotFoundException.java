package org.iatoki.judgels.jerahmeel;

import org.iatoki.judgels.play.EntityNotFoundException;

public final class CourseSessionNotFoundException extends EntityNotFoundException {

    public CourseSessionNotFoundException() {
        super();
    }

    public CourseSessionNotFoundException(String s) {
        super(s);
    }

    public CourseSessionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public CourseSessionNotFoundException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getEntityName() {
        return "Course Session";
    }
}
