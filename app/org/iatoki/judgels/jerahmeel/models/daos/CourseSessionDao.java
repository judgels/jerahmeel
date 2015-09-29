package org.iatoki.judgels.jerahmeel.models.daos;

import org.iatoki.judgels.jerahmeel.models.entities.CourseSessionModel;
import org.iatoki.judgels.play.models.daos.Dao;

public interface CourseSessionDao extends Dao<Long, CourseSessionModel> {

    boolean existsByCourseJidAndAlias(String courseJid, String alias);

    boolean existsByCourseJidAndSessionJid(String courseJid, String sessionJid);
}
