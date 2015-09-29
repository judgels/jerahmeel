package org.iatoki.judgels.jerahmeel.models.daos;

import org.iatoki.judgels.jerahmeel.models.entities.SessionDependencyModel;
import org.iatoki.judgels.play.models.daos.Dao;

import java.util.List;

public interface SessionDependencyDao extends Dao<Long, SessionDependencyModel> {

    boolean existsBySessionJidAndDependencyJid(String sessionJid, String dependencyJid);

    List<SessionDependencyModel> getBySessionJid(String sessionJid);
}
