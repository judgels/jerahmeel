package org.iatoki.judgels.jerahmeel.services.impls;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.iatoki.judgels.jerahmeel.CourseSession;
import org.iatoki.judgels.jerahmeel.CourseSessionNotFoundException;
import org.iatoki.judgels.jerahmeel.CourseSessionProgress;
import org.iatoki.judgels.jerahmeel.SessionProgress;
import org.iatoki.judgels.jerahmeel.UserItemStatus;
import org.iatoki.judgels.jerahmeel.models.daos.BundleGradingDao;
import org.iatoki.judgels.jerahmeel.models.daos.BundleSubmissionDao;
import org.iatoki.judgels.jerahmeel.models.daos.CourseSessionDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProgrammingGradingDao;
import org.iatoki.judgels.jerahmeel.models.daos.SessionDao;
import org.iatoki.judgels.jerahmeel.models.daos.SessionDependencyDao;
import org.iatoki.judgels.jerahmeel.models.daos.SessionProblemDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProgrammingSubmissionDao;
import org.iatoki.judgels.jerahmeel.models.daos.UserItemDao;
import org.iatoki.judgels.jerahmeel.models.entities.BundleGradingModel;
import org.iatoki.judgels.jerahmeel.models.entities.BundleSubmissionModel;
import org.iatoki.judgels.jerahmeel.models.entities.CourseSessionModel;
import org.iatoki.judgels.jerahmeel.models.entities.CourseSessionModel_;
import org.iatoki.judgels.jerahmeel.models.entities.ProgrammingGradingModel;
import org.iatoki.judgels.jerahmeel.models.entities.SessionDependencyModel;
import org.iatoki.judgels.jerahmeel.models.entities.SessionProblemModel;
import org.iatoki.judgels.jerahmeel.models.entities.ProgrammingSubmissionModel;
import org.iatoki.judgels.jerahmeel.models.entities.UserItemModel;
import org.iatoki.judgels.jerahmeel.services.CourseSessionService;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.sandalphon.ProblemType;

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
    private final SessionDao sessionDao;
    private final SessionDependencyDao sessionDependencyDao;
    private final SessionProblemDao sessionProblemDao;
    private final BundleSubmissionDao bundleSubmissionDao;
    private final BundleGradingDao bundleGradingDao;
    private final ProgrammingSubmissionDao programmingSubmissionDao;
    private final ProgrammingGradingDao programmingGradingDao;
    private final UserItemDao userItemDao;

    @Inject
    public CourseSessionServiceImpl(CourseSessionDao courseSessionDao, SessionDao sessionDao, SessionDependencyDao sessionDependencyDao, SessionProblemDao sessionProblemDao, BundleSubmissionDao bundleSubmissionDao, BundleGradingDao bundleGradingDao, ProgrammingSubmissionDao programmingSubmissionDao, ProgrammingGradingDao programmingGradingDao, UserItemDao userItemDao) {
        this.courseSessionDao = courseSessionDao;
        this.sessionDao = sessionDao;
        this.sessionDependencyDao = sessionDependencyDao;
        this.sessionProblemDao = sessionProblemDao;
        this.bundleSubmissionDao = bundleSubmissionDao;
        this.bundleGradingDao = bundleGradingDao;
        this.programmingSubmissionDao = programmingSubmissionDao;
        this.programmingGradingDao = programmingGradingDao;
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
            return CourseSessionServiceUtils.createFromModel(sessionDao, courseSessionModel);
        } else {
            throw new CourseSessionNotFoundException("Course Session Not Found.");
        }
    }

    @Override
    public Page<CourseSession> getPageOfCourseSessions(String courseJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = courseSessionDao.countByFilters(filterString, ImmutableMap.of(CourseSessionModel_.courseJid, courseJid), ImmutableMap.of());
        List<CourseSessionModel> courseSessionModels = courseSessionDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(CourseSessionModel_.courseJid, courseJid), ImmutableMap.of(), pageIndex * pageSize, pageSize);

        List<CourseSession> courseSessions = courseSessionModels.stream().map(m -> CourseSessionServiceUtils.createFromModel(sessionDao, m)).collect(Collectors.toList());

        return new Page<>(courseSessions, totalPages, pageIndex, pageSize);
    }

    @Override
    public Page<CourseSessionProgress> getPageOfCourseSessionsProgress(String userJid, String courseJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = courseSessionDao.countByFilters(filterString, ImmutableMap.of(CourseSessionModel_.courseJid, courseJid), ImmutableMap.of());
        List<CourseSessionModel> courseSessionModels = courseSessionDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(CourseSessionModel_.courseJid, courseJid), ImmutableMap.of(), pageIndex * pageSize, pageSize);

        ImmutableList.Builder<CourseSessionProgress> courseSessionProgressBuilder = ImmutableList.builder();
        List<UserItemModel> completedUserItemModel = userItemDao.getByUserJidAndStatus(userJid, UserItemStatus.COMPLETED.name());
        Set<String> completedJids = completedUserItemModel.stream().map(m -> m.itemJid).collect(Collectors.toSet());
        List<UserItemModel> onProgressUserItemModel = userItemDao.getByUserJidAndStatus(userJid, UserItemStatus.VIEWED.name());
        Set<String> onProgressJids = onProgressUserItemModel.stream().map(m -> m.itemJid).collect(Collectors.toSet());
        for (CourseSessionModel courseSessionModel : courseSessionModels) {
            List<SessionProblemModel> sessionProblemModels = sessionProblemDao.getBySessionJid(courseSessionModel.sessionJid);

            long totalProblems = sessionProblemModels.size();
            long solvedProblems = 0;
            double totalScore = 0;
            for (SessionProblemModel sessionProblemModel : sessionProblemModels) {
                if (userItemDao.existsByUserJidItemJidAndStatus(IdentityUtils.getUserJid(), sessionProblemModel.problemJid, UserItemStatus.COMPLETED.name())) {
                    solvedProblems++;
                }
                if (sessionProblemModel.type.equals(ProblemType.BUNDLE.name())) {
                    double maxScore = 0;
                    List<BundleSubmissionModel> bundleSubmissionModels = bundleSubmissionDao.getByContainerJidAndUserJidAndProblemJid(sessionProblemModel.sessionJid, userJid, sessionProblemModel.problemJid);
                    Map<String, List<BundleGradingModel>> bundleGradingModels = bundleGradingDao.getBySubmissionJids(bundleSubmissionModels.stream().map(m -> m.jid).collect(Collectors.toList()));
                    for (String submissionJid : bundleGradingModels.keySet()) {
                        double submissionScore = bundleGradingModels.get(submissionJid).get(bundleGradingModels.get(submissionJid).size() - 1).score;
                        if (submissionScore > maxScore) {
                            maxScore = submissionScore;
                        }
                    }
                    totalScore += maxScore;
                } else if (sessionProblemModel.type.equals(ProblemType.PROGRAMMING.name())) {
                    double maxScore = 0;
                    List<ProgrammingSubmissionModel> programmingSubmissionModels = programmingSubmissionDao.getByContainerJidAndUserJidAndProblemJid(sessionProblemModel.sessionJid, userJid, sessionProblemModel.problemJid);
                    Map<String, List<ProgrammingGradingModel>> gradingModels = programmingGradingDao.getBySubmissionJids(programmingSubmissionModels.stream().map(m -> m.jid).collect(Collectors.toList()));
                    for (String submissionJid : gradingModels.keySet()) {
                        double submissionScore = gradingModels.get(submissionJid).get(gradingModels.get(submissionJid).size() - 1).score;
                        if (submissionScore > maxScore) {
                            maxScore = submissionScore;
                        }
                    }
                    totalScore += maxScore;
                }
            }

            SessionProgress progress = SessionProgress.LOCKED;
            if ((completedJids.contains(courseSessionModel.sessionJid)) && (courseSessionModel.completeable)) {
                progress = SessionProgress.COMPLETED;
            } else if ((onProgressJids.contains(courseSessionModel.sessionJid)) && (courseSessionModel.completeable)) {
                progress = SessionProgress.IN_PROGRESS;
            } else {
                List<SessionDependencyModel> sessionDependencyModels = sessionDependencyDao.getBySessionJid(courseSessionModel.sessionJid);
                Set<String> dependencyJids = sessionDependencyModels.stream().map(m -> m.dependedSessionJid).collect(Collectors.toSet());
                dependencyJids.removeAll(completedJids);
                if (dependencyJids.isEmpty()) {
                    progress = SessionProgress.AVAILABLE;
                }
            }
            courseSessionProgressBuilder.add(new CourseSessionProgress(CourseSessionServiceUtils.createFromModel(sessionDao, courseSessionModel), progress, solvedProblems, totalProblems, totalScore));
        }

        return new Page<>(courseSessionProgressBuilder.build(), totalPages, pageIndex, pageSize);
    }

    @Override
    public void addCourseSession(String courseJid, String sessionJid, String alias, boolean completeable, String userJid, String userIpAddress) {
        CourseSessionModel courseSessionModel = new CourseSessionModel();
        courseSessionModel.courseJid = courseJid;
        courseSessionModel.sessionJid = sessionJid;
        courseSessionModel.alias = alias;
        courseSessionModel.completeable = completeable;

        courseSessionDao.persist(courseSessionModel, userJid, userIpAddress);
    }

    @Override
    public void updateCourseSession(long courseSessionId, String alias, boolean completeable, String userJid, String userIpAddress) {
        CourseSessionModel courseSessionModel = courseSessionDao.findById(courseSessionId);
        courseSessionModel.alias = alias;
        courseSessionModel.completeable = completeable;

        courseSessionDao.edit(courseSessionModel, userJid, userIpAddress);
    }

    @Override
    public void removeCourseSession(long courseSessionId) {
        CourseSessionModel courseSessionModel = courseSessionDao.findById(courseSessionId);

        courseSessionDao.remove(courseSessionModel);
    }

}
