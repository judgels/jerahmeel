package org.iatoki.judgels.jerahmeel.services.impls;

import org.iatoki.judgels.jerahmeel.CourseChapter;
import org.iatoki.judgels.jerahmeel.models.entities.CourseChapterModel;

final class CourseChapterServiceUtils {

    private CourseChapterServiceUtils() {
        // prevent instantiation
    }

    static CourseChapter createFromModel(CourseChapterModel model) {
        return new CourseChapter(model.id, model.courseJid, model.chapterJid, model.alias);
    }
}
