package org.iatoki.judgels.jerahmeel.models.daos.jedishibernate;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.jerahmeel.models.daos.ChapterDao;
import org.iatoki.judgels.jerahmeel.models.entities.ChapterModel;
import org.iatoki.judgels.jerahmeel.models.entities.ChapterModel_;
import org.iatoki.judgels.play.models.daos.impls.AbstractJudgelsJedisHibernateDao;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.metamodel.SingularAttribute;
import java.util.List;

@Singleton
@Named("chapterDao")
public final class ChapterJedisHibernateDao extends AbstractJudgelsJedisHibernateDao<ChapterModel> implements ChapterDao {

    @Inject
    public ChapterJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, ChapterModel.class);
    }

    @Override
    protected List<SingularAttribute<ChapterModel, String>> getColumnsFilterableByString() {
        return ImmutableList.of(ChapterModel_.name, ChapterModel_.description);
    }
}
