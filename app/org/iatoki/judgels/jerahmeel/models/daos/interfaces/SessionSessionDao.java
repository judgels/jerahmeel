package org.iatoki.judgels.jerahmeel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.jerahmeel.models.domains.SessionSessionModel;

import java.util.List;

public interface SessionSessionDao extends Dao<Long, SessionSessionModel> {

    boolean existBySessionJidAndDependencyJid(String sessionJid, String dependencyJid);

    List<SessionSessionModel> findBySessionJid(String sessionJid);

}
