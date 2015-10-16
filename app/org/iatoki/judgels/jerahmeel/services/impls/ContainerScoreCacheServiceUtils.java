package org.iatoki.judgels.jerahmeel.services.impls;

import org.iatoki.judgels.jerahmeel.models.daos.ContainerScoreCacheDao;
import org.iatoki.judgels.jerahmeel.models.entities.ContainerScoreCacheModel;

public final class ContainerScoreCacheServiceUtils {

    private ContainerScoreCacheServiceUtils() {
        // prevent instantiation
    }

    public static void addToContainerScoreCache(ContainerScoreCacheDao containerScoreCacheDao, String userJid, String containerJid, double score) {
        ContainerScoreCacheModel containerScoreCacheModel = new ContainerScoreCacheModel();
        containerScoreCacheModel.userJid = userJid;
        containerScoreCacheModel.containerJid = containerJid;
        containerScoreCacheModel.score = score;

        containerScoreCacheDao.persist(containerScoreCacheModel, "cacheLazyUpdater", "localhost");
    }
}
