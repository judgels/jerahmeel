package org.iatoki.judgels.jerahmeel.services;

import org.iatoki.judgels.jerahmeel.ProblemStatistic;
import org.iatoki.judgels.jerahmeel.ProblemStatisticEntry;

import java.util.List;

public interface ProblemStatisticService {

    boolean problemStatisticExists();

    ProblemStatistic getLatestProblemStatisticWithPagination(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    void updateProblemStatistic(List<ProblemStatisticEntry> problemStatisticEntries, long time);
}
