package org.iatoki.judgels.jerahmeel.models.daos;

import org.iatoki.judgels.jerahmeel.models.entities.UserItemModel;
import org.iatoki.judgels.play.models.daos.Dao;

import java.util.List;

public interface UserItemDao extends Dao<Long, UserItemModel> {

    boolean existsByUserJidAndItemJid(String userJid, String itemJid);

    boolean existsByUserJidItemJidAndStatus(String userJid, String itemJid, String status);

    UserItemModel findByUserJidAndItemJid(String userJid, String itemJid);

    List<UserItemModel> getByUserJid(String userJid);

    List<UserItemModel> getByItemJid(String itemJid);

    List<UserItemModel> getByUserJidAndStatus(String userJid, String status);
}
