package org.iatoki.judgels.jerahmeel;

import org.iatoki.judgels.play.EntityNotFoundException;

public final class SessionProblemNotFoundException extends EntityNotFoundException {

    public SessionProblemNotFoundException() {
        super();
    }

    public SessionProblemNotFoundException(String s) {
        super(s);
    }

    public SessionProblemNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public SessionProblemNotFoundException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getEntityName() {
        return "Session Problem";
    }
}
