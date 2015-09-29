package org.iatoki.judgels.jerahmeel.services.impls;

import com.google.common.collect.ImmutableMap;
import org.iatoki.judgels.jerahmeel.SessionDependency;
import org.iatoki.judgels.jerahmeel.SessionDependencyNotFoundException;
import org.iatoki.judgels.jerahmeel.UserItemStatus;
import org.iatoki.judgels.jerahmeel.models.daos.SessionDao;
import org.iatoki.judgels.jerahmeel.models.daos.SessionDependencyDao;
import org.iatoki.judgels.jerahmeel.models.daos.UserItemDao;
import org.iatoki.judgels.jerahmeel.models.entities.SessionDependencyModel;
import org.iatoki.judgels.jerahmeel.models.entities.SessionDependencyModel_;
import org.iatoki.judgels.jerahmeel.models.entities.UserItemModel;
import org.iatoki.judgels.jerahmeel.services.SessionDependencyService;
import org.iatoki.judgels.play.Page;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
@Named("sessionDependencyService")
public final class SessionDependencyServiceImpl implements SessionDependencyService {

    private final SessionDao sessionDao;
    private final SessionDependencyDao sessionDependencyDao;
    private final UserItemDao userItemDao;

    @Inject
    public SessionDependencyServiceImpl(SessionDao sessionDao, SessionDependencyDao sessionDependencyDao, UserItemDao userItemDao) {
        this.sessionDao = sessionDao;
        this.sessionDependencyDao = sessionDependencyDao;
        this.userItemDao = userItemDao;
    }

    @Override
    public boolean isDependenciesFulfilled(String userJid, String sessionJid) {
        List<UserItemModel> completedUserItemModel = userItemDao.getByUserJidAndStatus(userJid, UserItemStatus.COMPLETED.name());
        Set<String> completedJids = completedUserItemModel.stream().map(m -> m.itemJid).collect(Collectors.toSet());

        List<SessionDependencyModel> sessionDependencyModels = sessionDependencyDao.getBySessionJid(sessionJid);
        Set<String> dependencyJids = sessionDependencyModels.stream().map(m -> m.dependedSessionJid).collect(Collectors.toSet());

        dependencyJids.removeAll(completedJids);
        return dependencyJids.isEmpty();
    }

    @Override
    public boolean existsBySessionJidAndDependencyJid(String sessionJid, String dependencyJid) {
        return sessionDependencyDao.existsBySessionJidAndDependencyJid(sessionJid, dependencyJid);
    }

    @Override
    public SessionDependency findSessionDependencyById(long sessionDependencyId) throws SessionDependencyNotFoundException {
        SessionDependencyModel sessionDependencyModel = sessionDependencyDao.findById(sessionDependencyId);
        if (sessionDependencyModel != null) {
            return new SessionDependency(sessionDependencyModel.id, sessionDependencyModel.sessionJid, sessionDependencyModel.dependedSessionJid, null);
        } else {
            throw new SessionDependencyNotFoundException("Session Dependency Not Found.");
        }
    }

    @Override
    public Page<SessionDependency> getPageOfSessionDependencies(String sessionJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = sessionDependencyDao.countByFilters(filterString, ImmutableMap.of(SessionDependencyModel_.sessionJid, sessionJid), ImmutableMap.of());
        List<SessionDependencyModel> sessionDependencyModels = sessionDependencyDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(SessionDependencyModel_.sessionJid, sessionJid), ImmutableMap.of(), pageIndex * pageSize, pageSize);

        List<SessionDependency> sessionDependencies = sessionDependencyModels.stream().map(s -> new SessionDependency(s.id, s.sessionJid, s.dependedSessionJid, sessionDao.findByJid(s.dependedSessionJid).name)).collect(Collectors.toList());

        return new Page<>(sessionDependencies, totalPages, pageIndex, pageSize);
    }

    @Override
    public SessionDependency addSessionDependency(String sessionJid, String dependedSessionJid, String userJid, String userIpAddress) {
        SessionDependencyModel sessionDependencyModel = new SessionDependencyModel();
        sessionDependencyModel.sessionJid = sessionJid;
        sessionDependencyModel.dependedSessionJid = dependedSessionJid;

        sessionDependencyDao.persist(sessionDependencyModel, userJid, userIpAddress);

        return new SessionDependency(sessionDependencyModel.id, sessionDependencyModel.sessionJid, sessionDependencyModel.dependedSessionJid, null);
    }

    @Override
    public void removeSessionDependency(long sessionDependencyId) {
        SessionDependencyModel sessionDependencyModel = sessionDependencyDao.findById(sessionDependencyId);

        sessionDependencyDao.remove(sessionDependencyModel);
    }
}
