package org.iatoki.judgels.jerahmeel.models.daos.hibernate;

import org.iatoki.judgels.jerahmeel.models.daos.PointStatisticEntryDao;
import org.iatoki.judgels.jerahmeel.models.entities.PointStatisticEntryModel;
import org.iatoki.judgels.play.models.daos.impls.AbstractHibernateDao;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("pointStatisticEntryDao")
public final class PointStatisticEntryHibernateDao extends AbstractHibernateDao<Long, PointStatisticEntryModel> implements PointStatisticEntryDao {

    public PointStatisticEntryHibernateDao() {
        super(PointStatisticEntryModel.class);
    }
}
