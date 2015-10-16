package org.iatoki.judgels.jerahmeel.services.impls;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.iatoki.judgels.jerahmeel.CourseProgress;
import org.iatoki.judgels.jerahmeel.CurriculumCourse;
import org.iatoki.judgels.jerahmeel.CurriculumCourseNotFoundException;
import org.iatoki.judgels.jerahmeel.CurriculumCourseWithProgress;
import org.iatoki.judgels.jerahmeel.UserItemStatus;
import org.iatoki.judgels.jerahmeel.models.daos.CourseSessionDao;
import org.iatoki.judgels.jerahmeel.models.daos.CurriculumCourseDao;
import org.iatoki.judgels.jerahmeel.models.daos.SessionDependencyDao;
import org.iatoki.judgels.jerahmeel.models.daos.SessionProblemDao;
import org.iatoki.judgels.jerahmeel.models.daos.UserItemDao;
import org.iatoki.judgels.jerahmeel.models.entities.CourseSessionModel;
import org.iatoki.judgels.jerahmeel.models.entities.CourseSessionModel_;
import org.iatoki.judgels.jerahmeel.models.entities.CurriculumCourseModel;
import org.iatoki.judgels.jerahmeel.models.entities.CurriculumCourseModel_;
import org.iatoki.judgels.jerahmeel.models.entities.SessionDependencyModel;
import org.iatoki.judgels.jerahmeel.models.entities.SessionProblemModel;
import org.iatoki.judgels.jerahmeel.models.entities.SessionProblemModel_;
import org.iatoki.judgels.jerahmeel.models.entities.UserItemModel;
import org.iatoki.judgels.jerahmeel.services.CurriculumCourseService;
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
@Named("curriculumCourseService")
public final class CurriculumCourseServiceImpl implements CurriculumCourseService {

    private final CourseSessionDao courseSessionDao;
    private final CurriculumCourseDao curriculumCourseDao;
    private final SessionDependencyDao sessionDependencyDao;
    private final SessionProblemDao sessionProblemDao;
    private final UserItemDao userItemDao;

    @Inject
    public CurriculumCourseServiceImpl(CourseSessionDao courseSessionDao, CurriculumCourseDao curriculumCourseDao, SessionDependencyDao sessionDependencyDao, SessionProblemDao sessionProblemDao, UserItemDao userItemDao) {
        this.courseSessionDao = courseSessionDao;
        this.curriculumCourseDao = curriculumCourseDao;
        this.sessionDependencyDao = sessionDependencyDao;
        this.sessionProblemDao = sessionProblemDao;
        this.userItemDao = userItemDao;
    }

    @Override
    public boolean existsByCurriculumJidAndAlias(String curriculumJid, String alias) {
        return curriculumCourseDao.existsByCurriculumJidAndAlias(curriculumJid, alias);
    }

    @Override
    public boolean existsByCurriculumJidAndCourseJid(String curriculumJid, String courseJid) {
        return curriculumCourseDao.existsByCurriculumJidAndCourseJid(curriculumJid, courseJid);
    }

    @Override
    public CurriculumCourse findCurriculumCourseByCurriculumCourseId(long curriculumCourseId) throws CurriculumCourseNotFoundException {
        CurriculumCourseModel curriculumCourseModel = curriculumCourseDao.findById(curriculumCourseId);
        if (curriculumCourseModel != null) {
            return CurriculumCourseServiceUtils.createFromModel(curriculumCourseModel);
        } else {
            throw new CurriculumCourseNotFoundException("Curriculum Course Not Found.");
        }
    }

    @Override
    public Page<CurriculumCourse> getPageOfCurriculumCourses(String curriculumJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = curriculumCourseDao.countByFiltersEq(filterString, ImmutableMap.of(CurriculumCourseModel_.curriculumJid, curriculumJid));
        List<CurriculumCourseModel> curriculumCourseModels = curriculumCourseDao.findSortedByFiltersEq(orderBy, orderDir, filterString, ImmutableMap.of(CurriculumCourseModel_.curriculumJid, curriculumJid), pageIndex * pageSize, pageSize);

        List<CurriculumCourse> curriculumCourses = curriculumCourseModels.stream().map(m -> CurriculumCourseServiceUtils.createFromModel(m)).collect(Collectors.toList());

        return new Page<>(curriculumCourses, totalPages, pageIndex, pageSize);
    }

    @Override
    public Page<CurriculumCourseWithProgress> getPageOfCurriculumCoursesWithProgress(String userJid, String curriculumJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = curriculumCourseDao.countByFiltersEq(filterString, ImmutableMap.of(CurriculumCourseModel_.curriculumJid, curriculumJid));
        List<CurriculumCourseModel> curriculumCourseModels = curriculumCourseDao.findSortedByFiltersEq(orderBy, orderDir, filterString, ImmutableMap.of(CurriculumCourseModel_.curriculumJid, curriculumJid), pageIndex * pageSize, pageSize);

        List<String> courseJids = curriculumCourseModels.stream().map(m -> m.courseJid).collect(Collectors.toList());
        List<CourseSessionModel> courseSessionModels = courseSessionDao.findSortedByFiltersIn(orderBy, orderDir, "", ImmutableMap.of(CourseSessionModel_.courseJid, courseJids), 0, -1);
        Map<String, List<CourseSessionModel>> mapCourseJidToCourseSessionModels = Maps.newHashMap();
        for (CourseSessionModel courseSessionModel : courseSessionModels) {
            List<CourseSessionModel> value;
            if (mapCourseJidToCourseSessionModels.containsKey(courseSessionModel.courseJid)) {
                value = mapCourseJidToCourseSessionModels.get(courseSessionModel.courseJid);
            } else {
                value = Lists.newArrayList();
            }
            value.add(courseSessionModel);
            mapCourseJidToCourseSessionModels.put(courseSessionModel.courseJid, value);
        }

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

        ImmutableList.Builder<CurriculumCourseWithProgress> curriculumCourseProgressBuilder = ImmutableList.builder();
        for (CurriculumCourseModel curriculumCourseModel : curriculumCourseModels) {
            List<CourseSessionModel> currentCourseSessionModels = mapCourseJidToCourseSessionModels.get(curriculumCourseModel.courseJid);

            if (currentCourseSessionModels == null) {
                currentCourseSessionModels = ImmutableList.of();
            }

            CourseProgressWithCompleted courseProgressWithCompleted = getUserProgressFromCourseSessionModels(userJid, currentCourseSessionModels);

            double totalScore = SessionScoreCacheUtils.getInstance().getUserTotalScoreFromCourseSessionModels(userJid, curriculumCourseModel.courseJid, currentCourseSessionModels, mapSessionJidToSessionProblemModels);

            curriculumCourseProgressBuilder.add(new CurriculumCourseWithProgress(CurriculumCourseServiceUtils.createFromModel(curriculumCourseModel), courseProgressWithCompleted.courseProgress, courseProgressWithCompleted.completed, courseSessionModels.size(), totalScore));
        }

        return new Page<>(curriculumCourseProgressBuilder.build(), totalPages, pageIndex, pageSize);
    }

    @Override
    public CurriculumCourse addCurriculumCourse(String curriculumJid, String courseJid, String alias, String userJid, String userIpAddress) {
        CurriculumCourseModel curriculumCourseModel = new CurriculumCourseModel();
        curriculumCourseModel.curriculumJid = curriculumJid;
        curriculumCourseModel.courseJid = courseJid;
        curriculumCourseModel.alias = alias;

        curriculumCourseDao.persist(curriculumCourseModel, userJid, userIpAddress);

        return CurriculumCourseServiceUtils.createFromModel(curriculumCourseModel);
    }

    @Override
    public void updateCurriculumCourse(long curriculumCourseId, String alias, String userJid, String userIpAddress) {
        CurriculumCourseModel curriculumCourseModel = curriculumCourseDao.findById(curriculumCourseId);
        curriculumCourseModel.alias = alias;

        curriculumCourseDao.edit(curriculumCourseModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void removeCurriculumCourse(long curriculumCourseId) {
        CurriculumCourseModel curriculumCourseModel = curriculumCourseDao.findById(curriculumCourseId);

        curriculumCourseDao.remove(curriculumCourseModel);
    }

    private CourseProgressWithCompleted getUserProgressFromCourseSessionModels(String userJid, List<CourseSessionModel> courseSessionModels) {
        List<UserItemModel> completedUserItemModel = userItemDao.getByUserJidAndStatus(userJid, UserItemStatus.COMPLETED.name());
        Set<String> completedJids = completedUserItemModel.stream().map(m -> m.itemJid).collect(Collectors.toSet());
        List<UserItemModel> onProgressUserItemModel = userItemDao.getByUserJidAndStatus(userJid, UserItemStatus.VIEWED.name());
        Set<String> onProgressJids = onProgressUserItemModel.stream().map(m -> m.itemJid).collect(Collectors.toSet());

        int completed = 0;
        CourseProgress progress = CourseProgress.LOCKED;
        for (CourseSessionModel courseSessionModel : courseSessionModels) {
            if (completedJids.contains(courseSessionModel.sessionJid)) {
                progress = CourseProgress.IN_PROGRESS;
                completed++;
            } else if (onProgressJids.contains(courseSessionModel.sessionJid)) {
                progress = CourseProgress.IN_PROGRESS;
                break;
            } else {
                List<SessionDependencyModel> sessionDependencyModels = sessionDependencyDao.getBySessionJid(courseSessionModel.sessionJid);
                Set<String> dependencyJids = sessionDependencyModels.stream().map(m -> m.dependedSessionJid).collect(Collectors.toSet());
                dependencyJids.removeAll(completedJids);
                if (dependencyJids.isEmpty() && progress.equals(CourseProgress.LOCKED)) {
                    progress = CourseProgress.AVAILABLE;
                    break;
                }
            }
        }
        if (completed == courseSessionModels.size()) {
            progress = CourseProgress.COMPLETED;
        }

        return new CourseProgressWithCompleted(completed, progress);
    }

    private class CourseProgressWithCompleted {
        private final long completed;
        private final CourseProgress courseProgress;

        public CourseProgressWithCompleted(long completed, CourseProgress courseProgress) {
            this.completed = completed;
            this.courseProgress = courseProgress;
        }
    }
}
