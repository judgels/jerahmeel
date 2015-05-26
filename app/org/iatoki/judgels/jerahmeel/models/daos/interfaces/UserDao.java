package org.iatoki.judgels.jerahmeel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.jerahmeel.models.domains.UserModel;

public interface UserDao extends Dao<Long, UserModel> {

    boolean existsByUserJid(String userJid);

    UserModel findByUserJid(String userJid);
}
