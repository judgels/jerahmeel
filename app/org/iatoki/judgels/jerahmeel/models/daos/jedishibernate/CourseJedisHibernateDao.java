package org.iatoki.judgels.jerahmeel.models.daos.jedishibernate;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.jerahmeel.models.daos.CourseDao;
import org.iatoki.judgels.jerahmeel.models.entities.CourseModel;
import org.iatoki.judgels.jerahmeel.models.entities.CourseModel_;
import org.iatoki.judgels.play.models.daos.impls.AbstractJudgelsJedisHibernateDao;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.metamodel.SingularAttribute;
import java.util.List;

@Singleton
@Named("courseDao")
public final class CourseJedisHibernateDao extends AbstractJudgelsJedisHibernateDao<CourseModel> implements CourseDao {

    @Inject
    public CourseJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, CourseModel.class);
    }

    @Override
    protected List<SingularAttribute<CourseModel, String>> getColumnsFilterableByString() {
        return ImmutableList.of(CourseModel_.name, CourseModel_.description);
    }
}
