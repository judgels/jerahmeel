package org.iatoki.judgels.jerahmeel.services;

import org.iatoki.judgels.jophiel.services.AbstractAvatarCacheService;
import org.iatoki.judgels.jerahmeel.models.entities.AvatarCacheModel;
import org.iatoki.judgels.jophiel.Jophiel;
import org.iatoki.judgels.jophiel.models.daos.BaseAvatarCacheDao;

public final class AvatarCacheService extends AbstractAvatarCacheService<AvatarCacheModel> {

    private static AvatarCacheService INSTANCE;

    private AvatarCacheService(Jophiel jophiel, BaseAvatarCacheDao<AvatarCacheModel> avatarCacheDao) {
        super(jophiel, avatarCacheDao);
    }

    public static synchronized void buildInstance(Jophiel jophiel, BaseAvatarCacheDao<AvatarCacheModel> avatarCacheDao) {
        if (INSTANCE != null) {
            throw new UnsupportedOperationException("AvatarCacheService instance has already been built");
        }
        INSTANCE = new AvatarCacheService(jophiel, avatarCacheDao);
    }

    public static AvatarCacheService getInstance() {
        if (INSTANCE == null) {
            throw new UnsupportedOperationException("AvatarCacheService instance has not been built");
        }
        return INSTANCE;
    }
}
