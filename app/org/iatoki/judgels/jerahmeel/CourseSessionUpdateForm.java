package org.iatoki.judgels.jerahmeel;

import play.data.validation.Constraints;

public final class CourseSessionUpdateForm {

    @Constraints.Required
    public String alias;

    @Constraints.Required
    public boolean completeable;

}