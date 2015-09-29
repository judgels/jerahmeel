package org.iatoki.judgels.jerahmeel.models.daos;

import org.iatoki.judgels.jerahmeel.models.entities.SessionProblemModel;
import org.iatoki.judgels.play.models.daos.Dao;

import java.util.List;

public interface SessionProblemDao extends Dao<Long, SessionProblemModel> {

    boolean existsBySessionJidAndAlias(String sessionJid, String alias);

    List<SessionProblemModel> getBySessionJid(String sessionJid);

    SessionProblemModel findBySesssionJidAndProblemJid(String sessionJid, String problemJid);
}
