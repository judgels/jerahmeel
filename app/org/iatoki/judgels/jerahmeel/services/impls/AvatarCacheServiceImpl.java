package org.iatoki.judgels.jerahmeel.services.impls;

import org.iatoki.judgels.jophiel.services.impls.AbstractBaseAvatarCacheServiceImpl;
import org.iatoki.judgels.jerahmeel.models.entities.AvatarCacheModel;
import org.iatoki.judgels.jophiel.Jophiel;
import org.iatoki.judgels.jophiel.models.daos.BaseAvatarCacheDao;

public final class AvatarCacheServiceImpl extends AbstractBaseAvatarCacheServiceImpl<AvatarCacheModel> {

    private static AvatarCacheServiceImpl INSTANCE;

    private AvatarCacheServiceImpl(Jophiel jophiel, BaseAvatarCacheDao<AvatarCacheModel> avatarCacheDao) {
        super(jophiel, avatarCacheDao);
    }

    public static synchronized void buildInstance(Jophiel jophiel, BaseAvatarCacheDao<AvatarCacheModel> avatarCacheDao) {
        if (INSTANCE != null) {
            throw new UnsupportedOperationException("AvatarCacheService instance has already been built");
        }
        INSTANCE = new AvatarCacheServiceImpl(jophiel, avatarCacheDao);
    }

    public static AvatarCacheServiceImpl getInstance() {
        if (INSTANCE == null) {
            throw new UnsupportedOperationException("AvatarCacheService instance has not been built");
        }
        return INSTANCE;
    }
}
