package org.iatoki.judgels.jerahmeel.models.daos.impls;

import org.iatoki.judgels.sandalphon.commons.models.daos.hibernate.AbstractSubmissionHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.SubmissionDao;
import org.iatoki.judgels.jerahmeel.models.entities.SubmissionModel;

public final class SubmissionHibernateDao extends AbstractSubmissionHibernateDao<SubmissionModel> implements SubmissionDao {
    public SubmissionHibernateDao() {
        super(SubmissionModel.class);
    }

    @Override
    public SubmissionModel createSubmissionModel() {
        return new SubmissionModel();
    }

}