package org.iatoki.judgels.jerahmeel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.jerahmeel.models.domains.SessionProblemModel;

import java.util.List;

public interface SessionProblemDao extends Dao<Long, SessionProblemModel> {
    boolean existBySessionJidAndAlias(String sessionJid, String alias);

    List<SessionProblemModel> findBySessionJid(String sessionJid);

    SessionProblemModel findBySesssionJidAndProblemJid(String sessionJid, String problemJid);
}
