package org.iatoki.judgels.jerahmeel.services.impls;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.jerahmeel.SessionProblemWithProgress;
import org.iatoki.judgels.jerahmeel.models.daos.BundleGradingDao;
import org.iatoki.judgels.jerahmeel.models.daos.BundleSubmissionDao;
import org.iatoki.judgels.jerahmeel.models.daos.ContainerProblemScoreCacheDao;
import org.iatoki.judgels.jerahmeel.models.daos.ContainerScoreCacheDao;
import org.iatoki.judgels.jerahmeel.models.daos.CourseSessionDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProgrammingGradingDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProgrammingSubmissionDao;
import org.iatoki.judgels.jerahmeel.models.daos.SessionProblemDao;
import org.iatoki.judgels.jerahmeel.models.daos.UserItemDao;
import org.iatoki.judgels.jerahmeel.models.entities.BundleGradingModel;
import org.iatoki.judgels.jerahmeel.models.entities.BundleSubmissionModel;
import org.iatoki.judgels.jerahmeel.models.entities.ContainerProblemScoreCacheModel;
import org.iatoki.judgels.jerahmeel.models.entities.ContainerScoreCacheModel;
import org.iatoki.judgels.jerahmeel.models.entities.CourseSessionModel;
import org.iatoki.judgels.jerahmeel.models.entities.ProgrammingGradingModel;
import org.iatoki.judgels.jerahmeel.models.entities.ProgrammingSubmissionModel;
import org.iatoki.judgels.jerahmeel.models.entities.SessionProblemModel;
import org.iatoki.judgels.sandalphon.ProblemType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class SessionScoreCacheUtils {

    private static SessionScoreCacheUtils instance;

    private final BundleSubmissionDao bundleSubmissionDao;
    private final BundleGradingDao bundleGradingDao;
    private final ContainerScoreCacheDao containerScoreCacheDao;
    private final ContainerProblemScoreCacheDao containerProblemScoreCacheDao;
    private final CourseSessionDao courseSessionDao;
    private final ProgrammingSubmissionDao programmingSubmissionDao;
    private final ProgrammingGradingDao programmingGradingDao;
    private final SessionProblemDao sessionProblemDao;
    private final UserItemDao userItemDao;

    public SessionScoreCacheUtils(BundleSubmissionDao bundleSubmissionDao, BundleGradingDao bundleGradingDao, ContainerScoreCacheDao containerScoreCacheDao, ContainerProblemScoreCacheDao containerProblemScoreCacheDao, CourseSessionDao courseSessionDao, ProgrammingSubmissionDao programmingSubmissionDao, ProgrammingGradingDao programmingGradingDao, SessionProblemDao sessionProblemDao, UserItemDao userItemDao) {
        this.bundleSubmissionDao = bundleSubmissionDao;
        this.bundleGradingDao = bundleGradingDao;
        this.containerScoreCacheDao = containerScoreCacheDao;
        this.containerProblemScoreCacheDao = containerProblemScoreCacheDao;
        this.courseSessionDao = courseSessionDao;
        this.programmingSubmissionDao = programmingSubmissionDao;
        this.programmingGradingDao = programmingGradingDao;
        this.sessionProblemDao = sessionProblemDao;
        this.userItemDao = userItemDao;
    }

    public static synchronized void buildInstance(BundleSubmissionDao bundleSubmissionDao, BundleGradingDao bundleGradingDao, ContainerScoreCacheDao containerScoreCacheDao, ContainerProblemScoreCacheDao containerProblemScoreCacheDao, CourseSessionDao courseSessionDao, ProgrammingSubmissionDao programmingSubmissionDao, ProgrammingGradingDao programmingGradingDao, SessionProblemDao sessionProblemDao, UserItemDao userItemDao) {
        if (instance != null) {
            throw new UnsupportedOperationException("SessionScoreCacheUtils instance has already been built");
        }
        instance = new SessionScoreCacheUtils(bundleSubmissionDao, bundleGradingDao, containerScoreCacheDao, containerProblemScoreCacheDao, courseSessionDao, programmingSubmissionDao, programmingGradingDao, sessionProblemDao, userItemDao);
    }

    static SessionScoreCacheUtils getInstance() {
        if (instance == null) {
            throw new UnsupportedOperationException("SessionScoreCacheUtils instance has not been built");
        }
        return instance;
    }

    double getUserTotalScoreFromCourseSessionModels(String userJid, String courseJid, List<CourseSessionModel> courseSessionModels, Map<String, List<SessionProblemModel>> mapSessionJidToSessionProblemModels) {
        if (containerScoreCacheDao.existsByUserJidAndContainerJid(userJid, courseJid)) {
            ContainerScoreCacheModel containerScoreCacheModel = containerScoreCacheDao.getByUserJidAndContainerJid(userJid, courseJid);
            return containerScoreCacheModel.score;
        }

        double totalScore = getUserTotalScoreFromCourseSessionModelsWithoutCache(userJid, courseSessionModels, mapSessionJidToSessionProblemModels);

        ContainerScoreCacheServiceUtils.addToContainerScoreCache(containerScoreCacheDao, userJid, courseJid, totalScore);
        return totalScore;
    }

    double getUserTotalScoreFromCourseSessionModelsWithoutCache(String userJid, List<CourseSessionModel> courseSessionModels, Map<String, List<SessionProblemModel>> mapSessionJidToSessionProblemModels) {
        double totalScore = 0;
        for (CourseSessionModel courseSessionModel : courseSessionModels) {
            List<SessionProblemModel> sessionProblemModels = mapSessionJidToSessionProblemModels.get(courseSessionModel.sessionJid);
            if (sessionProblemModels == null) {
                sessionProblemModels = ImmutableList.of();
            }

            double sessionScore = getUserTotalScoreFromSessionProblemModels(userJid, courseSessionModel.sessionJid, sessionProblemModels);

            totalScore += sessionScore;
        }

        return totalScore;
    }

    double getUserTotalScoreFromSessionProblemModels(String userJid, String sessionJid, List<SessionProblemModel> sessionProblemModels) {
        if (containerScoreCacheDao.existsByUserJidAndContainerJid(userJid, sessionJid)) {
            ContainerScoreCacheModel containerScoreCacheModel = containerScoreCacheDao.getByUserJidAndContainerJid(userJid, sessionJid);
            return containerScoreCacheModel.score;
        }

        double totalScore = getUserTotalScoreFromSessionProblemModelsWithoutCache(userJid, sessionProblemModels);

        ContainerScoreCacheServiceUtils.addToContainerScoreCache(containerScoreCacheDao, userJid, sessionJid, totalScore);
        return totalScore;
    }

    double getUserTotalScoreFromSessionProblemModelsWithoutCache(String userJid, List<SessionProblemModel> sessionProblemModels) {
        double totalScore = 0;
        for (SessionProblemModel sessionProblemModel : sessionProblemModels) {
            double sessionProblemScore = getUserMaxScoreFromSessionProblemModel(userJid, sessionProblemModel);
            if (Double.compare(SessionProblemWithProgress.MINIMUM_SCORE, sessionProblemScore) != 0) {
                totalScore += sessionProblemScore;
            }
        }

        return totalScore;
    }

    double getUserMaxScoreFromSessionProblemModel(String userJid, SessionProblemModel sessionProblemModel) {
        if (containerProblemScoreCacheDao.existsByUserJidContainerJidAndProblemJid(userJid, sessionProblemModel.sessionJid, sessionProblemModel.problemJid)) {
            ContainerProblemScoreCacheModel containerProblemScoreCacheModel = containerProblemScoreCacheDao.getByUserJidContainerJidAndProblemJid(userJid, sessionProblemModel.sessionJid, sessionProblemModel.problemJid);
            return containerProblemScoreCacheModel.score;
        }

        double maxScore = SessionProblemWithProgress.MINIMUM_SCORE;
        if (sessionProblemModel.type.equals(ProblemType.BUNDLE.name())) {
            List<BundleSubmissionModel> bundleSubmissionModels = bundleSubmissionDao.getByContainerJidAndUserJidAndProblemJid(sessionProblemModel.sessionJid, userJid, sessionProblemModel.problemJid);

            if (bundleSubmissionModels.isEmpty()) {
                ContainerProblemScoreCacheServiceUtils.addToContainerProblemScoreCache(containerProblemScoreCacheDao, userJid, sessionProblemModel.sessionJid, sessionProblemModel.problemJid, SessionProblemWithProgress.MINIMUM_SCORE);

                return SessionProblemWithProgress.MINIMUM_SCORE;
            }

            Map<String, List<BundleGradingModel>> bundleGradingModels = bundleGradingDao.getBySubmissionJids(bundleSubmissionModels.stream().map(m -> m.jid).collect(Collectors.toList()));

            for (String submissionJid : bundleGradingModels.keySet()) {
                double submissionScore = bundleGradingModels.get(submissionJid).get(bundleGradingModels.get(submissionJid).size() - 1).score;
                if (submissionScore > maxScore) {
                    maxScore = submissionScore;
                }
            }
        } else if (sessionProblemModel.type.equals(ProblemType.PROGRAMMING.name())) {
            List<ProgrammingSubmissionModel> programmingSubmissionModels = programmingSubmissionDao.getByContainerJidAndUserJidAndProblemJid(sessionProblemModel.sessionJid, userJid, sessionProblemModel.problemJid);

            if (programmingSubmissionModels.isEmpty()) {
                ContainerProblemScoreCacheServiceUtils.addToContainerProblemScoreCache(containerProblemScoreCacheDao, userJid, sessionProblemModel.sessionJid, sessionProblemModel.problemJid, SessionProblemWithProgress.MINIMUM_SCORE);

                return SessionProblemWithProgress.MINIMUM_SCORE;
            }

            Map<String, List<ProgrammingGradingModel>> gradingModels = programmingGradingDao.getBySubmissionJids(programmingSubmissionModels.stream().map(m -> m.jid).collect(Collectors.toList()));

            for (String submissionJid : gradingModels.keySet()) {
                double submissionScore = gradingModels.get(submissionJid).get(gradingModels.get(submissionJid).size() - 1).score;
                if (submissionScore > maxScore) {
                    maxScore = submissionScore;
                }
            }
        }

        ContainerProblemScoreCacheServiceUtils.addToContainerProblemScoreCache(containerProblemScoreCacheDao, userJid, sessionProblemModel.sessionJid, sessionProblemModel.problemJid, maxScore);

        return maxScore;
    }
}
