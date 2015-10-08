package org.iatoki.judgels.jerahmeel.services.impls;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.iatoki.judgels.jerahmeel.Archive;
import org.iatoki.judgels.jerahmeel.ProblemSet;
import org.iatoki.judgels.jerahmeel.ProblemSetNotFoundException;
import org.iatoki.judgels.jerahmeel.ProblemSetWithScore;
import org.iatoki.judgels.jerahmeel.models.daos.ArchiveDao;
import org.iatoki.judgels.jerahmeel.models.daos.BundleGradingDao;
import org.iatoki.judgels.jerahmeel.models.daos.BundleSubmissionDao;
import org.iatoki.judgels.jerahmeel.models.daos.ContainerProblemScoreCacheDao;
import org.iatoki.judgels.jerahmeel.models.daos.ContainerScoreCacheDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProblemSetDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProblemSetProblemDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProgrammingGradingDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProgrammingSubmissionDao;
import org.iatoki.judgels.jerahmeel.models.entities.ArchiveModel;
import org.iatoki.judgels.jerahmeel.models.entities.ContainerScoreCacheModel;
import org.iatoki.judgels.jerahmeel.models.entities.ProblemSetModel;
import org.iatoki.judgels.jerahmeel.models.entities.ProblemSetModel_;
import org.iatoki.judgels.jerahmeel.models.entities.ProblemSetProblemModel;
import org.iatoki.judgels.jerahmeel.models.entities.ProblemSetProblemModel_;
import org.iatoki.judgels.jerahmeel.services.ProblemSetService;
import org.iatoki.judgels.play.Page;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
@Named("problemSetService")
public final class ProblemSetServiceImpl implements ProblemSetService {

    private final ArchiveDao archiveDao;
    private final BundleGradingDao bundleGradingDao;
    private final BundleSubmissionDao bundleSubmissionDao;
    private final ContainerScoreCacheDao containerScoreCacheDao;
    private final ContainerProblemScoreCacheDao containerProblemScoreCacheDao;
    private final ProblemSetDao problemSetDao;
    private final ProblemSetProblemDao problemSetProblemDao;
    private final ProgrammingGradingDao programmingGradingDao;
    private final ProgrammingSubmissionDao programmingSubmissionDao;

    @Inject
    public ProblemSetServiceImpl(ArchiveDao archiveDao, BundleGradingDao bundleGradingDao, BundleSubmissionDao bundleSubmissionDao, ContainerScoreCacheDao containerScoreCacheDao, ContainerProblemScoreCacheDao containerProblemScoreCacheDao, ProblemSetDao problemSetDao, ProblemSetProblemDao problemSetProblemDao, ProgrammingGradingDao programmingGradingDao, ProgrammingSubmissionDao programmingSubmissionDao) {
        this.archiveDao = archiveDao;
        this.bundleGradingDao = bundleGradingDao;
        this.bundleSubmissionDao = bundleSubmissionDao;
        this.containerScoreCacheDao = containerScoreCacheDao;
        this.containerProblemScoreCacheDao = containerProblemScoreCacheDao;
        this.problemSetDao = problemSetDao;
        this.problemSetProblemDao = problemSetProblemDao;
        this.programmingGradingDao = programmingGradingDao;
        this.programmingSubmissionDao = programmingSubmissionDao;
    }

    @Override
    public Page<ProblemSet> getPageOfProblemSets(Archive archive, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalRowsCount = problemSetDao.countByFiltersEq(filterString, ImmutableMap.of(ProblemSetModel_.archiveJid, archive.getJid()));
        List<ProblemSetModel> problemSetModels = problemSetDao.findSortedByFiltersEq(orderBy, orderDir, filterString, ImmutableMap.of(ProblemSetModel_.archiveJid, archive.getJid()), pageIndex, pageSize);

        List<ProblemSet> problemSets = problemSetModels.stream().map(m -> ProblemSetServiceUtils.createProblemSetFromModelAndArchive(m, archive)).collect(Collectors.toList());

        return new Page<>(problemSets, totalRowsCount, pageIndex, pageSize);
    }

    @Override
    public Page<ProblemSetWithScore> getPageOfProblemSetsWithScore(Archive archive, String userJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalRowsCount = problemSetDao.countByFiltersEq(filterString, ImmutableMap.of(ProblemSetModel_.archiveJid, archive.getJid()));
        List<ProblemSetModel> problemSetModels = problemSetDao.findSortedByFiltersEq(orderBy, orderDir, filterString, ImmutableMap.of(ProblemSetModel_.archiveJid, archive.getJid()), pageIndex, pageSize);

        ImmutableList.Builder<ProblemSetWithScore> problemSetWithScoreBuilder = ImmutableList.builder();
        for (ProblemSetModel problemSetModel : problemSetModels) {
            double totalScore;
            if (containerScoreCacheDao.existsByUserJidAndContainerJid(userJid, problemSetModel.jid)) {
                ContainerScoreCacheModel containerScoreCacheModel = containerScoreCacheDao.getByUserJidAndContainerJid(userJid, problemSetModel.jid);
                totalScore = containerScoreCacheModel.score;
            } else {
                List<ProblemSetProblemModel> problemSetProblemModels = problemSetProblemDao.findSortedByFiltersEq("id", "asc", "", ImmutableMap.of(ProblemSetProblemModel_.problemSetJid, problemSetModel.jid), 0, -1);

                totalScore = ProblemSetProblemServiceUtils.getUserTotalScoreFromProblemSetProblemModels(containerProblemScoreCacheDao, bundleSubmissionDao, bundleGradingDao, programmingSubmissionDao, programmingGradingDao, userJid, problemSetProblemModels);

                ContainerScoreCacheServiceUtils.addToContainerScoreCache(containerScoreCacheDao, userJid, problemSetModel.jid, totalScore);
            }

            problemSetWithScoreBuilder.add(new ProblemSetWithScore(ProblemSetServiceUtils.createProblemSetFromModelAndArchive(problemSetModel, archive), totalScore));
        }

        return new Page<>(problemSetWithScoreBuilder.build(), totalRowsCount, pageIndex, pageSize);
    }

    @Override
    public ProblemSet findProblemSetById(long problemSetId) throws ProblemSetNotFoundException {
        ProblemSetModel problemSetModel = problemSetDao.findById(problemSetId);

        if (problemSetModel == null) {
            throw new ProblemSetNotFoundException("Archive Contest Not Found.");
        }

        ArchiveModel archiveModel = archiveDao.findByJid(problemSetModel.archiveJid);
        Archive archive = ArchiveServiceUtils.createArchiveWithParentsFromModel(archiveDao, archiveModel);

        return ProblemSetServiceUtils.createProblemSetFromModelAndArchive(problemSetModel, archive);
    }

    @Override
    public ProblemSet findProblemSetByJid(String problemSetJid) {
        ProblemSetModel problemSetModel = problemSetDao.findByJid(problemSetJid);

        ArchiveModel archiveModel = archiveDao.findByJid(problemSetModel.archiveJid);
        Archive archive = ArchiveServiceUtils.createArchiveWithParentsFromModel(archiveDao, archiveModel);

        return ProblemSetServiceUtils.createProblemSetFromModelAndArchive(problemSetModel, archive);
    }

    @Override
    public Map<String, String> getProblemSetJidToNameMapByProblemSetJids(Collection<String> problemSetJids) {
        List<ProblemSetModel> problemSetModels = problemSetDao.findSortedByFiltersIn("id", "asc", "", ImmutableMap.of(ProblemSetModel_.jid, problemSetJids), 0, -1);

        ImmutableMap.Builder<String, String> problemSetJidToNameMap = ImmutableMap.builder();
        for (ProblemSetModel problemSetModel : problemSetModels) {
            problemSetJidToNameMap.put(problemSetModel.jid, problemSetModel.name);
        }

        return problemSetJidToNameMap.build();
    }

    @Override
    public void createProblemSet(String archiveJid, String name, String description, String userJid, String userIpAddress) {
        ProblemSetModel problemSetModel = new ProblemSetModel();
        problemSetModel.archiveJid = archiveJid;
        problemSetModel.name = name;
        problemSetModel.description = description;

        problemSetDao.persist(problemSetModel, userJid, userIpAddress);

        String parentArchiveJid = problemSetModel.archiveJid;
        while ((parentArchiveJid != null) && !parentArchiveJid.isEmpty()) {
            ArchiveModel archiveModel = archiveDao.findByJid(parentArchiveJid);
            archiveDao.edit(archiveModel, userJid, userIpAddress);

            parentArchiveJid = archiveModel.parentJid;
        }
    }

    @Override
    public void updateProblemSet(String problemSetJid, String archiveJid, String name, String description, String userJid, String userIpAddress) {
        ProblemSetModel problemSetModel = problemSetDao.findByJid(problemSetJid);
        problemSetModel.archiveJid = archiveJid;
        problemSetModel.name = name;
        problemSetModel.description = description;

        problemSetDao.edit(problemSetModel, userJid, userIpAddress);

        String parentArchiveJid = problemSetModel.archiveJid;
        while ((parentArchiveJid != null) && !parentArchiveJid.isEmpty()) {
            ArchiveModel archiveModel = archiveDao.findByJid(parentArchiveJid);
            archiveDao.edit(archiveModel, userJid, userIpAddress);

            parentArchiveJid = archiveModel.parentJid;
        }
    }
}
