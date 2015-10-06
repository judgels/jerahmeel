package org.iatoki.judgels.jerahmeel.services.impls;

import org.iatoki.judgels.jerahmeel.CourseSession;
import org.iatoki.judgels.jerahmeel.models.daos.BundleGradingDao;
import org.iatoki.judgels.jerahmeel.models.daos.BundleSubmissionDao;
import org.iatoki.judgels.jerahmeel.models.daos.ContainerProblemScoreCacheDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProgrammingGradingDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProgrammingSubmissionDao;
import org.iatoki.judgels.jerahmeel.models.daos.SessionDao;
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

    static double getUserTotalScoreFromCourseSessionModels(ContainerProblemScoreCacheDao containerProblemScoreCacheDao, BundleSubmissionDao bundleSubmissionDao, BundleGradingDao bundleGradingDao, ProgrammingSubmissionDao programmingSubmissionDao, ProgrammingGradingDao programmingGradingDao, String userJid, List<CourseSessionModel> courseSessionModels, Map<String, List<SessionProblemModel>> mapSessionJidToSessionProblemModels) {
        double totalScore = 0;
        for (CourseSessionModel courseSessionModel : courseSessionModels) {
            double sessionScore = SessionProblemServiceUtils.getUserTotalScoreFromSessionProblemModels(containerProblemScoreCacheDao, bundleSubmissionDao, bundleGradingDao, programmingSubmissionDao, programmingGradingDao, userJid, mapSessionJidToSessionProblemModels.get(courseSessionModel.sessionJid));

            totalScore += sessionScore;
        }

        return totalScore;
    }
}
