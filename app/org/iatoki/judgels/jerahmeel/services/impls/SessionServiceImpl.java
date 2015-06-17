package org.iatoki.judgels.jerahmeel.services.impls;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.jerahmeel.Session;
import org.iatoki.judgels.jerahmeel.SessionNotFoundException;
import org.iatoki.judgels.jerahmeel.models.daos.SessionDao;
import org.iatoki.judgels.jerahmeel.models.entities.SessionModel;
import org.iatoki.judgels.jerahmeel.services.SessionService;

import java.util.List;

public final class SessionServiceImpl implements SessionService {

    private final SessionDao sessionDao;

    public SessionServiceImpl(SessionDao sessionDao) {
        this.sessionDao = sessionDao;
    }

    @Override
    public boolean existBySessionJid(String sessionJid) {
        return sessionDao.existsByJid(sessionJid);
    }

    @Override
    public List<Session> findAllSessionByTerm(String term) {
        List<SessionModel> sessions = sessionDao.findSortedByFilters("id", "asc", term, 0, -1);
        ImmutableList.Builder<Session> sessionBuilder = ImmutableList.builder();

        for (SessionModel session : sessions) {
            sessionBuilder.add(createSessionFromModel(session));
        }

        return sessionBuilder.build();
    }

    @Override
    public Page<Session> pageSessions(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = sessionDao.countByFilters(filterString, ImmutableMap.of(), ImmutableMap.of());
        List<SessionModel> sessionModels = sessionDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(), ImmutableMap.of(), pageIndex * pageSize, pageSize);

        List<Session> sessions = Lists.transform(sessionModels, m -> createSessionFromModel(m));

        return new Page<>(sessions, totalPages, pageIndex, pageSize);
    }

    @Override
    public Session findSessionBySessionJid(String sessionJid) {
        SessionModel sessionModel = sessionDao.findByJid(sessionJid);

        return new Session(sessionModel.id, sessionModel.jid, sessionModel.name, sessionModel.description);
    }

    @Override
    public Session findSessionBySessionId(long sessionId) throws SessionNotFoundException {
        SessionModel sessionModel = sessionDao.findById(sessionId);
        if (sessionModel != null) {
            return createSessionFromModel(sessionModel);
        } else {
            throw new SessionNotFoundException("Session not found.");
        }
    }

    @Override
    public void createSession(String name, String description) {
        SessionModel sessionModel = new SessionModel();
        sessionModel.name = name;
        sessionModel.description = description;

        sessionDao.persist(sessionModel, "michael", IdentityUtils.getIpAddress());
    }

    @Override
    public void updateSession(long sessionId, String name, String description) throws SessionNotFoundException {
        SessionModel sessionModel = sessionDao.findById(sessionId);
        if (sessionModel != null) {
            sessionModel.name = name;
            sessionModel.description = description;

            sessionDao.edit(sessionModel, "michael", IdentityUtils.getIpAddress());
        } else {
            throw new SessionNotFoundException("Session not found.");
        }
    }

    private Session createSessionFromModel(SessionModel sessionModel) {
        return new Session(sessionModel.id, sessionModel.jid, sessionModel.name, sessionModel.description);
    }
}
