package org.iatoki.judgels.jerahmeel.services;

import org.iatoki.judgels.jerahmeel.SessionDependency;
import org.iatoki.judgels.jerahmeel.SessionDependencyNotFoundException;
import org.iatoki.judgels.play.Page;

public interface SessionDependencyService {

    boolean isDependenciesFulfilled(String userJid, String sessionJid);

    boolean existsBySessionJidAndDependencyJid(String sessionJid, String dependencyJid);

    SessionDependency findSessionDependencyById(long sessionDependencyId) throws SessionDependencyNotFoundException;

    Page<SessionDependency> getPageOfSessionDependencies(String sessionJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    SessionDependency addSessionDependency(String sessionJid, String dependedSessionJid, String userJid, String userIpAddress);

    void removeSessionDependency(long sessionDependencyId);
}
