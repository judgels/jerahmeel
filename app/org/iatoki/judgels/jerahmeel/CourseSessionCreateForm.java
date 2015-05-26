package org.iatoki.judgels.jerahmeel;

import play.data.validation.Constraints;

public final class CourseSessionCreateForm {

    @Constraints.Required
    public String sessionJid;

}
