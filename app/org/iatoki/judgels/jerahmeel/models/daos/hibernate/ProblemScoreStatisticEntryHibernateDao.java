package org.iatoki.judgels.jerahmeel.models.daos.hibernate;

import org.iatoki.judgels.jerahmeel.models.daos.ProblemScoreStatisticEntryDao;
import org.iatoki.judgels.jerahmeel.models.entities.ProblemScoreStatisticEntryModel;
import org.iatoki.judgels.play.models.daos.impls.AbstractHibernateDao;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("problemScoreStatisticEntryDao")
public final class ProblemScoreStatisticEntryHibernateDao extends AbstractHibernateDao<Long, ProblemScoreStatisticEntryModel> implements ProblemScoreStatisticEntryDao {

    public ProblemScoreStatisticEntryHibernateDao() {
        super(ProblemScoreStatisticEntryModel.class);
    }
}
