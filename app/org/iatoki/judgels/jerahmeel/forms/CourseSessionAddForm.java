package org.iatoki.judgels.jerahmeel.forms;

import play.data.validation.Constraints;

public final class CourseSessionAddForm {

    @Constraints.Required
    public String sessionJid;

    @Constraints.Required
    public String alias;
}
