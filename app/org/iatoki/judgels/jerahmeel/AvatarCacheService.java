package org.iatoki.judgels.jerahmeel;

import org.iatoki.judgels.jophiel.commons.AbstractAvatarCacheService;
import org.iatoki.judgels.jerahmeel.models.domains.AvatarCacheModel;
import org.iatoki.judgels.jophiel.commons.Jophiel;
import org.iatoki.judgels.jophiel.commons.models.daos.interfaces.BaseAvatarCacheDao;

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
