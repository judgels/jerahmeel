package org.iatoki.judgels.jerahmeel.models.daos;

import org.iatoki.judgels.jerahmeel.models.entities.ProblemSetProblemModel;
import org.iatoki.judgels.play.models.daos.Dao;

import java.util.List;

public interface ProblemSetProblemDao extends Dao<Long, ProblemSetProblemModel> {

    boolean existsByProblemSetJidAndAlias(String problemSetJid, String alias);

    List<ProblemSetProblemModel> getByProblemSetJid(String problemSetJid);

    ProblemSetProblemModel findByProblemSetJidAndProblemJid(String problemSetJid, String problemJid);
}
