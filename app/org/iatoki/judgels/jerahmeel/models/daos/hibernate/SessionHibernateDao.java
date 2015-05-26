package org.iatoki.judgels.jerahmeel.models.daos.hibernate;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.commons.models.daos.hibernate.AbstractJudgelsHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.SessionDao;
import org.iatoki.judgels.jerahmeel.models.domains.SessionModel;
import org.iatoki.judgels.jerahmeel.models.domains.SessionModel_;

import javax.persistence.metamodel.SingularAttribute;
import java.util.List;

public final class SessionHibernateDao extends AbstractJudgelsHibernateDao<SessionModel> implements SessionDao {

    public SessionHibernateDao() {
        super(SessionModel.class);
    }

    @Override
    protected List<SingularAttribute<SessionModel, String>> getColumnsFilterableByString() {
        return ImmutableList.of(SessionModel_.name, SessionModel_.description);
    }
}
