package org.iatoki.judgels.jerahmeel.models.daos.hibernate;

import org.iatoki.judgels.jerahmeel.models.daos.ProblemStatisticEntryDao;
import org.iatoki.judgels.jerahmeel.models.entities.ProblemStatisticEntryModel;
import org.iatoki.judgels.play.models.daos.impls.AbstractHibernateDao;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("problemStatisticEntryDao")
public final class ProblemStatisticEntryHibernateDao extends AbstractHibernateDao<Long, ProblemStatisticEntryModel> implements ProblemStatisticEntryDao {

    public ProblemStatisticEntryHibernateDao() {
        super(ProblemStatisticEntryModel.class);
    }
}
