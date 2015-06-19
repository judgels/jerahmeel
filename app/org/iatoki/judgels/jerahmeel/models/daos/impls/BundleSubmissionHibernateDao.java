package org.iatoki.judgels.jerahmeel.models.daos.impls;

import org.iatoki.judgels.jerahmeel.models.daos.BundleSubmissionDao;
import org.iatoki.judgels.jerahmeel.models.entities.BundleSubmissionModel;
import org.iatoki.judgels.sandalphon.models.daos.impls.AbstractBundleSubmissionHibernateDao;

public final class BundleSubmissionHibernateDao extends AbstractBundleSubmissionHibernateDao<BundleSubmissionModel> implements BundleSubmissionDao {
    public BundleSubmissionHibernateDao() {
        super(BundleSubmissionModel.class);
    }

    @Override
    public BundleSubmissionModel createSubmissionModel() {
        return new BundleSubmissionModel();
    }

}