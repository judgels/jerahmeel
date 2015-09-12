package org.iatoki.judgels.jerahmeel.services.impls;

import org.iatoki.judgels.jerahmeel.SessionLesson;
import org.iatoki.judgels.jerahmeel.SessionLessonStatus;
import org.iatoki.judgels.jerahmeel.models.entities.SessionLessonModel;

final class SessionLessonServiceUtils {

    private SessionLessonServiceUtils() {
        // prevent instantiation
    }

    static SessionLesson createFromModel(SessionLessonModel model) {
        return new SessionLesson(model.id, model.sessionJid, model.lessonJid, model.lessonSecret, model.alias, SessionLessonStatus.valueOf(model.status));
    }
}
