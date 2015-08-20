package org.iatoki.judgels.jerahmeel.models.daos.impls;

import org.iatoki.judgels.sandalphon.models.daos.impls.AbstractProgrammingGradingHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProgrammingGradingDao;
import org.iatoki.judgels.jerahmeel.models.entities.ProgrammingGradingModel;

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
