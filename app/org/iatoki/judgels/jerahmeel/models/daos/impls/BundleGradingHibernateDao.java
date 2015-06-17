package org.iatoki.judgels.jerahmeel.models.daos.impls;

import org.iatoki.judgels.jerahmeel.models.daos.BundleGradingDao;
import org.iatoki.judgels.jerahmeel.models.entities.BundleGradingModel;
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
