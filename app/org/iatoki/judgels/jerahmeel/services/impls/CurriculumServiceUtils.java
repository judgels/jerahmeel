package org.iatoki.judgels.jerahmeel.services.impls;

import org.iatoki.judgels.jerahmeel.Curriculum;
import org.iatoki.judgels.jerahmeel.models.entities.CurriculumModel;

final class CurriculumServiceUtils {

    private CurriculumServiceUtils() {
        // prevent instantiation
    }

    static Curriculum createCurriculumFromModel(CurriculumModel curriculumModel) {
        return new Curriculum(curriculumModel.id, curriculumModel.jid, curriculumModel.name, curriculumModel.description);
    }
}
