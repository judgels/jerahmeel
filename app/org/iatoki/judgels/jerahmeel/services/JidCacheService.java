package org.iatoki.judgels.jerahmeel.services;

import org.iatoki.judgels.commons.AbstractJidCacheService;
import org.iatoki.judgels.commons.models.daos.interfaces.BaseJidCacheDao;
import org.iatoki.judgels.jerahmeel.models.entities.JidCacheModel;

public final class JidCacheService extends AbstractJidCacheService<JidCacheModel> {
    private static JidCacheService INSTANCE;

    private JidCacheService(BaseJidCacheDao<JidCacheModel> jidCacheDao) {
        super(jidCacheDao);
    }

    public static void buildInstance(BaseJidCacheDao<JidCacheModel> jidCacheDao) {
        if (INSTANCE != null) {
            throw new UnsupportedOperationException("JidCacheService instance has already been built");
        }
        INSTANCE = new JidCacheService(jidCacheDao);
    }

    public static JidCacheService getInstance() {
        if (INSTANCE == null) {
            throw new UnsupportedOperationException("JidCacheService instance has not been built");
        }
        return INSTANCE;
    }
}
