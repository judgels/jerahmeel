package org.iatoki.judgels.jerahmeel.forms;

import play.data.validation.Constraints;

public final class CurriculumUpsertForm {

    @Constraints.Required
    public String name;

    @Constraints.Required
    public String description;

}
