package org.iatoki.judgels.jerahmeel.services.impls;

import org.iatoki.judgels.jerahmeel.Archive;
import org.iatoki.judgels.jerahmeel.ProblemSet;
import org.iatoki.judgels.jerahmeel.models.daos.BundleGradingDao;
import org.iatoki.judgels.jerahmeel.models.daos.BundleSubmissionDao;
import org.iatoki.judgels.jerahmeel.models.daos.ContainerProblemScoreCacheDao;
import org.iatoki.judgels.jerahmeel.models.daos.ContainerScoreCacheDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProgrammingGradingDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProgrammingSubmissionDao;
import org.iatoki.judgels.jerahmeel.models.entities.ContainerScoreCacheModel;
import org.iatoki.judgels.jerahmeel.models.entities.ProblemSetModel;
import org.iatoki.judgels.jerahmeel.models.entities.ProblemSetProblemModel;

import java.util.List;

public final class ProblemSetServiceUtils {

    private ProblemSetServiceUtils() {
        // prevent instantiation
    }

    static ProblemSet createProblemSetFromModelAndArchive(ProblemSetModel problemSetModel, Archive archive) {
        return new ProblemSet(problemSetModel.id, problemSetModel.jid, archive, problemSetModel.name, problemSetModel.description);
    }

    static double getUserTotalScoreFromProblemSetModelAndProblemSetProblemModels(ContainerScoreCacheDao containerScoreCacheDao, ContainerProblemScoreCacheDao containerProblemScoreCacheDao, BundleSubmissionDao bundleSubmissionDao, BundleGradingDao bundleGradingDao, ProgrammingSubmissionDao programmingSubmissionDao, ProgrammingGradingDao programmingGradingDao, String userJid, ProblemSetModel problemSetModel, List<ProblemSetProblemModel> problemSetProblemModels) {
        if (containerScoreCacheDao.existsByUserJidAndContainerJid(userJid, problemSetModel.jid)) {
            ContainerScoreCacheModel containerScoreCacheModel = containerScoreCacheDao.getByUserJidAndContainerJid(userJid, problemSetModel.jid);
            return containerScoreCacheModel.score;
        }

        double problemSetScore = getUserTotalScoreFromProblemSetModelAndProblemSetProblemModelsWithoutCache(containerScoreCacheDao, containerProblemScoreCacheDao, bundleSubmissionDao, bundleGradingDao, programmingSubmissionDao, programmingGradingDao, userJid, problemSetModel, problemSetProblemModels);

        ContainerScoreCacheServiceUtils.addToContainerScoreCache(containerScoreCacheDao, userJid, problemSetModel.jid, problemSetScore);
        return problemSetScore;
    }

    static double getUserTotalScoreFromProblemSetModelAndProblemSetProblemModelsWithoutCache(ContainerScoreCacheDao containerScoreCacheDao, ContainerProblemScoreCacheDao containerProblemScoreCacheDao, BundleSubmissionDao bundleSubmissionDao, BundleGradingDao bundleGradingDao, ProgrammingSubmissionDao programmingSubmissionDao, ProgrammingGradingDao programmingGradingDao, String userJid, ProblemSetModel problemSetModel, List<ProblemSetProblemModel> problemSetProblemModels) {
        return ProblemSetProblemServiceUtils.getUserTotalScoreFromProblemSetProblemModels(containerProblemScoreCacheDao, bundleSubmissionDao, bundleGradingDao, programmingSubmissionDao, programmingGradingDao, userJid, problemSetProblemModels);
    }
}
