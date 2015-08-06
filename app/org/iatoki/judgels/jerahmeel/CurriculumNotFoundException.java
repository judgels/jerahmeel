package org.iatoki.judgels.jerahmeel;

import org.iatoki.judgels.commons.EntityNotFoundException;

public final class CurriculumNotFoundException extends EntityNotFoundException {

    public CurriculumNotFoundException() {
        super();
    }

    public CurriculumNotFoundException(String s) {
        super(s);
    }

    public CurriculumNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public CurriculumNotFoundException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getEntityName() {
        return "Curriculum";
    }
}
