package org.iatoki.judgels.jerahmeel;

import play.data.validation.Constraints;

public final class CourseUpsertForm {

    @Constraints.Required
    public String name;

    @Constraints.Required
    public String description;

}
