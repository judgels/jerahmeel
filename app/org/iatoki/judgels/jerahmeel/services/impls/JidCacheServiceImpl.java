package org.iatoki.judgels.jerahmeel.services.impls;

import org.iatoki.judgels.play.services.impls.AbstractBaseJidCacheServiceImpl;
import org.iatoki.judgels.play.models.daos.interfaces.BaseJidCacheDao;
import org.iatoki.judgels.jerahmeel.models.entities.JidCacheModel;

public final class JidCacheServiceImpl extends AbstractBaseJidCacheServiceImpl<JidCacheModel> {

    private static JidCacheServiceImpl INSTANCE;

    private JidCacheServiceImpl(BaseJidCacheDao<JidCacheModel> jidCacheDao) {
        super(jidCacheDao);
    }

    public static void buildInstance(BaseJidCacheDao<JidCacheModel> jidCacheDao) {
        if (INSTANCE != null) {
            throw new UnsupportedOperationException("JidCacheService instance has already been built");
        }
        INSTANCE = new JidCacheServiceImpl(jidCacheDao);
    }

    public static JidCacheServiceImpl getInstance() {
        if (INSTANCE == null) {
            throw new UnsupportedOperationException("JidCacheService instance has not been built");
        }
        return INSTANCE;
    }
}
