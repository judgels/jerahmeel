package org.iatoki.judgels.jerahmeel.services.impls;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.iatoki.judgels.jerahmeel.LessonProgress;
import org.iatoki.judgels.jerahmeel.SessionLesson;
import org.iatoki.judgels.jerahmeel.SessionLessonNotFoundException;
import org.iatoki.judgels.jerahmeel.SessionLessonWithProgress;
import org.iatoki.judgels.jerahmeel.SessionLessonStatus;
import org.iatoki.judgels.jerahmeel.models.daos.SessionLessonDao;
import org.iatoki.judgels.jerahmeel.models.daos.UserItemDao;
import org.iatoki.judgels.jerahmeel.models.entities.SessionLessonModel;
import org.iatoki.judgels.jerahmeel.models.entities.SessionLessonModel_;
import org.iatoki.judgels.jerahmeel.services.SessionLessonService;
import org.iatoki.judgels.play.Page;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
@Named("sessionLessonService")
public final class SessionLessonServiceImpl implements SessionLessonService {

    private final SessionLessonDao sessionLessonDao;
    private final UserItemDao userItemDao;

    @Inject
    public SessionLessonServiceImpl(SessionLessonDao sessionLessonDao, UserItemDao userItemDao) {
        this.sessionLessonDao = sessionLessonDao;
        this.userItemDao = userItemDao;
    }

    @Override
    public boolean aliasExistsInSession(String sessionJid, String alias) {
        return sessionLessonDao.existsBySessionJidAndAlias(sessionJid, alias);
    }

    @Override
    public SessionLesson findSessionLessonById(long sessionLessonId) throws SessionLessonNotFoundException {
        SessionLessonModel sessionLessonModel = sessionLessonDao.findById(sessionLessonId);
        if (sessionLessonModel != null) {
            return SessionLessonServiceUtils.createFromModel(sessionLessonModel);
        } else {
            throw new SessionLessonNotFoundException("Session Lesson Not Found");
        }
    }

    @Override
    public Page<SessionLesson> getPageOfSessionLessons(String sessionJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = sessionLessonDao.countByFiltersEq(filterString, ImmutableMap.of(SessionLessonModel_.sessionJid, sessionJid));
        List<SessionLessonModel> sessionLessonModels = sessionLessonDao.findSortedByFiltersEq(orderBy, orderDir, filterString, ImmutableMap.of(SessionLessonModel_.sessionJid, sessionJid), pageIndex * pageSize, pageSize);

        List<SessionLesson> sessionLessons = sessionLessonModels.stream().map(m -> SessionLessonServiceUtils.createFromModel(m)).collect(Collectors.toList());

        return new Page<>(sessionLessons, totalPages, pageIndex, pageSize);
    }

    @Override
    public Page<SessionLessonWithProgress> getPageOfSessionLessonsWithProgress(String userJid, String sessionJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = sessionLessonDao.countByFiltersEq(filterString, ImmutableMap.of(SessionLessonModel_.sessionJid, sessionJid, SessionLessonModel_.status, SessionLessonStatus.VISIBLE.name()));
        List<SessionLessonModel> sessionLessonModels = sessionLessonDao.findSortedByFiltersEq(orderBy, orderDir, filterString, ImmutableMap.of(SessionLessonModel_.sessionJid, sessionJid, SessionLessonModel_.status, SessionLessonStatus.VISIBLE.name()), pageIndex * pageSize, pageSize);

        ImmutableList.Builder<SessionLessonWithProgress> sessionLessonProgressBuilder = ImmutableList.builder();
        for (SessionLessonModel sessionLessonModel :  sessionLessonModels) {
            LessonProgress progress = LessonProgress.NOT_VIEWED;
            if (userItemDao.existsByUserJidAndItemJid(userJid, sessionLessonModel.lessonJid)) {
                progress = LessonProgress.VIEWED;
            }
            sessionLessonProgressBuilder.add(new SessionLessonWithProgress(SessionLessonServiceUtils.createFromModel(sessionLessonModel), progress));
        }

        return new Page<>(sessionLessonProgressBuilder.build(), totalPages, pageIndex, pageSize);
    }

    @Override
    public void addSessionLesson(String sessionJid, String lessonJid, String lessonSecret, String alias, SessionLessonStatus status, String userJid, String userIpAddress) {
        SessionLessonModel sessionLessonModel = new SessionLessonModel();
        sessionLessonModel.sessionJid = sessionJid;
        sessionLessonModel.lessonJid = lessonJid;
        sessionLessonModel.lessonSecret = lessonSecret;
        sessionLessonModel.alias = alias;
        sessionLessonModel.status = status.name();

        sessionLessonDao.persist(sessionLessonModel, userJid, userIpAddress);
    }

    @Override
    public void updateSessionLesson(long sessionLessonId, String alias, SessionLessonStatus status, String userJid, String userIpAddress) {
        SessionLessonModel sessionLessonModel = sessionLessonDao.findById(sessionLessonId);
        sessionLessonModel.alias = alias;
        sessionLessonModel.status = status.name();

        sessionLessonDao.edit(sessionLessonModel, userJid, userIpAddress);
    }

    @Override
    public void removeSessionLesson(long sessionLessonId) {
        SessionLessonModel sessionLessonModel = sessionLessonDao.findById(sessionLessonId);

        sessionLessonDao.remove(sessionLessonModel);
    }
}
