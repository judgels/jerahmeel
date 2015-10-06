package org.iatoki.judgels.jerahmeel.services.impls;

import org.iatoki.judgels.jerahmeel.SessionProblem;
import org.iatoki.judgels.jerahmeel.SessionProblemStatus;
import org.iatoki.judgels.jerahmeel.SessionProblemType;
import org.iatoki.judgels.jerahmeel.models.daos.BundleGradingDao;
import org.iatoki.judgels.jerahmeel.models.daos.BundleSubmissionDao;
import org.iatoki.judgels.jerahmeel.models.daos.ContainerProblemScoreCacheDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProgrammingGradingDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProgrammingSubmissionDao;
import org.iatoki.judgels.jerahmeel.models.entities.BundleGradingModel;
import org.iatoki.judgels.jerahmeel.models.entities.BundleSubmissionModel;
import org.iatoki.judgels.jerahmeel.models.entities.ContainerProblemScoreCacheModel;
import org.iatoki.judgels.jerahmeel.models.entities.ProgrammingGradingModel;
import org.iatoki.judgels.jerahmeel.models.entities.ProgrammingSubmissionModel;
import org.iatoki.judgels.jerahmeel.models.entities.SessionProblemModel;
import org.iatoki.judgels.sandalphon.ProblemType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

final class SessionProblemServiceUtils {

    private SessionProblemServiceUtils() {
        // prevent instantiation
    }

    static SessionProblem createFromModel(SessionProblemModel model) {
        return new SessionProblem(model.id, model.sessionJid, model.problemJid, model.problemSecret, model.alias, SessionProblemType.valueOf(model.type), SessionProblemStatus.valueOf(model.status));
    }

    static double getUserTotalScoreFromSessionProblemModels(ContainerProblemScoreCacheDao containerProblemScoreCacheDao, BundleSubmissionDao bundleSubmissionDao, BundleGradingDao bundleGradingDao, ProgrammingSubmissionDao programmingSubmissionDao, ProgrammingGradingDao programmingGradingDao, String userJid, List<SessionProblemModel> sessionProblemModels) {
        double totalScore = 0;
        for (SessionProblemModel sessionProblemModel : sessionProblemModels) {
            if (containerProblemScoreCacheDao.existsByUserJidContainerJidAndProblemJid(userJid, sessionProblemModel.sessionJid, sessionProblemModel.problemJid)) {
                ContainerProblemScoreCacheModel containerProblemScoreCacheModel = containerProblemScoreCacheDao.getByUserJidContainerJidAndProblemJid(userJid, sessionProblemModel.sessionJid, sessionProblemModel.problemJid);
                totalScore += containerProblemScoreCacheModel.score;
            } else {
                double maxScore = 0;
                if (sessionProblemModel.type.equals(ProblemType.BUNDLE.name())) {
                    List<BundleSubmissionModel> bundleSubmissionModels = bundleSubmissionDao.getByContainerJidAndUserJidAndProblemJid(sessionProblemModel.sessionJid, userJid, sessionProblemModel.problemJid);
                    Map<String, List<BundleGradingModel>> bundleGradingModels = bundleGradingDao.getBySubmissionJids(bundleSubmissionModels.stream().map(m -> m.jid).collect(Collectors.toList()));
                    for (String submissionJid : bundleGradingModels.keySet()) {
                        double submissionScore = bundleGradingModels.get(submissionJid).get(bundleGradingModels.get(submissionJid).size() - 1).score;
                        if (submissionScore > maxScore) {
                            maxScore = submissionScore;
                        }
                    }
                } else if (sessionProblemModel.type.equals(ProblemType.PROGRAMMING.name())) {
                    List<ProgrammingSubmissionModel> programmingSubmissionModels = programmingSubmissionDao.getByContainerJidAndUserJidAndProblemJid(sessionProblemModel.sessionJid, userJid, sessionProblemModel.problemJid);
                    Map<String, List<ProgrammingGradingModel>> gradingModels = programmingGradingDao.getBySubmissionJids(programmingSubmissionModels.stream().map(m -> m.jid).collect(Collectors.toList()));
                    for (String submissionJid : gradingModels.keySet()) {
                        double submissionScore = gradingModels.get(submissionJid).get(gradingModels.get(submissionJid).size() - 1).score;
                        if (submissionScore > maxScore) {
                            maxScore = submissionScore;
                        }
                    }
                }
                ContainerProblemScoreCacheModel containerProblemScoreCacheModel = new ContainerProblemScoreCacheModel();
                containerProblemScoreCacheModel.containerJid = sessionProblemModel.sessionJid;
                containerProblemScoreCacheModel.problemJid = sessionProblemModel.problemJid;
                containerProblemScoreCacheModel.userJid = userJid;
                containerProblemScoreCacheModel.score = maxScore;
                containerProblemScoreCacheDao.persist(containerProblemScoreCacheModel, "cacheLazyUpdater", "localhost");

                totalScore += maxScore;
            }
        }

        return totalScore;
    }
}
