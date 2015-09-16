package org.iatoki.judgels.jerahmeel.models.daos.hibernate;

import org.iatoki.judgels.play.models.daos.impls.AbstractJidCacheHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.JidCacheDao;
import org.iatoki.judgels.jerahmeel.models.entities.JidCacheModel;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("jidCacheDao")
public final class JidCacheHibernateDao extends AbstractJidCacheHibernateDao<JidCacheModel> implements JidCacheDao {

    public JidCacheHibernateDao() {
        super(JidCacheModel.class);
    }

    @Override
    public JidCacheModel createJidCacheModel() {
        return new JidCacheModel();
    }
}
