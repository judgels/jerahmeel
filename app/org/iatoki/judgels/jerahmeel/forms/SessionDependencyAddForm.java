package org.iatoki.judgels.jerahmeel.forms;

import play.data.validation.Constraints;

public final class SessionDependencyAddForm {

    @Constraints.Required
    public String sessionJid;
}
