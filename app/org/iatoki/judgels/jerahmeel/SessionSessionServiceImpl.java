package org.iatoki.judgels.jerahmeel;

import com.google.common.collect.ImmutableMap;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.SessionDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.SessionSessionDao;
import org.iatoki.judgels.jerahmeel.models.domains.SessionSessionModel;
import org.iatoki.judgels.jerahmeel.models.domains.SessionSessionModel_;

import java.util.List;
import java.util.stream.Collectors;

public final class SessionSessionServiceImpl implements SessionSessionService {

    private final SessionDao sessionDao;
    private final SessionSessionDao sessionSessionDao;

    public SessionSessionServiceImpl(SessionDao sessionDao, SessionSessionDao sessionSessionDao) {
        this.sessionDao = sessionDao;
        this.sessionSessionDao = sessionSessionDao;
    }

    @Override
    public boolean existBySessionJidAndDependencyJid(String sessionJid, String dependencyJid) {
        return sessionSessionDao.existBySessionJidAndDependencyJid(sessionJid, dependencyJid);
    }

    @Override
    public SessionSession findBySessionSessionId(long sessionSessionId) throws SessionSessionNotFoundException {
        SessionSessionModel sessionSessionModel = sessionSessionDao.findById(sessionSessionId);
        if (sessionSessionModel != null) {
            return new SessionSession(sessionSessionModel.id, sessionSessionModel.sessionJid, sessionSessionModel.dependedSessionJid, null);
        } else {
            throw new SessionSessionNotFoundException("Session Dependency Not Found.");
        }
    }

    @Override
    public Page<SessionSession> findSessionDependencies(String sessionJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = sessionSessionDao.countByFilters(filterString, ImmutableMap.of(SessionSessionModel_.sessionJid, sessionJid), ImmutableMap.of());
        List<SessionSessionModel> sessionSessionModels = sessionSessionDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(SessionSessionModel_.sessionJid, sessionJid), ImmutableMap.of(), pageIndex * pageSize, pageSize);

        List<SessionSession> sessionSessions = sessionSessionModels.stream().map(s -> new SessionSession(s.id, s.sessionJid, s.dependedSessionJid, sessionDao.findByJid(s.dependedSessionJid).name)).collect(Collectors.toList());

        return new Page<>(sessionSessions, totalPages, pageIndex, pageSize);
    }

    @Override
    public void addSessionDependency(String sessionJid, String dependedSessionJid) {
        SessionSessionModel sessionSessionModel = new SessionSessionModel();
        sessionSessionModel.sessionJid = sessionJid;
        sessionSessionModel.dependedSessionJid = dependedSessionJid;

        sessionSessionDao.persist(sessionSessionModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void removeSessionDependency(long sessionSessionId) throws SessionSessionNotFoundException {
        SessionSessionModel sessionSessionModel = sessionSessionDao.findById(sessionSessionId);
        if (sessionSessionModel != null) {
            sessionSessionDao.remove(sessionSessionModel);
        } else {
            throw new SessionSessionNotFoundException("Session Dependency Not Found.");
        }
    }
}
