package org.iatoki.judgels.jerahmeel.services.impls;

import org.iatoki.judgels.jerahmeel.ChapterLesson;
import org.iatoki.judgels.jerahmeel.ChapterLessonStatus;
import org.iatoki.judgels.jerahmeel.models.entities.ChapterLessonModel;

final class ChapterLessonServiceUtils {

    private ChapterLessonServiceUtils() {
        // prevent instantiation
    }

    static ChapterLesson createFromModel(ChapterLessonModel model) {
        return new ChapterLesson(model.id, model.chapterJid, model.lessonJid, model.lessonSecret, model.alias, ChapterLessonStatus.valueOf(model.status));
    }
}
