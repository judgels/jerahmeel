package org.iatoki.judgels.jerahmeel.models.daos;

import org.iatoki.judgels.play.models.daos.interfaces.Dao;
import org.iatoki.judgels.jerahmeel.models.entities.UserModel;

public interface UserDao extends Dao<Long, UserModel> {

    boolean existsByUserJid(String userJid);

    UserModel findByUserJid(String userJid);
}
