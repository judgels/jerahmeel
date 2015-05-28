package org.iatoki.judgels.jerahmeel.models.daos.hibernate;

import org.iatoki.judgels.sandalphon.commons.models.daos.hibernate.AbstractGradingHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.GradingDao;
import org.iatoki.judgels.jerahmeel.models.domains.GradingModel;

public final class GradingHibernateDao extends AbstractGradingHibernateDao<GradingModel> implements GradingDao {
    public GradingHibernateDao() {
        super(GradingModel.class);
    }

    @Override
    public GradingModel createGradingModel() {
        return new GradingModel();
    }
}
