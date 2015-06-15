package org.iatoki.judgels.jerahmeel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.SessionLessonDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.UserItemDao;
import org.iatoki.judgels.jerahmeel.models.domains.SessionLessonModel;
import org.iatoki.judgels.jerahmeel.models.domains.SessionLessonModel_;

import java.util.List;
import java.util.stream.Collectors;

public final class SessionLessonServiceImpl implements SessionLessonService {

    private final SessionLessonDao sessionLessonDao;
    private final UserItemDao userItemDao;

    public SessionLessonServiceImpl(SessionLessonDao sessionLessonDao, UserItemDao userItemDao) {
        this.sessionLessonDao = sessionLessonDao;
        this.userItemDao = userItemDao;
    }

    @Override
    public boolean isInSessionByAlias(String sessionJid, String alias) {
        return sessionLessonDao.existBySessionJidAndAlias(sessionJid, alias);
    }

    @Override
    public SessionLesson findSessionLessonBySessionLessonId(long sessionLessonId) throws SessionLessonNotFoundException {
        SessionLessonModel sessionLessonModel = sessionLessonDao.findById(sessionLessonId);
        if (sessionLessonModel != null) {
            return createFromModel(sessionLessonModel);
        } else {
            throw new SessionLessonNotFoundException("Session Lesson Not Found");
        }
    }

    @Override
    public Page<SessionLesson> findSessionLessons(String sessionJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = sessionLessonDao.countByFilters(filterString, ImmutableMap.of(SessionLessonModel_.sessionJid, sessionJid), ImmutableMap.of());
        List<SessionLessonModel> sessionLessonModels = sessionLessonDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(SessionLessonModel_.sessionJid, sessionJid), ImmutableMap.of(), pageIndex * pageSize, pageSize);

        List<SessionLesson> sessionLessons = sessionLessonModels.stream().map(m -> createFromModel(m)).collect(Collectors.toList());

        return new Page<>(sessionLessons, totalPages, pageIndex, pageSize);
    }

    @Override
    public Page<SessionLessonProgress> findSessionLessons(String userJid, String sessionJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = sessionLessonDao.countByFilters(filterString, ImmutableMap.of(SessionLessonModel_.sessionJid, sessionJid), ImmutableMap.of());
        List<SessionLessonModel> sessionLessonModels = sessionLessonDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(SessionLessonModel_.sessionJid, sessionJid), ImmutableMap.of(), pageIndex * pageSize, pageSize);

        ImmutableList.Builder<SessionLessonProgress> sessionLessonProgressBuilder = ImmutableList.builder();
        for (SessionLessonModel sessionLessonModel :  sessionLessonModels) {
            LessonProgress progress = LessonProgress.NOT_VIEWED;
            if (userItemDao.existByUserJidAndItemJid(userJid, sessionLessonModel.lessonJid)) {
                progress = LessonProgress.VIEWED;
            }
            sessionLessonProgressBuilder.add(new SessionLessonProgress(createFromModel(sessionLessonModel), progress));
        }

        return new Page<>(sessionLessonProgressBuilder.build(), totalPages, pageIndex, pageSize);
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

    private SessionLesson createFromModel(SessionLessonModel model) {
        return new SessionLesson(model.id, model.sessionJid, model.lessonJid, model.lessonSecret, model.alias, SessionLessonStatus.valueOf(model.status));
    }
}
