package org.iatoki.judgels.jerahmeel;

import org.iatoki.judgels.play.EntityNotFoundException;

public final class SessionDependencyNotFoundException extends EntityNotFoundException {

    public SessionDependencyNotFoundException() {
        super();
    }

    public SessionDependencyNotFoundException(String s) {
        super(s);
    }

    public SessionDependencyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public SessionDependencyNotFoundException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getEntityName() {
        return "Session Dependency";
    }
}
