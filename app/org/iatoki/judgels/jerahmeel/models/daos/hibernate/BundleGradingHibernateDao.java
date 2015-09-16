package org.iatoki.judgels.jerahmeel.models.daos.hibernate;

import org.iatoki.judgels.jerahmeel.models.daos.BundleGradingDao;
import org.iatoki.judgels.jerahmeel.models.entities.BundleGradingModel;
import org.iatoki.judgels.sandalphon.models.daos.hibernate.AbstractBundleGradingHibernateDao;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("bundleGradingDao")
public final class BundleGradingHibernateDao extends AbstractBundleGradingHibernateDao<BundleGradingModel> implements BundleGradingDao {

    public BundleGradingHibernateDao() {
        super(BundleGradingModel.class);
    }

    @Override
    public BundleGradingModel createGradingModel() {
        return new BundleGradingModel();
    }
}
