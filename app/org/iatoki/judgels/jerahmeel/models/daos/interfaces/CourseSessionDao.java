package org.iatoki.judgels.jerahmeel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.jerahmeel.models.domains.CourseSessionModel;

public interface CourseSessionDao extends Dao<Long, CourseSessionModel> {

    boolean existByCourseJidAndSessionJid(String courseJid, String sessionJid);

}
