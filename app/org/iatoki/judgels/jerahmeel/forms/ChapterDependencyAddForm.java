package org.iatoki.judgels.jerahmeel.forms;

import play.data.validation.Constraints;

public final class ChapterDependencyAddForm {

    @Constraints.Required
    public String chapterJid;
}
