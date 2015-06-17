package org.iatoki.judgels.jerahmeel.models.daos;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.jerahmeel.models.entities.CourseSessionModel;

public interface CourseSessionDao extends Dao<Long, CourseSessionModel> {

    boolean existByCourseJidAndAlias(String courseJid, String alias);

    boolean existByCourseJidAndSessionJid(String courseJid, String sessionJid);

}
