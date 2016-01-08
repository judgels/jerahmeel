package org.iatoki.judgels.jerahmeel.forms;

import play.data.validation.Constraints;

public final class ChapterUpsertForm {

    @Constraints.Required
    public String name;

    public String description;
}
