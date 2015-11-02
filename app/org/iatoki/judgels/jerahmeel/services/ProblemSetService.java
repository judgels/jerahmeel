package org.iatoki.judgels.jerahmeel.services;

import org.iatoki.judgels.jerahmeel.Archive;
import org.iatoki.judgels.jerahmeel.ProblemSet;
import org.iatoki.judgels.jerahmeel.ProblemSetNotFoundException;
import org.iatoki.judgels.jerahmeel.ProblemSetWithScore;
import org.iatoki.judgels.play.Page;

import java.util.Collection;
import java.util.Map;

public interface ProblemSetService {

    Page<ProblemSet> getPageOfProblemSets(Archive archive, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    Page<ProblemSetWithScore> getPageOfProblemSetsWithScore(Archive archive, String userJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    boolean problemSetExistsByJid(String problemSetJid);

    ProblemSet findProblemSetById(long problemSetId) throws ProblemSetNotFoundException;

    ProblemSet findProblemSetByJid(String problemSetJid);

    Map<String, String> getProblemSetJidToNameMapByProblemSetJids(Collection<String> problemSetJids);

    void createProblemSet(String archiveJid, String name, String description, String userJid, String userIpAddress);

    void updateProblemSet(String problemSetJid, String archiveJid, String name, String description, String userJid, String userIpAddress);
}
