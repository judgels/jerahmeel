package org.iatoki.judgels.jerahmeel.controllers.forms;

import play.data.validation.Constraints;

public final class SessionDependencyCreateForm {

    @Constraints.Required
    public String sessionJid;

}
