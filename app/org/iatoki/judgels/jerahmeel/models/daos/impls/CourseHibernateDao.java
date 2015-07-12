package org.iatoki.judgels.jerahmeel.models.daos.impls;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.play.models.daos.hibernate.AbstractJudgelsHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.CourseDao;
import org.iatoki.judgels.jerahmeel.models.entities.CourseModel;
import org.iatoki.judgels.jerahmeel.models.entities.CourseModel_;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.metamodel.SingularAttribute;
import java.util.List;

@Singleton
@Named("courseDao")
public final class CourseHibernateDao extends AbstractJudgelsHibernateDao<CourseModel> implements CourseDao {

    public CourseHibernateDao() {
        super(CourseModel.class);
    }

    @Override
    protected List<SingularAttribute<CourseModel, String>> getColumnsFilterableByString() {
        return ImmutableList.of(CourseModel_.name, CourseModel_.description);
    }
}
