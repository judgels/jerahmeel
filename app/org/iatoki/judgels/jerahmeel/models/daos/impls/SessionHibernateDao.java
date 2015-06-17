package org.iatoki.judgels.jerahmeel.models.daos.impls;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.commons.models.daos.hibernate.AbstractJudgelsHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.SessionDao;
import org.iatoki.judgels.jerahmeel.models.entities.SessionModel;
import org.iatoki.judgels.jerahmeel.models.entities.SessionModel_;

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
