package org.iatoki.judgels.jerahmeel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.jerahmeel.models.domains.SessionLessonModel;

public interface SessionLessonDao extends Dao<Long, SessionLessonModel> {

    boolean existBySessionJidAndAlias(String sessionJid, String alias);
}
