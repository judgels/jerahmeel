package org.iatoki.judgels.jerahmeel.forms;

import play.data.validation.Constraints;

public final class CourseSessionEditForm {

    @Constraints.Required
    public String alias;
}
