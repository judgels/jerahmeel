package org.iatoki.judgels.jerahmeel.avatar;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.jophiel.models.daos.BaseAvatarCacheDao;

@ImplementedBy(AvatarCacheHibernateDao.class)
public interface AvatarCacheDao extends BaseAvatarCacheDao<AvatarCacheModel> {

}
