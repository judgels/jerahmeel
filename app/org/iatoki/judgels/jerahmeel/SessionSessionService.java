package org.iatoki.judgels.jerahmeel;

import org.iatoki.judgels.commons.Page;

public interface SessionSessionService {

    boolean isDependenciesFulfilled(String userJid, String sessionJid);

    boolean existBySessionJidAndDependencyJid(String sessionJid, String dependencyJid);

    SessionSession findSessionSessionBySessionSessionId(long sessionSessionId) throws SessionSessionNotFoundException;

    Page<SessionSession> findSessionDependencies(String sessionJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    void addSessionDependency(String sessionJid, String dependedSessionJid);

    void removeSessionDependency(long sessionSessionId) throws SessionSessionNotFoundException;
}
