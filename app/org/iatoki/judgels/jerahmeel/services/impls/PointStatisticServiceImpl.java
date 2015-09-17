package org.iatoki.judgels.jerahmeel.services.impls;

import com.google.common.collect.ImmutableMap;
import org.iatoki.judgels.jerahmeel.PointStatistic;
import org.iatoki.judgels.jerahmeel.PointStatisticEntry;
import org.iatoki.judgels.jerahmeel.models.daos.PointStatisticDao;
import org.iatoki.judgels.jerahmeel.models.daos.PointStatisticEntryDao;
import org.iatoki.judgels.jerahmeel.models.entities.PointStatisticEntryModel;
import org.iatoki.judgels.jerahmeel.models.entities.PointStatisticEntryModel_;
import org.iatoki.judgels.jerahmeel.models.entities.PointStatisticModel;
import org.iatoki.judgels.jerahmeel.services.PointStatisticService;
import org.iatoki.judgels.play.Page;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
@Named("pointStatisticService")
public final class PointStatisticServiceImpl implements PointStatisticService {

    private final PointStatisticDao pointStatisticDao;
    private final PointStatisticEntryDao pointStatisticEntryDao;

    @Inject
    public PointStatisticServiceImpl(PointStatisticDao pointStatisticDao, PointStatisticEntryDao pointStatisticEntryDao) {
        this.pointStatisticDao = pointStatisticDao;
        this.pointStatisticEntryDao = pointStatisticEntryDao;
    }

    @Override
    public boolean pointStatisticExists() {
        return pointStatisticDao.countByFilters("") != 0;
    }

    @Override
    public PointStatistic getLatestPointStatisticWithPagination(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        PointStatisticModel pointStatisticModel = pointStatisticDao.findSortedByFilters("time", "desc", "", 0, 1).get(0);
        long totalRowCount = pointStatisticEntryDao.countByFiltersEq(filterString, ImmutableMap.of(PointStatisticEntryModel_.pointStatisticJid, pointStatisticModel.jid));
        List<PointStatisticEntryModel> pointStatisticEntryModels = pointStatisticEntryDao.findSortedByFiltersEq(orderBy, orderDir, filterString, ImmutableMap.of(PointStatisticEntryModel_.pointStatisticJid, pointStatisticModel.jid), pageIndex, pageSize);
        List<PointStatisticEntry> pointStatisticEntries = pointStatisticEntryModels.stream().map(m -> new PointStatisticEntry(m.userJid, m.totalPoints, m.totalProblems)).collect(Collectors.toList());

        return new PointStatistic(new Page<>(pointStatisticEntries, totalRowCount, pageIndex, pageSize), pointStatisticModel.time);
    }

    @Override
    public void updatePointStatistic(List<PointStatisticEntry> pointStatisticEntries, long time) {
        PointStatisticModel pointStatisticModel = new PointStatisticModel();
        pointStatisticModel.time = time;

        pointStatisticDao.persist(pointStatisticModel, "statisticUpdater", "statisticUpdater");

        for (PointStatisticEntry pointStatisticEntry : pointStatisticEntries) {
            PointStatisticEntryModel pointStatisticEntryModel = new PointStatisticEntryModel();
            pointStatisticEntryModel.pointStatisticJid = pointStatisticModel.jid;
            pointStatisticEntryModel.userJid = pointStatisticEntry.getUserJid();
            pointStatisticEntryModel.totalPoints = pointStatisticEntry.getTotalPoints();
            pointStatisticEntryModel.totalProblems = pointStatisticEntry.getTotalProblems();

            pointStatisticEntryDao.persist(pointStatisticEntryModel, "statisticUpdater", "statisticUpdater");
        }
    }
}
