package org.iatoki.judgels.jerahmeel.forms;

import play.data.validation.Constraints;

public final class SessionProblemAddForm {

    @Constraints.Required
    public String alias;

    @Constraints.Required
    public String problemJid;

    @Constraints.Required
    public String problemSecret;

    @Constraints.Required
    public String type;

    @Constraints.Required
    public String status;
}
