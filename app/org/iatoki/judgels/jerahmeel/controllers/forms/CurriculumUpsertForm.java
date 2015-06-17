package org.iatoki.judgels.jerahmeel.controllers.forms;

import play.data.validation.Constraints;

public final class CurriculumUpsertForm {

    @Constraints.Required
    public String name;

    @Constraints.Required
    public String description;

}
