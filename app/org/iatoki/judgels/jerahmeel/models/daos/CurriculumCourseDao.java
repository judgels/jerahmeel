package org.iatoki.judgels.jerahmeel.models.daos;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.jerahmeel.models.entities.CurriculumCourseModel;

public interface CurriculumCourseDao extends Dao<Long, CurriculumCourseModel> {

    boolean existByCurriculumJidAndAlias(String curriculumJid, String alias);

    boolean existByCurriculumJidAndCourseJid(String curriculumJid, String courseJid);

}
