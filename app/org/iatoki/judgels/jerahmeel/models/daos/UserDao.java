package org.iatoki.judgels.jerahmeel.models.daos;

import org.iatoki.judgels.jerahmeel.models.entities.UserModel;
import org.iatoki.judgels.play.models.daos.Dao;

public interface UserDao extends Dao<Long, UserModel> {

    boolean existsByJid(String jid);

    UserModel findByJid(String jid);
}
