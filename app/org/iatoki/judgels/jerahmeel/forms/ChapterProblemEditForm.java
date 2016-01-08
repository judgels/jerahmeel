package org.iatoki.judgels.jerahmeel.forms;

import play.data.validation.Constraints;

public class ChapterProblemEditForm {

    @Constraints.Required
    public String alias;

    @Constraints.Required
    public String status;
}
