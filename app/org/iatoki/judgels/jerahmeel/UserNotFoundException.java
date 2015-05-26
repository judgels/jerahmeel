package org.iatoki.judgels.jerahmeel;

import org.iatoki.judgels.commons.EntityNotFoundException;

public final class UserNotFoundException extends EntityNotFoundException {

    public UserNotFoundException() {
        super();
    }

    public UserNotFoundException(String s) {
        super(s);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserNotFoundException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getEntityName() {
        return "User";
    }
}
