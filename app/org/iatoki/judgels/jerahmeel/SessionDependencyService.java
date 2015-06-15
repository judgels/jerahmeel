package org.iatoki.judgels.jerahmeel;

import org.iatoki.judgels.commons.Page;

public interface SessionDependencyService {

    boolean isDependenciesFulfilled(String userJid, String sessionJid);

    boolean existBySessionJidAndDependencyJid(String sessionJid, String dependencyJid);

    SessionDependency findSessionDependencyBySessionDependencyId(long sessionDependencyId) throws SessionDependencyNotFoundException;

    Page<SessionDependency> findSessionDependencies(String sessionJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    void addSessionDependency(String sessionJid, String dependedSessionJid);

    void removeSessionDependency(long sessionDependencyId) throws SessionDependencyNotFoundException;
}
