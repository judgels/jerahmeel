package org.iatoki.judgels.jerahmeel;

import play.data.validation.Constraints;

public final class SessionLessonCreateForm {

    @Constraints.Required
    public String alias;

    @Constraints.Required
    public String lessonJid;

    @Constraints.Required
    public String lessonSecret;

    @Constraints.Required
    public String status;
}
