package org.iatoki.judgels.jerahmeel.models.daos.hibernate;

import org.iatoki.judgels.commons.models.daos.hibernate.AbstractJidCacheHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.JidCacheDao;
import org.iatoki.judgels.jerahmeel.models.domains.JidCacheModel;

public final class JidCacheHibernateDao extends AbstractJidCacheHibernateDao<JidCacheModel> implements JidCacheDao {
    public JidCacheHibernateDao() {
        super(JidCacheModel.class);
    }

    @Override
    public JidCacheModel createJidCacheModel() {
        return new JidCacheModel();
    }
}
