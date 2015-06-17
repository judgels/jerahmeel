package org.iatoki.judgels.jerahmeel.services;

import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.jerahmeel.SessionDependency;
import org.iatoki.judgels.jerahmeel.SessionDependencyNotFoundException;

public interface SessionDependencyService {

    boolean isDependenciesFulfilled(String userJid, String sessionJid);

    boolean existBySessionJidAndDependencyJid(String sessionJid, String dependencyJid);

    SessionDependency findSessionDependencyBySessionDependencyId(long sessionDependencyId) throws SessionDependencyNotFoundException;

    Page<SessionDependency> findSessionDependencies(String sessionJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    void addSessionDependency(String sessionJid, String dependedSessionJid);

    void removeSessionDependency(long sessionDependencyId) throws SessionDependencyNotFoundException;
}
