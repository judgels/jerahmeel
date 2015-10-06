package org.iatoki.judgels.jerahmeel.models.daos;

import org.iatoki.judgels.jerahmeel.models.entities.ContainerProblemScoreCacheModel;
import org.iatoki.judgels.play.models.daos.Dao;

public interface ContainerProblemScoreCacheDao extends Dao<Long, ContainerProblemScoreCacheModel> {

    boolean existsByUserJidContainerJidAndProblemJid(String userJid, String containerJid, String problemJid);

    ContainerProblemScoreCacheModel getByUserJidContainerJidAndProblemJid(String userJid, String containerJid, String problemJid);
}
