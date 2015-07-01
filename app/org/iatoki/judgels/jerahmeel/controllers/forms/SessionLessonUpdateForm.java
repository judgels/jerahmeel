package org.iatoki.judgels.jerahmeel.controllers.forms;

import play.data.validation.Constraints;

public class SessionLessonUpdateForm {

    @Constraints.Required
    public String alias;

    @Constraints.Required
    public String status;
}
