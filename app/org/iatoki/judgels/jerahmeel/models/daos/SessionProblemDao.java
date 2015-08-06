package org.iatoki.judgels.jerahmeel.models.daos;

import org.iatoki.judgels.play.models.daos.Dao;
import org.iatoki.judgels.jerahmeel.models.entities.SessionProblemModel;

import java.util.List;

public interface SessionProblemDao extends Dao<Long, SessionProblemModel> {

    boolean existBySessionJidAndAlias(String sessionJid, String alias);

    List<SessionProblemModel> findBySessionJid(String sessionJid);

    SessionProblemModel findBySesssionJidAndProblemJid(String sessionJid, String problemJid);
}
