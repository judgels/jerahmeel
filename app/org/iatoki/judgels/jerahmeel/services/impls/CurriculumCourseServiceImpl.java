package org.iatoki.judgels.jerahmeel.services.impls;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.iatoki.judgels.jerahmeel.models.daos.BundleGradingDao;
import org.iatoki.judgels.jerahmeel.models.daos.BundleSubmissionDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProgrammingGradingDao;
import org.iatoki.judgels.jerahmeel.models.daos.SessionProblemDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProgrammingSubmissionDao;
import org.iatoki.judgels.jerahmeel.models.entities.BundleGradingModel;
import org.iatoki.judgels.jerahmeel.models.entities.BundleSubmissionModel;
import org.iatoki.judgels.jerahmeel.models.entities.ProgrammingGradingModel;
import org.iatoki.judgels.jerahmeel.models.entities.SessionProblemModel;
import org.iatoki.judgels.jerahmeel.models.entities.ProgrammingSubmissionModel;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.jerahmeel.CourseProgress;
import org.iatoki.judgels.jerahmeel.CurriculumCourse;
import org.iatoki.judgels.jerahmeel.CurriculumCourseNotFoundException;
import org.iatoki.judgels.jerahmeel.CurriculumCourseProgress;
import org.iatoki.judgels.jerahmeel.UserItemStatus;
import org.iatoki.judgels.jerahmeel.models.daos.CourseDao;
import org.iatoki.judgels.jerahmeel.models.daos.CourseSessionDao;
import org.iatoki.judgels.jerahmeel.models.daos.CurriculumCourseDao;
import org.iatoki.judgels.jerahmeel.models.daos.SessionDependencyDao;
import org.iatoki.judgels.jerahmeel.models.daos.UserItemDao;
import org.iatoki.judgels.jerahmeel.models.entities.CourseSessionModel;
import org.iatoki.judgels.jerahmeel.models.entities.CourseSessionModel_;
import org.iatoki.judgels.jerahmeel.models.entities.CurriculumCourseModel;
import org.iatoki.judgels.jerahmeel.models.entities.CurriculumCourseModel_;
import org.iatoki.judgels.jerahmeel.models.entities.SessionDependencyModel;
import org.iatoki.judgels.jerahmeel.models.entities.UserItemModel;
import org.iatoki.judgels.jerahmeel.services.CurriculumCourseService;
import org.iatoki.judgels.sandalphon.ProblemType;

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

    private final CurriculumCourseDao curriculumCourseDao;
    private final CourseDao courseDao;
    private final CourseSessionDao courseSessionDao;
    private final SessionDependencyDao sessionDependencyDao;
    private final SessionProblemDao sessionProblemDao;
    private final BundleSubmissionDao bundleSubmissionDao;
    private final BundleGradingDao bundleGradingDao;
    private final ProgrammingSubmissionDao programmingSubmissionDao;
    private final ProgrammingGradingDao programmingGradingDao;
    private final UserItemDao userItemDao;

    @Inject
    public CurriculumCourseServiceImpl(CurriculumCourseDao curriculumCourseDao, CourseDao courseDao, CourseSessionDao courseSessionDao, SessionDependencyDao sessionDependencyDao, SessionProblemDao sessionProblemDao, BundleSubmissionDao bundleSubmissionDao, BundleGradingDao bundleGradingDao, ProgrammingSubmissionDao programmingSubmissionDao, ProgrammingGradingDao programmingGradingDao, UserItemDao userItemDao) {
        this.curriculumCourseDao = curriculumCourseDao;
        this.courseDao = courseDao;
        this.courseSessionDao = courseSessionDao;
        this.sessionDependencyDao = sessionDependencyDao;
        this.sessionProblemDao = sessionProblemDao;
        this.bundleSubmissionDao = bundleSubmissionDao;
        this.bundleGradingDao = bundleGradingDao;
        this.programmingSubmissionDao = programmingSubmissionDao;
        this.programmingGradingDao = programmingGradingDao;
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
            return createFromModel(curriculumCourseModel);
        } else {
            throw new CurriculumCourseNotFoundException("Curriculum Course Not Found.");
        }
    }

    @Override
    public Page<CurriculumCourse> getPageOfCurriculumCourses(String curriculumJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = curriculumCourseDao.countByFilters(filterString, ImmutableMap.of(CurriculumCourseModel_.curriculumJid, curriculumJid), ImmutableMap.of());
        List<CurriculumCourseModel> curriculumCourseModels = curriculumCourseDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(CurriculumCourseModel_.curriculumJid, curriculumJid), ImmutableMap.of(), pageIndex * pageSize, pageSize);

        List<CurriculumCourse> curriculumCourses = curriculumCourseModels.stream().map(m -> createFromModel(m)).collect(Collectors.toList());

        return new Page<>(curriculumCourses, totalPages, pageIndex, pageSize);
    }

    @Override
    public Page<CurriculumCourseProgress> getPageOfCurriculumCoursesProgress(String userJid, String curriculumJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = curriculumCourseDao.countByFilters(filterString, ImmutableMap.of(CurriculumCourseModel_.curriculumJid, curriculumJid), ImmutableMap.of());
        List<CurriculumCourseModel> curriculumCourseModels = curriculumCourseDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(CurriculumCourseModel_.curriculumJid, curriculumJid), ImmutableMap.of(), pageIndex * pageSize, pageSize);

        ImmutableList.Builder<CurriculumCourseProgress> curriculumCourseProgressBuilder = ImmutableList.builder();
        List<UserItemModel> completedUserItemModel = userItemDao.getByUserJidAndStatus(userJid, UserItemStatus.COMPLETED.name());
        Set<String> completedJids = completedUserItemModel.stream().map(m -> m.itemJid).collect(Collectors.toSet());
        List<UserItemModel> onProgressUserItemModel = userItemDao.getByUserJidAndStatus(userJid, UserItemStatus.VIEWED.name());
        Set<String> onProgressJids = onProgressUserItemModel.stream().map(m -> m.itemJid).collect(Collectors.toSet());
        for (CurriculumCourseModel curriculumCourseModel : curriculumCourseModels) {
            List<CourseSessionModel> courseSessionModels = courseSessionDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(CourseSessionModel_.courseJid, curriculumCourseModel.courseJid), ImmutableMap.of(), 0, -1);
            int completed = 0;
            CourseProgress progress = CourseProgress.LOCKED;
            for (CourseSessionModel courseSessionModel : courseSessionModels) {
                if ((completedJids.contains(courseSessionModel.sessionJid)) && (curriculumCourseModel.completeable)) {
                    progress = CourseProgress.IN_PROGRESS;
                    completed++;
                } else if ((onProgressJids.contains(courseSessionModel.sessionJid)) && (curriculumCourseModel.completeable)) {
                    progress = CourseProgress.IN_PROGRESS;
                    break;
                } else {
                    List<SessionDependencyModel> sessionDependencyModels = sessionDependencyDao.getBySessionJid(courseSessionModel.sessionJid);
                    Set<String> dependencyJids = sessionDependencyModels.stream().map(m -> m.dependedSessionJid).collect(Collectors.toSet());
                    dependencyJids.removeAll(completedJids);
                    if (dependencyJids.isEmpty()) {
                        progress = CourseProgress.AVAILABLE;
                        break;
                    }
                }
            }
            if (completed == courseSessionModels.size()) {
                progress = CourseProgress.COMPLETED;
            }

            double totalScore = 0;
            for (CourseSessionModel courseSessionModel : courseSessionModels) {
                List<SessionProblemModel> sessionProblemModels = sessionProblemDao.getBySessionJid(courseSessionModel.sessionJid);


                for (SessionProblemModel sessionProblemModel : sessionProblemModels) {
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
            }

            curriculumCourseProgressBuilder.add(new CurriculumCourseProgress(createFromModel(curriculumCourseModel), progress, completed, courseSessionModels.size(), totalScore));
        }

        return new Page<>(curriculumCourseProgressBuilder.build(), totalPages, pageIndex, pageSize);
    }

    @Override
    public void addCurriculumCourse(String curriculumJid, String courseJid, String alias, boolean completeable) {
        CurriculumCourseModel curriculumCourseModel = new CurriculumCourseModel();
        curriculumCourseModel.curriculumJid = curriculumJid;
        curriculumCourseModel.courseJid = courseJid;
        curriculumCourseModel.alias = alias;
        curriculumCourseModel.completeable = completeable;

        curriculumCourseDao.persist(curriculumCourseModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void updateCurriculumCourse(long curriculumCourseId, String alias, boolean completeable) throws CurriculumCourseNotFoundException {
        CurriculumCourseModel curriculumCourseModel = curriculumCourseDao.findById(curriculumCourseId);
        if (curriculumCourseModel != null) {
            curriculumCourseModel.alias = alias;
            curriculumCourseModel.completeable = completeable;

            curriculumCourseDao.edit(curriculumCourseModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        } else {
            throw new CurriculumCourseNotFoundException("Curriculum Course Not Found.");
        }
    }

    @Override
    public void removeCurriculumCourse(long curriculumCourseId) throws CurriculumCourseNotFoundException {
        CurriculumCourseModel curriculumCourseModel = curriculumCourseDao.findById(curriculumCourseId);
        if (curriculumCourseModel != null) {
            curriculumCourseDao.remove(curriculumCourseModel);
        } else {
            throw new CurriculumCourseNotFoundException("Curriculum Course Not Found.");
        }
    }

    private CurriculumCourse createFromModel(CurriculumCourseModel model) {
        return new CurriculumCourse(model.id, model.curriculumJid, model.courseJid, model.alias, courseDao.findByJid(model.courseJid).name, model.completeable);
    }
}
