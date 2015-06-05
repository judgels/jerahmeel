package org.iatoki.judgels.jerahmeel;

import com.google.common.collect.ImmutableMap;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.SessionLessonDao;
import org.iatoki.judgels.jerahmeel.models.domains.SessionLessonModel;
import org.iatoki.judgels.jerahmeel.models.domains.SessionLessonModel_;

import java.util.List;
import java.util.stream.Collectors;

public final class SessionLessonServiceImpl implements SessionLessonService {

    private final SessionLessonDao sessionLessonDao;

    public SessionLessonServiceImpl(SessionLessonDao sessionLessonDao) {
        this.sessionLessonDao = sessionLessonDao;
    }

    @Override
    public boolean isInSessionByLessonJidAndAlias(String sessionJid, String lessonJid, String alias) {
        return sessionLessonDao.existBySessionJidLessonJidAndAlias(sessionJid, lessonJid, alias);
    }

    @Override
    public SessionLesson findSessionLessonBySessionLessonId(long sessionLessonId) throws SessionLessonNotFoundException {
        SessionLessonModel sessionLessonModel = sessionLessonDao.findById(sessionLessonId);
        if (sessionLessonModel != null) {
            return new SessionLesson(sessionLessonModel.id, sessionLessonModel.sessionJid, sessionLessonModel.lessonJid, sessionLessonModel.lessonSecret, sessionLessonModel.alias, SessionLessonStatus.valueOf(sessionLessonModel.status));
        } else {
            throw new SessionLessonNotFoundException("Session Lesson Not Found");
        }
    }

    @Override
    public Page<SessionLesson> findSessionLessons(String sessionJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = sessionLessonDao.countByFilters(filterString, ImmutableMap.of(SessionLessonModel_.sessionJid, sessionJid), ImmutableMap.of());
        List<SessionLessonModel> sessionLessonModels = sessionLessonDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(SessionLessonModel_.sessionJid, sessionJid), ImmutableMap.of(), pageIndex * pageSize, pageSize);

        List<SessionLesson> sessionLessons = sessionLessonModels.stream().map(s -> new SessionLesson(s.id, s.sessionJid, s.lessonJid, s.lessonSecret, s.alias, SessionLessonStatus.valueOf(s.status))).collect(Collectors.toList());

        return new Page<>(sessionLessons, totalPages, pageIndex, pageSize);

    }

    @Override
    public void addSessionLesson(String sessionJid, String lessonJid, String lessonSecret, String alias, SessionLessonStatus status) {
        SessionLessonModel sessionLessonModel = new SessionLessonModel();
        sessionLessonModel.sessionJid = sessionJid;
        sessionLessonModel.lessonJid = lessonJid;
        sessionLessonModel.lessonSecret = lessonSecret;
        sessionLessonModel.alias = alias;
        sessionLessonModel.status = status.name();

        sessionLessonDao.persist(sessionLessonModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void removeSessionLesson(long sessionLessonId) throws SessionLessonNotFoundException {
        SessionLessonModel sessionLessonModel = sessionLessonDao.findById(sessionLessonId);
        if (sessionLessonModel != null) {
            sessionLessonDao.remove(sessionLessonModel);
        } else {
            throw new SessionLessonNotFoundException("Session Lesson Not Found");
        }
    }
}
