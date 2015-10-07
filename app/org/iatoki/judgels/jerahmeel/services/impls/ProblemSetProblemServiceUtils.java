package org.iatoki.judgels.jerahmeel.services.impls;

import com.google.common.collect.Lists;
import org.iatoki.judgels.jerahmeel.ProblemSetProblem;
import org.iatoki.judgels.jerahmeel.ProblemSetProblemStatus;
import org.iatoki.judgels.jerahmeel.ProblemSetProblemType;
import org.iatoki.judgels.jerahmeel.models.daos.BundleGradingDao;
import org.iatoki.judgels.jerahmeel.models.daos.BundleSubmissionDao;
import org.iatoki.judgels.jerahmeel.models.daos.ContainerProblemScoreCacheDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProgrammingGradingDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProgrammingSubmissionDao;
import org.iatoki.judgels.jerahmeel.models.entities.BundleGradingModel;
import org.iatoki.judgels.jerahmeel.models.entities.BundleSubmissionModel;
import org.iatoki.judgels.jerahmeel.models.entities.ContainerProblemScoreCacheModel;
import org.iatoki.judgels.jerahmeel.models.entities.ProblemSetProblemModel;
import org.iatoki.judgels.jerahmeel.models.entities.ProgrammingGradingModel;
import org.iatoki.judgels.jerahmeel.models.entities.ProgrammingSubmissionModel;

import java.util.List;
import java.util.Map;

final class ProblemSetProblemServiceUtils {

    private ProblemSetProblemServiceUtils() {
        // prevent instantiation
    }

    static ProblemSetProblem createFromModel(ProblemSetProblemModel model) {
        return new ProblemSetProblem(model.id, model.problemSetJid, model.problemJid, model.problemSecret, model.alias, ProblemSetProblemType.valueOf(model.type), ProblemSetProblemStatus.valueOf(model.status));
    }

    static double getUserMaxScoreFromProblemSetProblemModel(ContainerProblemScoreCacheDao containerProblemScoreCacheDao, BundleSubmissionDao bundleSubmissionDao, BundleGradingDao bundleGradingDao, ProgrammingSubmissionDao programmingSubmissionDao, ProgrammingGradingDao programmingGradingDao, String userJid, ProblemSetProblemModel problemSetProblemModel) {
        if (containerProblemScoreCacheDao.existsByUserJidContainerJidAndProblemJid(userJid, problemSetProblemModel.problemSetJid, problemSetProblemModel.problemJid)) {
            ContainerProblemScoreCacheModel containerProblemScoreCacheModel = containerProblemScoreCacheDao.getByUserJidContainerJidAndProblemJid(userJid, problemSetProblemModel.problemSetJid, problemSetProblemModel.problemJid);
            return containerProblemScoreCacheModel.score;
        }

        double maxScore = Double.NEGATIVE_INFINITY;
        if (problemSetProblemModel.type.equals(ProblemSetProblemType.BUNDLE.name())) {
            List<BundleSubmissionModel> bundleSubmissionModels = bundleSubmissionDao.getByContainerJidAndUserJidAndProblemJid(problemSetProblemModel.problemSetJid, userJid, problemSetProblemModel.problemJid);

            if (bundleSubmissionModels.isEmpty()) {
                ContainerProblemScoreCacheServiceUtils.addToContainerProblemScoreCache(containerProblemScoreCacheDao, userJid, problemSetProblemModel.problemJid, problemSetProblemModel.problemJid, 0);

                return 0;
            }

            Map<String, List<BundleGradingModel>> gradingModelsMap = bundleGradingDao.getBySubmissionJids(Lists.transform(bundleSubmissionModels, m -> m.jid));

            for (BundleSubmissionModel bundleSubmissionModel : bundleSubmissionModels) {
                double submissionScore = gradingModelsMap.get(bundleSubmissionModel.jid).get(gradingModelsMap.get(bundleSubmissionModel.jid).size() - 1).score;
                if (submissionScore > maxScore) {
                    maxScore = submissionScore;
                }
            }
        } else if (problemSetProblemModel.type.equals(ProblemSetProblemType.PROGRAMMING.name())) {
            List<ProgrammingSubmissionModel> programmingSubmissionModels = programmingSubmissionDao.getByContainerJidAndUserJidAndProblemJid(problemSetProblemModel.problemSetJid, userJid, problemSetProblemModel.problemJid);

            if (programmingSubmissionModels.isEmpty()) {
                ContainerProblemScoreCacheServiceUtils.addToContainerProblemScoreCache(containerProblemScoreCacheDao, userJid, problemSetProblemModel.problemJid, problemSetProblemModel.problemJid, 0);

                return 0;
            }

            Map<String, List<ProgrammingGradingModel>> gradingModelsMap = programmingGradingDao.getBySubmissionJids(Lists.transform(programmingSubmissionModels, m -> m.jid));

            for (ProgrammingSubmissionModel programmingSubmissionModel : programmingSubmissionModels) {
                double submissionScore = gradingModelsMap.get(programmingSubmissionModel.jid).get(gradingModelsMap.get(programmingSubmissionModel.jid).size() - 1).score;
                if (submissionScore > maxScore) {
                    maxScore = submissionScore;
                }
            }
        }

        ContainerProblemScoreCacheServiceUtils.addToContainerProblemScoreCache(containerProblemScoreCacheDao, userJid, problemSetProblemModel.problemJid, problemSetProblemModel.problemJid, maxScore);
        return maxScore;
    }

    static double getUserTotalScoreFromProblemSetProblemModels(ContainerProblemScoreCacheDao containerProblemScoreCacheDao, BundleSubmissionDao bundleSubmissionDao, BundleGradingDao bundleGradingDao, ProgrammingSubmissionDao programmingSubmissionDao, ProgrammingGradingDao programmingGradingDao, String userJid, List<ProblemSetProblemModel> problemSetProblemModels) {
        double totalScore = 0;
        for (ProblemSetProblemModel problemSetProblemModel : problemSetProblemModels) {
            totalScore += getUserMaxScoreFromProblemSetProblemModel(containerProblemScoreCacheDao, bundleSubmissionDao, bundleGradingDao, programmingSubmissionDao, programmingGradingDao, userJid, problemSetProblemModel);
        }

        return totalScore;
    }
}
