package org.iatoki.judgels.jerahmeel.services.impls;

import org.iatoki.judgels.jerahmeel.SessionProblem;
import org.iatoki.judgels.jerahmeel.SessionProblemStatus;
import org.iatoki.judgels.jerahmeel.SessionProblemType;
import org.iatoki.judgels.jerahmeel.models.daos.BundleGradingDao;
import org.iatoki.judgels.jerahmeel.models.daos.BundleSubmissionDao;
import org.iatoki.judgels.jerahmeel.models.daos.ContainerProblemScoreCacheDao;
import org.iatoki.judgels.jerahmeel.models.daos.ContainerScoreCacheDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProgrammingGradingDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProgrammingSubmissionDao;
import org.iatoki.judgels.jerahmeel.models.entities.BundleGradingModel;
import org.iatoki.judgels.jerahmeel.models.entities.BundleSubmissionModel;
import org.iatoki.judgels.jerahmeel.models.entities.ContainerProblemScoreCacheModel;
import org.iatoki.judgels.jerahmeel.models.entities.ContainerScoreCacheModel;
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

    static double getUserMaxScoreFromSessionProblemModel(ContainerProblemScoreCacheDao containerProblemScoreCacheDao, BundleSubmissionDao bundleSubmissionDao, BundleGradingDao bundleGradingDao, ProgrammingSubmissionDao programmingSubmissionDao, ProgrammingGradingDao programmingGradingDao, String userJid, SessionProblemModel sessionProblemModel) {
        if (containerProblemScoreCacheDao.existsByUserJidContainerJidAndProblemJid(userJid, sessionProblemModel.sessionJid, sessionProblemModel.problemJid)) {
            ContainerProblemScoreCacheModel containerProblemScoreCacheModel = containerProblemScoreCacheDao.getByUserJidContainerJidAndProblemJid(userJid, sessionProblemModel.sessionJid, sessionProblemModel.problemJid);
            return containerProblemScoreCacheModel.score;
        }

        double maxScore = Double.NEGATIVE_INFINITY;
        if (sessionProblemModel.type.equals(ProblemType.BUNDLE.name())) {
            List<BundleSubmissionModel> bundleSubmissionModels = bundleSubmissionDao.getByContainerJidAndUserJidAndProblemJid(sessionProblemModel.sessionJid, userJid, sessionProblemModel.problemJid);

            if (bundleSubmissionModels.isEmpty()) {
                ContainerProblemScoreCacheServiceUtils.addToContainerProblemScoreCache(containerProblemScoreCacheDao, userJid, sessionProblemModel.sessionJid, sessionProblemModel.problemJid, 0);

                return 0;
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
                ContainerProblemScoreCacheServiceUtils.addToContainerProblemScoreCache(containerProblemScoreCacheDao, userJid, sessionProblemModel.sessionJid, sessionProblemModel.problemJid, 0);

                return 0;
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

    static double getUserTotalScoreFromSessionProblemModels(ContainerScoreCacheDao containerScoreCacheDao, ContainerProblemScoreCacheDao containerProblemScoreCacheDao, BundleSubmissionDao bundleSubmissionDao, BundleGradingDao bundleGradingDao, ProgrammingSubmissionDao programmingSubmissionDao, ProgrammingGradingDao programmingGradingDao, String userJid, String sessionJid, List<SessionProblemModel> sessionProblemModels) {
        if (containerScoreCacheDao.existsByUserJidAndContainerJid(userJid, sessionJid)) {
            ContainerScoreCacheModel containerScoreCacheModel = containerScoreCacheDao.getByUserJidAndContainerJid(userJid, sessionJid);
            return containerScoreCacheModel.score;
        }

        double totalScore = getUserTotalScoreFromSessionProblemModelsWithoutCache(containerScoreCacheDao, containerProblemScoreCacheDao, bundleSubmissionDao, bundleGradingDao, programmingSubmissionDao, programmingGradingDao, userJid, sessionProblemModels);

        ContainerScoreCacheServiceUtils.addToContainerScoreCache(containerScoreCacheDao, userJid, sessionJid, totalScore);
        return totalScore;
    }

    static double getUserTotalScoreFromSessionProblemModelsWithoutCache(ContainerScoreCacheDao containerScoreCacheDao, ContainerProblemScoreCacheDao containerProblemScoreCacheDao, BundleSubmissionDao bundleSubmissionDao, BundleGradingDao bundleGradingDao, ProgrammingSubmissionDao programmingSubmissionDao, ProgrammingGradingDao programmingGradingDao, String userJid, List<SessionProblemModel> sessionProblemModels) {
        double totalScore = 0;
        for (SessionProblemModel sessionProblemModel : sessionProblemModels) {
            totalScore += getUserMaxScoreFromSessionProblemModel(containerProblemScoreCacheDao, bundleSubmissionDao, bundleGradingDao, programmingSubmissionDao, programmingGradingDao, userJid, sessionProblemModel);
        }

        return totalScore;
    }
}
