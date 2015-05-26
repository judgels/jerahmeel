package org.iatoki.judgels.jerahmeel;

import org.iatoki.judgels.commons.AbstractJidCacheService;
import org.iatoki.judgels.jerahmeel.models.domains.JidCacheModel;

public final class JidCacheService extends AbstractJidCacheService<JidCacheModel> {
    private static final JidCacheService INSTANCE = new JidCacheService();

    private JidCacheService() {
    }

    public static JidCacheService getInstance() {
        return INSTANCE;
    }
}
