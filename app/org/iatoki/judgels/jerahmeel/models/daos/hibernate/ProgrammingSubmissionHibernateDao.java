package org.iatoki.judgels.jerahmeel.models.daos.hibernate;

import org.iatoki.judgels.jerahmeel.models.daos.ProgrammingSubmissionDao;
import org.iatoki.judgels.jerahmeel.models.entities.ProgrammingSubmissionModel;
import org.iatoki.judgels.sandalphon.models.daos.hibernate.AbstractProgrammingSubmissionHibernateDao;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("programmingSubmissionDao")
public final class ProgrammingSubmissionHibernateDao extends AbstractProgrammingSubmissionHibernateDao<ProgrammingSubmissionModel> implements ProgrammingSubmissionDao {

    public ProgrammingSubmissionHibernateDao() {
        super(ProgrammingSubmissionModel.class);
    }

    @Override
    public ProgrammingSubmissionModel createSubmissionModel() {
        return new ProgrammingSubmissionModel();
    }
}
