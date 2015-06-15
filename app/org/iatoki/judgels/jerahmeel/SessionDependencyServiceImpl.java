package org.iatoki.judgels.jerahmeel;

import com.google.common.collect.ImmutableMap;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.SessionDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.SessionDependencyDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.UserItemDao;
import org.iatoki.judgels.jerahmeel.models.domains.SessionDependencyModel;
import org.iatoki.judgels.jerahmeel.models.domains.SessionDependencyModel_;
import org.iatoki.judgels.jerahmeel.models.domains.UserItemModel;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class SessionDependencyServiceImpl implements SessionDependencyService {

    private final SessionDao sessionDao;
    private final SessionDependencyDao sessionDependencyDao;
    private final UserItemDao userItemDao;

    public SessionDependencyServiceImpl(SessionDao sessionDao, SessionDependencyDao sessionDependencyDao, UserItemDao userItemDao) {
        this.sessionDao = sessionDao;
        this.sessionDependencyDao = sessionDependencyDao;
        this.userItemDao = userItemDao;
    }

    @Override
    public boolean isDependenciesFulfilled(String userJid, String sessionJid) {
        List<UserItemModel> completedUserItemModel = userItemDao.findByStatus(UserItemStatus.COMPLETED.name());
        Set<String> completedJids = completedUserItemModel.stream().map(m -> m.itemJid).collect(Collectors.toSet());

        List<SessionDependencyModel> sessionDependencyModels = sessionDependencyDao.findBySessionJid(sessionJid);
        Set<String> dependencyJids = sessionDependencyModels.stream().map(m -> m.dependedSessionJid).collect(Collectors.toSet());

        dependencyJids.removeAll(completedJids);
        return dependencyJids.isEmpty();
    }

    @Override
    public boolean existBySessionJidAndDependencyJid(String sessionJid, String dependencyJid) {
        return sessionDependencyDao.existBySessionJidAndDependencyJid(sessionJid, dependencyJid);
    }

    @Override
    public SessionDependency findSessionDependencyBySessionDependencyId(long sessionDependencyId) throws SessionDependencyNotFoundException {
        SessionDependencyModel sessionDependencyModel = sessionDependencyDao.findById(sessionDependencyId);
        if (sessionDependencyModel != null) {
            return new SessionDependency(sessionDependencyModel.id, sessionDependencyModel.sessionJid, sessionDependencyModel.dependedSessionJid, null);
        } else {
            throw new SessionDependencyNotFoundException("Session Dependency Not Found.");
        }
    }

    @Override
    public Page<SessionDependency> findSessionDependencies(String sessionJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = sessionDependencyDao.countByFilters(filterString, ImmutableMap.of(SessionDependencyModel_.sessionJid, sessionJid), ImmutableMap.of());
        List<SessionDependencyModel> sessionDependencyModels = sessionDependencyDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(SessionDependencyModel_.sessionJid, sessionJid), ImmutableMap.of(), pageIndex * pageSize, pageSize);

        List<SessionDependency> sessionDependencies = sessionDependencyModels.stream().map(s -> new SessionDependency(s.id, s.sessionJid, s.dependedSessionJid, sessionDao.findByJid(s.dependedSessionJid).name)).collect(Collectors.toList());

        return new Page<>(sessionDependencies, totalPages, pageIndex, pageSize);
    }

    @Override
    public void addSessionDependency(String sessionJid, String dependedSessionJid) {
        SessionDependencyModel sessionDependencyModel = new SessionDependencyModel();
        sessionDependencyModel.sessionJid = sessionJid;
        sessionDependencyModel.dependedSessionJid = dependedSessionJid;

        sessionDependencyDao.persist(sessionDependencyModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void removeSessionDependency(long sessionDependencyId) throws SessionDependencyNotFoundException {
        SessionDependencyModel sessionDependencyModel = sessionDependencyDao.findById(sessionDependencyId);
        if (sessionDependencyModel != null) {
            sessionDependencyDao.remove(sessionDependencyModel);
        } else {
            throw new SessionDependencyNotFoundException("Session Dependency Not Found.");
        }
    }
}
