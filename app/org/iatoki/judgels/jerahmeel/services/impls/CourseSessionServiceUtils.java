package org.iatoki.judgels.jerahmeel.services.impls;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.jerahmeel.CourseSession;
import org.iatoki.judgels.jerahmeel.models.daos.BundleGradingDao;
import org.iatoki.judgels.jerahmeel.models.daos.BundleSubmissionDao;
import org.iatoki.judgels.jerahmeel.models.daos.ContainerProblemScoreCacheDao;
import org.iatoki.judgels.jerahmeel.models.daos.ContainerScoreCacheDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProgrammingGradingDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProgrammingSubmissionDao;
import org.iatoki.judgels.jerahmeel.models.daos.SessionDao;
import org.iatoki.judgels.jerahmeel.models.entities.ContainerScoreCacheModel;
import org.iatoki.judgels.jerahmeel.models.entities.CourseSessionModel;
import org.iatoki.judgels.jerahmeel.models.entities.SessionProblemModel;

import java.util.List;
import java.util.Map;

final class CourseSessionServiceUtils {

    private CourseSessionServiceUtils() {
        // prevent instantiation
    }

    static CourseSession createFromModel(SessionDao sessionDao, CourseSessionModel model) {
        return new CourseSession(model.id, model.courseJid, model.sessionJid, model.alias, sessionDao.findByJid(model.sessionJid).name);
    }

    static double getUserTotalScoreFromCourseSessionModels(ContainerScoreCacheDao containerScoreCacheDao, ContainerProblemScoreCacheDao containerProblemScoreCacheDao, BundleSubmissionDao bundleSubmissionDao, BundleGradingDao bundleGradingDao, ProgrammingSubmissionDao programmingSubmissionDao, ProgrammingGradingDao programmingGradingDao, String userJid, String courseJid, List<CourseSessionModel> courseSessionModels, Map<String, List<SessionProblemModel>> mapSessionJidToSessionProblemModels) {
        if (containerScoreCacheDao.existsByUserJidAndContainerJid(userJid, courseJid)) {
            ContainerScoreCacheModel containerScoreCacheModel = containerScoreCacheDao.getByUserJidAndContainerJid(userJid, courseJid);
            return containerScoreCacheModel.score;
        }

        double totalScore = getUserTotalScoreFromCourseSessionModelsWithoutCache(containerScoreCacheDao, containerProblemScoreCacheDao, bundleSubmissionDao, bundleGradingDao, programmingSubmissionDao, programmingGradingDao, userJid, courseSessionModels, mapSessionJidToSessionProblemModels);

        ContainerScoreCacheServiceUtils.addToContainerScoreCache(containerScoreCacheDao, userJid, courseJid, totalScore);
        return totalScore;
    }

    static double getUserTotalScoreFromCourseSessionModelsWithoutCache(ContainerScoreCacheDao containerScoreCacheDao, ContainerProblemScoreCacheDao containerProblemScoreCacheDao, BundleSubmissionDao bundleSubmissionDao, BundleGradingDao bundleGradingDao, ProgrammingSubmissionDao programmingSubmissionDao, ProgrammingGradingDao programmingGradingDao, String userJid, List<CourseSessionModel> courseSessionModels, Map<String, List<SessionProblemModel>> mapSessionJidToSessionProblemModels) {
        double totalScore = 0;
        for (CourseSessionModel courseSessionModel : courseSessionModels) {
            List<SessionProblemModel> sessionProblemModels = mapSessionJidToSessionProblemModels.get(courseSessionModel.sessionJid);
            if (sessionProblemModels == null) {
                sessionProblemModels = ImmutableList.of();
            }

            double sessionScore = SessionProblemServiceUtils.getUserTotalScoreFromSessionProblemModels(containerScoreCacheDao, containerProblemScoreCacheDao, bundleSubmissionDao, bundleGradingDao, programmingSubmissionDao, programmingGradingDao, userJid, courseSessionModel.sessionJid, sessionProblemModels);

            totalScore += sessionScore;
        }

        return totalScore;
    }
}
