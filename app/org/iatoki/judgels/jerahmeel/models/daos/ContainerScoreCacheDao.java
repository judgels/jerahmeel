package org.iatoki.judgels.jerahmeel.models.daos;

import org.iatoki.judgels.jerahmeel.models.entities.ContainerScoreCacheModel;
import org.iatoki.judgels.play.models.daos.Dao;

public interface ContainerScoreCacheDao extends Dao<Long, ContainerScoreCacheModel> {

    boolean existsByUserJidAndContainerJid(String userJid, String containerJid);

    ContainerScoreCacheModel getByUserJidAndContainerJid(String userJid, String containerJid);
}
