package org.iatoki.judgels.jerahmeel.services.impls;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.iatoki.judgels.jerahmeel.Session;
import org.iatoki.judgels.jerahmeel.SessionNotFoundException;
import org.iatoki.judgels.jerahmeel.models.daos.SessionDao;
import org.iatoki.judgels.jerahmeel.models.entities.SessionModel;
import org.iatoki.judgels.jerahmeel.models.entities.SessionModel_;
import org.iatoki.judgels.jerahmeel.services.SessionService;
import org.iatoki.judgels.play.Page;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Singleton
@Named("sessionService")
public final class SessionServiceImpl implements SessionService {

    private final SessionDao sessionDao;

    @Inject
    public SessionServiceImpl(SessionDao sessionDao) {
        this.sessionDao = sessionDao;
    }

    @Override
    public boolean sessionExistsByJid(String sessionJid) {
        return sessionDao.existsByJid(sessionJid);
    }

    @Override
    public List<Session> getSessionsByTerm(String term) {
        List<SessionModel> sessions = sessionDao.findSortedByFilters("id", "asc", term, 0, -1);
        ImmutableList.Builder<Session> sessionBuilder = ImmutableList.builder();

        for (SessionModel session : sessions) {
            sessionBuilder.add(SessionServiceUtils.createSessionFromModel(session));
        }

        return sessionBuilder.build();
    }

    @Override
    public Page<Session> getPageOfSessions(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = sessionDao.countByFilters(filterString);
        List<SessionModel> sessionModels = sessionDao.findSortedByFilters(orderBy, orderDir, filterString, pageIndex * pageSize, pageSize);

        List<Session> sessions = Lists.transform(sessionModels, m -> SessionServiceUtils.createSessionFromModel(m));

        return new Page<>(sessions, totalPages, pageIndex, pageSize);
    }

    @Override
    public Session findSessionByJid(String sessionJid) {
        SessionModel sessionModel = sessionDao.findByJid(sessionJid);

        return new Session(sessionModel.id, sessionModel.jid, sessionModel.name, sessionModel.description);
    }

    @Override
    public Session findSessionById(long sessionId) throws SessionNotFoundException {
        SessionModel sessionModel = sessionDao.findById(sessionId);
        if (sessionModel != null) {
            return SessionServiceUtils.createSessionFromModel(sessionModel);
        } else {
            throw new SessionNotFoundException("Session not found.");
        }
    }

    @Override
    public Map<String, String> getSessionJidToNameMapBySessionJids(Collection<String> sessionJids) {
        List<SessionModel> sessionModels = sessionDao.findSortedByFiltersIn("id", "asc", "", ImmutableMap.of(SessionModel_.jid, sessionJids), 0, -1);

        ImmutableMap.Builder<String, String> sessionJidToNameMap = ImmutableMap.builder();
        for (SessionModel sessionModel : sessionModels) {
            sessionJidToNameMap.put(sessionModel.jid, sessionModel.name);
        }

        return sessionJidToNameMap.build();
    }

    @Override
    public Session createSession(String name, String description, String userJid, String userIpAddress) {
        SessionModel sessionModel = new SessionModel();
        sessionModel.name = name;
        sessionModel.description = description;

        sessionDao.persist(sessionModel, userJid, userIpAddress);

        return SessionServiceUtils.createSessionFromModel(sessionModel);
    }

    @Override
    public void updateSession(String sessionJid, String name, String description, String userJid, String userIpAddress) {
        SessionModel sessionModel = sessionDao.findByJid(sessionJid);
        sessionModel.name = name;
        sessionModel.description = description;

        sessionDao.edit(sessionModel, userJid, userIpAddress);
    }
}
