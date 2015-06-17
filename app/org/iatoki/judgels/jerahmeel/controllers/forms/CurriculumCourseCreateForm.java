package org.iatoki.judgels.jerahmeel.controllers.forms;

import play.data.validation.Constraints;

public final class CurriculumCourseCreateForm {

    @Constraints.Required
    public String courseJid;

    @Constraints.Required
    public String alias;

    @Constraints.Required
    public boolean completeable;

}
