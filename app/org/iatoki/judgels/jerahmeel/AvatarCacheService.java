package org.iatoki.judgels.jerahmeel;

import org.iatoki.judgels.jophiel.commons.AbstractAvatarCacheService;
import org.iatoki.judgels.jerahmeel.models.domains.AvatarCacheModel;

public final class AvatarCacheService extends AbstractAvatarCacheService<AvatarCacheModel> {
    private static final AvatarCacheService INSTANCE = new AvatarCacheService();;

    private AvatarCacheService() {
    }

    public static AvatarCacheService getInstance() {
        return INSTANCE;
    }
}
