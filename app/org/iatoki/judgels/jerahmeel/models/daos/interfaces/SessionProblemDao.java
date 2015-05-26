package org.iatoki.judgels.jerahmeel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.jerahmeel.models.domains.SessionProblemModel;

public interface SessionProblemDao extends Dao<Long, SessionProblemModel> {
    boolean existBySessionJidProblemJidAndAlias(String sessionJid, String problemJid, String alias);
}
