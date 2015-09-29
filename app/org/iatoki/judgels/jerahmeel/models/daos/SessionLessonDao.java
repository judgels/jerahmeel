package org.iatoki.judgels.jerahmeel.models.daos;

import org.iatoki.judgels.jerahmeel.models.entities.SessionLessonModel;
import org.iatoki.judgels.play.models.daos.Dao;

public interface SessionLessonDao extends Dao<Long, SessionLessonModel> {

    boolean existsBySessionJidAndAlias(String sessionJid, String alias);
}
