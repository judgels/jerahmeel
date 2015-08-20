package org.iatoki.judgels.jerahmeel.models.daos;

import org.iatoki.judgels.play.models.daos.Dao;
import org.iatoki.judgels.jerahmeel.models.entities.CourseSessionModel;

public interface CourseSessionDao extends Dao<Long, CourseSessionModel> {

    boolean existsByCourseJidAndAlias(String courseJid, String alias);

    boolean existsByCourseJidAndSessionJid(String courseJid, String sessionJid);
}
