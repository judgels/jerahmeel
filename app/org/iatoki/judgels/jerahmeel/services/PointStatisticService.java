package org.iatoki.judgels.jerahmeel.services;

import org.iatoki.judgels.jerahmeel.PointStatistic;
import org.iatoki.judgels.jerahmeel.PointStatisticEntry;

import java.util.List;

public interface PointStatisticService {

    boolean pointStatisticExists();

    PointStatistic getLatestPointStatisticWithPagination(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    void updatePointStatistic(List<PointStatisticEntry> pointStatisticEntries, long time);
}
