package org.iatoki.judgels.jerahmeel.forms;

import play.data.validation.Constraints;

public class SessionProblemUpdateForm {

    @Constraints.Required
    public String alias;

    @Constraints.Required
    public String status;
}
