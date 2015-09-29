package org.iatoki.judgels.jerahmeel.models.daos.hibernate;

import org.iatoki.judgels.jerahmeel.models.daos.ProgrammingGradingDao;
import org.iatoki.judgels.jerahmeel.models.entities.ProgrammingGradingModel;
import org.iatoki.judgels.sandalphon.models.daos.hibernate.AbstractProgrammingGradingHibernateDao;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("programmingGradingDao")
public final class ProgrammingGradingHibernateDao extends AbstractProgrammingGradingHibernateDao<ProgrammingGradingModel> implements ProgrammingGradingDao {

    public ProgrammingGradingHibernateDao() {
        super(ProgrammingGradingModel.class);
    }

    @Override
    public ProgrammingGradingModel createGradingModel() {
        return new ProgrammingGradingModel();
    }
}
