package org.iatoki.judgels.jerahmeel.models.daos;

import org.iatoki.judgels.jerahmeel.models.entities.CurriculumCourseModel;
import org.iatoki.judgels.play.models.daos.Dao;

public interface CurriculumCourseDao extends Dao<Long, CurriculumCourseModel> {

    boolean existsByCurriculumJidAndAlias(String curriculumJid, String alias);

    boolean existsByCurriculumJidAndCourseJid(String curriculumJid, String courseJid);
}
