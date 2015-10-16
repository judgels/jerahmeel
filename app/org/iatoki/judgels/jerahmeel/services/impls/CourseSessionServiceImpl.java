package org.iatoki.judgels.jerahmeel.services.impls;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.iatoki.judgels.jerahmeel.CourseSession;
import org.iatoki.judgels.jerahmeel.CourseSessionNotFoundException;
import org.iatoki.judgels.jerahmeel.CourseSessionWithProgress;
import org.iatoki.judgels.jerahmeel.SessionProgress;
import org.iatoki.judgels.jerahmeel.UserItemStatus;
import org.iatoki.judgels.jerahmeel.models.daos.CourseSessionDao;
import org.iatoki.judgels.jerahmeel.models.daos.SessionDependencyDao;
import org.iatoki.judgels.jerahmeel.models.daos.SessionProblemDao;
import org.iatoki.judgels.jerahmeel.models.daos.UserItemDao;
import org.iatoki.judgels.jerahmeel.models.entities.CourseSessionModel;
import org.iatoki.judgels.jerahmeel.models.entities.CourseSessionModel_;
import org.iatoki.judgels.jerahmeel.models.entities.SessionDependencyModel;
import org.iatoki.judgels.jerahmeel.models.entities.SessionProblemModel;
import org.iatoki.judgels.jerahmeel.models.entities.SessionProblemModel_;
import org.iatoki.judgels.jerahmeel.models.entities.UserItemModel;
import org.iatoki.judgels.jerahmeel.services.CourseSessionService;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.Page;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
@Named("courseSessionService")
public final class CourseSessionServiceImpl implements CourseSessionService {

    private final CourseSessionDao courseSessionDao;
    private final SessionDependencyDao sessionDependencyDao;
    private final SessionProblemDao sessionProblemDao;
    private final UserItemDao userItemDao;

    @Inject
    public CourseSessionServiceImpl(CourseSessionDao courseSessionDao, SessionDependencyDao sessionDependencyDao, SessionProblemDao sessionProblemDao, UserItemDao userItemDao) {
        this.courseSessionDao = courseSessionDao;
        this.sessionDependencyDao = sessionDependencyDao;
        this.sessionProblemDao = sessionProblemDao;
        this.userItemDao = userItemDao;
    }

    @Override
    public boolean existsByCourseJidAndAlias(String courseJid, String alias) {
        return courseSessionDao.existsByCourseJidAndAlias(courseJid, alias);
    }

    @Override
    public boolean existsByCourseJidAndSessionJid(String courseJid, String sessionJid) {
        return courseSessionDao.existsByCourseJidAndSessionJid(courseJid, sessionJid);
    }

    @Override
    public CourseSession findCourseSessionById(long courseSessionId) throws CourseSessionNotFoundException {
        CourseSessionModel courseSessionModel = courseSessionDao.findById(courseSessionId);
        if (courseSessionModel != null) {
            return CourseSessionServiceUtils.createFromModel(courseSessionModel);
        } else {
            throw new CourseSessionNotFoundException("Course Session Not Found.");
        }
    }

    @Override
    public Page<CourseSession> getPageOfCourseSessions(String courseJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = courseSessionDao.countByFiltersEq(filterString, ImmutableMap.of(CourseSessionModel_.courseJid, courseJid));
        List<CourseSessionModel> courseSessionModels = courseSessionDao.findSortedByFiltersEq(orderBy, orderDir, filterString, ImmutableMap.of(CourseSessionModel_.courseJid, courseJid), pageIndex * pageSize, pageSize);

        List<CourseSession> courseSessions = courseSessionModels.stream().map(m -> CourseSessionServiceUtils.createFromModel(m)).collect(Collectors.toList());

        return new Page<>(courseSessions, totalPages, pageIndex, pageSize);
    }

    @Override
    public Page<CourseSessionWithProgress> getPageOfCourseSessionsWithProgress(String userJid, String courseJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = courseSessionDao.countByFiltersEq(filterString, ImmutableMap.of(CourseSessionModel_.courseJid, courseJid));
        List<CourseSessionModel> courseSessionModels = courseSessionDao.findSortedByFiltersEq(orderBy, orderDir, filterString, ImmutableMap.of(CourseSessionModel_.courseJid, courseJid), pageIndex * pageSize, pageSize);

        List<String> sessionJids = courseSessionModels.stream().map(m -> m.sessionJid).collect(Collectors.toList());
        List<SessionProblemModel> sessionProblemModels = sessionProblemDao.findSortedByFiltersIn(orderBy, orderDir, "", ImmutableMap.of(SessionProblemModel_.sessionJid, sessionJids), 0, -1);
        Map<String, List<SessionProblemModel>> mapSessionJidToSessionProblemModels = Maps.newHashMap();
        for (SessionProblemModel sessionProblemModel : sessionProblemModels) {
            List<SessionProblemModel> value;
            if (mapSessionJidToSessionProblemModels.containsKey(sessionProblemModel.sessionJid)) {
                value = mapSessionJidToSessionProblemModels.get(sessionProblemModel.sessionJid);
            } else {
                value = Lists.newArrayList();
            }
            value.add(sessionProblemModel);
            mapSessionJidToSessionProblemModels.put(sessionProblemModel.sessionJid, value);
        }

        ImmutableList.Builder<CourseSessionWithProgress> courseSessionProgressBuilder = ImmutableList.builder();
        List<UserItemModel> completedUserItemModel = userItemDao.getByUserJidAndStatus(userJid, UserItemStatus.COMPLETED.name());
        Set<String> completedJids = completedUserItemModel.stream().map(m -> m.itemJid).collect(Collectors.toSet());
        List<UserItemModel> onProgressUserItemModel = userItemDao.getByUserJidAndStatus(userJid, UserItemStatus.VIEWED.name());
        Set<String> onProgressJids = onProgressUserItemModel.stream().map(m -> m.itemJid).collect(Collectors.toSet());

        for (CourseSessionModel courseSessionModel : courseSessionModels) {
            List<SessionProblemModel> currentSessionProblemModels = mapSessionJidToSessionProblemModels.get(courseSessionModel.sessionJid);

            if (currentSessionProblemModels == null) {
                currentSessionProblemModels = ImmutableList.of();
            }

            long totalProblems = currentSessionProblemModels.size();
            long solvedProblems = 0;
            double totalScore = SessionScoreCacheUtils.getInstance().getUserTotalScoreFromSessionProblemModels(userJid, courseSessionModel.sessionJid, currentSessionProblemModels);
            for (SessionProblemModel sessionProblemModel : currentSessionProblemModels) {
                if (userItemDao.existsByUserJidItemJidAndStatus(IdentityUtils.getUserJid(), sessionProblemModel.problemJid, UserItemStatus.COMPLETED.name())) {
                    solvedProblems++;
                }
            }

            SessionProgress progress = SessionProgress.LOCKED;
            if (completedJids.contains(courseSessionModel.sessionJid)) {
                progress = SessionProgress.COMPLETED;
            } else if (onProgressJids.contains(courseSessionModel.sessionJid)) {
                progress = SessionProgress.IN_PROGRESS;
            } else {
                List<SessionDependencyModel> sessionDependencyModels = sessionDependencyDao.getBySessionJid(courseSessionModel.sessionJid);
                Set<String> dependencyJids = sessionDependencyModels.stream().map(m -> m.dependedSessionJid).collect(Collectors.toSet());
                dependencyJids.removeAll(completedJids);
                if (dependencyJids.isEmpty()) {
                    progress = SessionProgress.AVAILABLE;
                }
            }
            courseSessionProgressBuilder.add(new CourseSessionWithProgress(CourseSessionServiceUtils.createFromModel(courseSessionModel), progress, solvedProblems, totalProblems, totalScore));
        }

        return new Page<>(courseSessionProgressBuilder.build(), totalPages, pageIndex, pageSize);
    }

    @Override
    public CourseSession addCourseSession(String courseJid, String sessionJid, String alias, String userJid, String userIpAddress) {
        CourseSessionModel courseSessionModel = new CourseSessionModel();
        courseSessionModel.courseJid = courseJid;
        courseSessionModel.sessionJid = sessionJid;
        courseSessionModel.alias = alias;

        courseSessionDao.persist(courseSessionModel, userJid, userIpAddress);

        return CourseSessionServiceUtils.createFromModel(courseSessionModel);
    }

    @Override
    public void updateCourseSession(long courseSessionId, String alias, String userJid, String userIpAddress) {
        CourseSessionModel courseSessionModel = courseSessionDao.findById(courseSessionId);
        courseSessionModel.alias = alias;

        courseSessionDao.edit(courseSessionModel, userJid, userIpAddress);
    }

    @Override
    public void removeCourseSession(long courseSessionId) {
        CourseSessionModel courseSessionModel = courseSessionDao.findById(courseSessionId);

        courseSessionDao.remove(courseSessionModel);
    }
}
