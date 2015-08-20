package org.iatoki.judgels.jerahmeel.services.impls;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.jerahmeel.LessonProgress;
import org.iatoki.judgels.jerahmeel.SessionLesson;
import org.iatoki.judgels.jerahmeel.SessionLessonNotFoundException;
import org.iatoki.judgels.jerahmeel.SessionLessonProgress;
import org.iatoki.judgels.jerahmeel.SessionLessonStatus;
import org.iatoki.judgels.jerahmeel.models.daos.SessionLessonDao;
import org.iatoki.judgels.jerahmeel.models.daos.UserItemDao;
import org.iatoki.judgels.jerahmeel.models.entities.SessionLessonModel;
import org.iatoki.judgels.jerahmeel.models.entities.SessionLessonModel_;
import org.iatoki.judgels.jerahmeel.services.SessionLessonService;

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
            return createFromModel(sessionLessonModel);
        } else {
            throw new SessionLessonNotFoundException("Session Lesson Not Found");
        }
    }

    @Override
    public Page<SessionLesson> getPageOfSessionLessons(String sessionJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = sessionLessonDao.countByFilters(filterString, ImmutableMap.of(SessionLessonModel_.sessionJid, sessionJid), ImmutableMap.of());
        List<SessionLessonModel> sessionLessonModels = sessionLessonDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(SessionLessonModel_.sessionJid, sessionJid), ImmutableMap.of(), pageIndex * pageSize, pageSize);

        List<SessionLesson> sessionLessons = sessionLessonModels.stream().map(m -> createFromModel(m)).collect(Collectors.toList());

        return new Page<>(sessionLessons, totalPages, pageIndex, pageSize);
    }

    @Override
    public Page<SessionLessonProgress> getPageOfSessionLessonsProgress(String userJid, String sessionJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = sessionLessonDao.countByFilters(filterString, ImmutableMap.of(SessionLessonModel_.sessionJid, sessionJid), ImmutableMap.of());
        List<SessionLessonModel> sessionLessonModels = sessionLessonDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(SessionLessonModel_.sessionJid, sessionJid), ImmutableMap.of(), pageIndex * pageSize, pageSize);

        ImmutableList.Builder<SessionLessonProgress> sessionLessonProgressBuilder = ImmutableList.builder();
        for (SessionLessonModel sessionLessonModel :  sessionLessonModels) {
            LessonProgress progress = LessonProgress.NOT_VIEWED;
            if (userItemDao.existsByUserJidAndItemJid(userJid, sessionLessonModel.lessonJid)) {
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
    public void updateSessionLesson(long sessionLessonId, String alias, SessionLessonStatus status) {
        SessionLessonModel sessionLessonModel = sessionLessonDao.findById(sessionLessonId);
        sessionLessonModel.alias = alias;
        sessionLessonModel.status = status.name();

        sessionLessonDao.edit(sessionLessonModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
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
