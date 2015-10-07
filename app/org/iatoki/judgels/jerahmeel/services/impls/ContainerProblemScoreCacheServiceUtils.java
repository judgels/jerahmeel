package org.iatoki.judgels.jerahmeel.services.impls;

import org.iatoki.judgels.jerahmeel.models.daos.ContainerProblemScoreCacheDao;
import org.iatoki.judgels.jerahmeel.models.entities.ContainerProblemScoreCacheModel;

final class ContainerProblemScoreCacheServiceUtils {

    private ContainerProblemScoreCacheServiceUtils() {
        // prevent instantiation
    }

    static void addToContainerProblemScoreCache(ContainerProblemScoreCacheDao containerProblemScoreCacheDao, String userJid, String containerJid, String problemJid, double score) {
        ContainerProblemScoreCacheModel containerProblemScoreCacheModel = new ContainerProblemScoreCacheModel();
        containerProblemScoreCacheModel.userJid = userJid;
        containerProblemScoreCacheModel.containerJid = containerJid;
        containerProblemScoreCacheModel.problemJid = problemJid;
        containerProblemScoreCacheModel.score = score;

        containerProblemScoreCacheDao.persist(containerProblemScoreCacheModel, "cacheLazyUpdater", "localhost");
    }
}
