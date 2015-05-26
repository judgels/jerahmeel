package org.iatoki.judgels.jerahmeel.models.daos.hibernate;

import org.iatoki.judgels.jerahmeel.models.daos.interfaces.BundleGradingDao;
import org.iatoki.judgels.jerahmeel.models.domains.BundleGradingModel;
import org.iatoki.judgels.sandalphon.commons.models.daos.hibernate.AbstractBundleGradingHibernateDao;

public final class BundleGradingHibernateDao extends AbstractBundleGradingHibernateDao<BundleGradingModel> implements BundleGradingDao {
    public BundleGradingHibernateDao() {
        super(BundleGradingModel.class);
    }

    @Override
    public BundleGradingModel createGradingModel() {
        return new BundleGradingModel();
    }
}
