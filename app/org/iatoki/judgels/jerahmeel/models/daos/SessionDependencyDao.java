package org.iatoki.judgels.jerahmeel.models.daos;

import org.iatoki.judgels.play.models.daos.interfaces.Dao;
import org.iatoki.judgels.jerahmeel.models.entities.SessionDependencyModel;

import java.util.List;

public interface SessionDependencyDao extends Dao<Long, SessionDependencyModel> {

    boolean existBySessionJidAndDependencyJid(String sessionJid, String dependencyJid);

    List<SessionDependencyModel> findBySessionJid(String sessionJid);

}
