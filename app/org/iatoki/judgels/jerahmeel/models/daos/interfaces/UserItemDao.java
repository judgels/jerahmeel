package org.iatoki.judgels.jerahmeel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.jerahmeel.models.domains.UserItemModel;

import java.util.List;

public interface UserItemDao extends Dao<Long, UserItemModel> {
    boolean existByUserJidAndItemJid(String userJid, String itemJid);

    boolean existByUserJidItemJidAndStatus(String userJid, String itemJid, String status);

    UserItemModel findByUserJidAndItemJid(String userJid, String itemJid);

    List<UserItemModel> findByUserJid(String userJid);

    List<UserItemModel> findByItemJid(String itemJid);

    List<UserItemModel> findByUserJidAndStatus(String userJid, String status);
}
