package org.iatoki.judgels.jerahmeel.models.daos.hibernate;

import org.iatoki.judgels.gabriel.commons.models.daos.hibernate.AbstractSubmissionHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.SubmissionDao;
import org.iatoki.judgels.jerahmeel.models.domains.SubmissionModel;

public final class SubmissionHibernateDao extends AbstractSubmissionHibernateDao<SubmissionModel> implements SubmissionDao {
    public SubmissionHibernateDao() {
        super(SubmissionModel.class);
    }

    @Override
    public SubmissionModel createSubmissionModel() {
        return new SubmissionModel();
    }

}