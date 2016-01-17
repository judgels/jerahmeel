package org.iatoki.judgels.jerahmeel.forms;

import play.data.validation.Constraints;

public final class CourseChapterAddForm {

    @Constraints.Required
    public String chapterJid;

    @Constraints.Required
    public String alias;
}
