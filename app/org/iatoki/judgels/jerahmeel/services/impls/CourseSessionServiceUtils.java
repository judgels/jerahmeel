package org.iatoki.judgels.jerahmeel.services.impls;

import org.iatoki.judgels.jerahmeel.CourseSession;
import org.iatoki.judgels.jerahmeel.models.entities.CourseSessionModel;

final class CourseSessionServiceUtils {

    private CourseSessionServiceUtils() {
        // prevent instantiation
    }

    static CourseSession createFromModel(CourseSessionModel model) {
        return new CourseSession(model.id, model.courseJid, model.sessionJid, model.alias);
    }
}
