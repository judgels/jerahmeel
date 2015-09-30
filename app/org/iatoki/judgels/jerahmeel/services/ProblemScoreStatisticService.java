package org.iatoki.judgels.jerahmeel.services;

import org.iatoki.judgels.jerahmeel.ProblemScoreStatistic;
import org.iatoki.judgels.jerahmeel.ProblemScoreStatisticEntry;

import java.util.List;

public interface ProblemScoreStatisticService {

    boolean problemScoreStatisticExists(String problemJid);

    ProblemScoreStatistic getLatestProblemScoreStatisticWithPagination(String problemJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    void updateProblemStatistic(List<ProblemScoreStatisticEntry> problemStatisticEntries, String problemJid, long time);
}
