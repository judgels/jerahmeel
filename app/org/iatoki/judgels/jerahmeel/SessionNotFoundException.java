package org.iatoki.judgels.jerahmeel;

import org.iatoki.judgels.commons.EntityNotFoundException;

public final class SessionNotFoundException extends EntityNotFoundException {

    public SessionNotFoundException() {
        super();
    }

    public SessionNotFoundException(String s) {
        super(s);
    }

    public SessionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public SessionNotFoundException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getEntityName() {
        return "Session";
    }
}
