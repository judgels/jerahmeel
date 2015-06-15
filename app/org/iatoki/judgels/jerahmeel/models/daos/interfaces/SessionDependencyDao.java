package org.iatoki.judgels.jerahmeel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.jerahmeel.models.domains.SessionDependencyModel;

import java.util.List;

public interface SessionDependencyDao extends Dao<Long, SessionDependencyModel> {

    boolean existBySessionJidAndDependencyJid(String sessionJid, String dependencyJid);

    List<SessionDependencyModel> findBySessionJid(String sessionJid);

}
