package org.iatoki.judgels.jerahmeel.services.impls;

import com.google.common.collect.ImmutableMap;
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
import org.iatoki.judgels.sandalphon.BundleSubmission;
import org.iatoki.judgels.sandalphon.ProgrammingSubmission;
import org.iatoki.judgels.sandalphon.models.entities.AbstractBundleGradingModel_;
import org.iatoki.judgels.sandalphon.models.entities.AbstractProgrammingGradingModel_;
import org.iatoki.judgels.sandalphon.services.impls.BundleSubmissionServiceUtils;
import org.iatoki.judgels.sandalphon.services.impls.ProgrammingSubmissionServiceUtils;

import java.util.List;

final class ProblemSetProblemServiceUtils {

    private ProblemSetProblemServiceUtils() {
        // prevent instantiation
    }

    static ProblemSetProblem createFromModel(ProblemSetProblemModel model) {
        return new ProblemSetProblem(model.id, model.problemSetJid, model.problemJid, model.problemSecret, model.alias, ProblemSetProblemType.valueOf(model.type), ProblemSetProblemStatus.valueOf(model.status));
    }

    static double getUserTotalScoreFromProblemSetProblemModels(ContainerProblemScoreCacheDao containerProblemScoreCacheDao, BundleSubmissionDao bundleSubmissionDao, BundleGradingDao bundleGradingDao, ProgrammingSubmissionDao programmingSubmissionDao, ProgrammingGradingDao programmingGradingDao, String userJid, List<ProblemSetProblemModel> problemSetProblemModels) {
        double totalScore = 0;
        for (ProblemSetProblemModel problemSetProblemModel : problemSetProblemModels) {
            if (containerProblemScoreCacheDao.existsByUserJidContainerJidAndProblemJid(userJid, problemSetProblemModel.problemSetJid, problemSetProblemModel.problemJid)) {
                ContainerProblemScoreCacheModel containerProblemScoreCacheModel = containerProblemScoreCacheDao.getByUserJidContainerJidAndProblemJid(userJid, problemSetProblemModel.problemSetJid, problemSetProblemModel.problemJid);
                totalScore += containerProblemScoreCacheModel.score;
            } else {
                double maxScore = 0;
                if (problemSetProblemModel.type.equals(ProblemSetProblemType.BUNDLE.name())) {
                    List<BundleSubmissionModel> bundleSubmissionModels = bundleSubmissionDao.getByContainerJidAndUserJidAndProblemJid(problemSetProblemModel.problemSetJid, userJid, problemSetProblemModel.problemJid);
                    for (BundleSubmissionModel bundleSubmissionModel : bundleSubmissionModels) {
                        List<BundleGradingModel> gradingModels = bundleGradingDao.findSortedByFiltersEq("id", "asc", "", ImmutableMap.of(AbstractBundleGradingModel_.submissionJid, bundleSubmissionModel.jid), 0, -1);
                        BundleSubmission bundleSubmission = BundleSubmissionServiceUtils.createSubmissionFromModels(bundleSubmissionModel, gradingModels);

                        if (bundleSubmission.getLatestScore() > maxScore) {
                            maxScore = bundleSubmission.getLatestScore();
                        }
                    }
                } else if (problemSetProblemModel.type.equals(ProblemSetProblemType.PROGRAMMING.name())) {
                    List<ProgrammingSubmissionModel> programmingSubmissionModels = programmingSubmissionDao.getByContainerJidAndUserJidAndProblemJid(problemSetProblemModel.problemSetJid, userJid, problemSetProblemModel.problemJid);
                    for (ProgrammingSubmissionModel programmingSubmissionModel : programmingSubmissionModels) {
                        List<ProgrammingGradingModel> gradingModels = programmingGradingDao.findSortedByFiltersEq("id", "asc", "", ImmutableMap.of(AbstractProgrammingGradingModel_.submissionJid, programmingSubmissionModel.jid), 0, -1);
                        ProgrammingSubmission programmingSubmission = ProgrammingSubmissionServiceUtils.createSubmissionFromModels(programmingSubmissionModel, gradingModels);

                        if (programmingSubmission.getLatestScore() > maxScore) {
                            maxScore = programmingSubmission.getLatestScore();
                        }
                    }
                }

                ContainerProblemScoreCacheModel containerProblemScoreCacheModel = new ContainerProblemScoreCacheModel();
                containerProblemScoreCacheModel.containerJid = problemSetProblemModel.problemSetJid;
                containerProblemScoreCacheModel.problemJid = problemSetProblemModel.problemJid;
                containerProblemScoreCacheModel.userJid = userJid;
                containerProblemScoreCacheModel.score = maxScore;
                containerProblemScoreCacheDao.persist(containerProblemScoreCacheModel, "cacheLazyUpdater", "localhost");

                totalScore += maxScore;
            }
        }

        return totalScore;
    }
}
