package org.iatoki.judgels.jerahmeel.forms;

import play.data.validation.Constraints;

public final class CurriculumCourseUpdateForm {

    @Constraints.Required
    public String alias;

    @Constraints.Required
    public boolean completeable;

}
