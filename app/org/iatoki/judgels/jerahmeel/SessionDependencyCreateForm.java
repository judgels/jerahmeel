package org.iatoki.judgels.jerahmeel;

import play.data.validation.Constraints;

public final class SessionDependencyCreateForm {

    @Constraints.Required
    public String sessionJid;

}
