package org.iatoki.judgels.jerahmeel.models.daos.hibernate;

import org.iatoki.judgels.jerahmeel.models.daos.interfaces.BundleSubmissionDao;
import org.iatoki.judgels.jerahmeel.models.domains.BundleSubmissionModel;
import org.iatoki.judgels.sandalphon.commons.models.daos.hibernate.AbstractBundleSubmissionHibernateDao;

public final class BundleSubmissionHibernateDao extends AbstractBundleSubmissionHibernateDao<BundleSubmissionModel> implements BundleSubmissionDao {
    public BundleSubmissionHibernateDao() {
        super(BundleSubmissionModel.class);
    }

    @Override
    public BundleSubmissionModel createSubmissionModel() {
        return new BundleSubmissionModel();
    }

}