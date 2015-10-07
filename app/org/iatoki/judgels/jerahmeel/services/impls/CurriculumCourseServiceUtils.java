package org.iatoki.judgels.jerahmeel.services.impls;

import org.iatoki.judgels.jerahmeel.CurriculumCourse;
import org.iatoki.judgels.jerahmeel.models.entities.CurriculumCourseModel;

final class CurriculumCourseServiceUtils {

    private CurriculumCourseServiceUtils() {
        // prevent instantiation
    }

    static CurriculumCourse createFromModel(CurriculumCourseModel model) {
        return new CurriculumCourse(model.id, model.curriculumJid, model.courseJid, model.alias);
    }
}
