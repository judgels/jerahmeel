package org.iatoki.judgels.jerahmeel.models.daos.hibernate;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.commons.models.daos.hibernate.AbstractJudgelsHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.CourseDao;
import org.iatoki.judgels.jerahmeel.models.domains.CourseModel;
import org.iatoki.judgels.jerahmeel.models.domains.CourseModel_;

import javax.persistence.metamodel.SingularAttribute;
import java.util.List;

public final class CourseHibernateDao extends AbstractJudgelsHibernateDao<CourseModel> implements CourseDao {

    public CourseHibernateDao() {
        super(CourseModel.class);
    }

    @Override
    protected List<SingularAttribute<CourseModel, String>> getColumnsFilterableByString() {
        return ImmutableList.of(CourseModel_.name, CourseModel_.description);
    }
}
