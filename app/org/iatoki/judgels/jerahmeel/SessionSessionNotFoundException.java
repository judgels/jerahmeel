package org.iatoki.judgels.jerahmeel;

import org.iatoki.judgels.commons.EntityNotFoundException;

public final class SessionSessionNotFoundException extends EntityNotFoundException {

    public SessionSessionNotFoundException() {
        super();
    }

    public SessionSessionNotFoundException(String s) {
        super(s);
    }

    public SessionSessionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public SessionSessionNotFoundException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getEntityName() {
        return "Session Dependency";
    }
}
