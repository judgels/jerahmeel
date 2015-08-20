package org.iatoki.judgels.jerahmeel.models.daos;

import org.iatoki.judgels.play.models.daos.Dao;
import org.iatoki.judgels.jerahmeel.models.entities.SessionLessonModel;

public interface SessionLessonDao extends Dao<Long, SessionLessonModel> {

    boolean existsBySessionJidAndAlias(String sessionJid, String alias);
}
