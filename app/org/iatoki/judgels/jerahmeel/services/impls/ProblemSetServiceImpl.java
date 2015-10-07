package org.iatoki.judgels.jerahmeel.services.impls;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.iatoki.judgels.jerahmeel.Archive;
import org.iatoki.judgels.jerahmeel.ProblemSet;
import org.iatoki.judgels.jerahmeel.ProblemSetNotFoundException;
import org.iatoki.judgels.jerahmeel.ProblemSetProblemType;
import org.iatoki.judgels.jerahmeel.ProblemSetWithScore;
import org.iatoki.judgels.jerahmeel.models.daos.ArchiveDao;
import org.iatoki.judgels.jerahmeel.models.daos.BundleGradingDao;
import org.iatoki.judgels.jerahmeel.models.daos.BundleSubmissionDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProblemSetDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProblemSetProblemDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProgrammingGradingDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProgrammingSubmissionDao;
import org.iatoki.judgels.jerahmeel.models.entities.ArchiveModel;
import org.iatoki.judgels.jerahmeel.models.entities.BundleGradingModel;
import org.iatoki.judgels.jerahmeel.models.entities.BundleSubmissionModel;
import org.iatoki.judgels.jerahmeel.models.entities.ProblemSetModel;
import org.iatoki.judgels.jerahmeel.models.entities.ProblemSetModel_;
import org.iatoki.judgels.jerahmeel.models.entities.ProblemSetProblemModel;
import org.iatoki.judgels.jerahmeel.models.entities.ProblemSetProblemModel_;
import org.iatoki.judgels.jerahmeel.models.entities.ProgrammingGradingModel;
import org.iatoki.judgels.jerahmeel.models.entities.ProgrammingSubmissionModel;
import org.iatoki.judgels.jerahmeel.services.ProblemSetService;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.sandalphon.BundleSubmission;
import org.iatoki.judgels.sandalphon.ProgrammingSubmission;
import org.iatoki.judgels.sandalphon.models.entities.AbstractBundleGradingModel_;
import org.iatoki.judgels.sandalphon.models.entities.AbstractProgrammingGradingModel_;
import org.iatoki.judgels.sandalphon.services.impls.BundleSubmissionServiceUtils;
import org.iatoki.judgels.sandalphon.services.impls.ProgrammingSubmissionServiceUtils;

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
    private final ProblemSetDao problemSetDao;
    private final ProblemSetProblemDao problemSetProblemDao;
    private final ProgrammingGradingDao programmingGradingDao;
    private final ProgrammingSubmissionDao programmingSubmissionDao;

    @Inject
    public ProblemSetServiceImpl(ArchiveDao archiveDao, BundleGradingDao bundleGradingDao, BundleSubmissionDao bundleSubmissionDao, ProblemSetDao problemSetDao, ProblemSetProblemDao problemSetProblemDao, ProgrammingGradingDao programmingGradingDao, ProgrammingSubmissionDao programmingSubmissionDao) {
        this.archiveDao = archiveDao;
        this.bundleGradingDao = bundleGradingDao;
        this.bundleSubmissionDao = bundleSubmissionDao;
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
            List<ProblemSetProblemModel> problemSetProblemModels = problemSetProblemDao.findSortedByFiltersEq("id", "asc", "", ImmutableMap.of(ProblemSetProblemModel_.problemSetJid, problemSetModel.jid), 0, -1);

            double totalScore = 0;
            for (ProblemSetProblemModel problemSetProblemModel : problemSetProblemModels) {
                double maxScore = -1;
                if (problemSetProblemModel.type.equals(ProblemSetProblemType.BUNDLE.name())) {
                    List<BundleSubmissionModel> bundleSubmissionModels = bundleSubmissionDao.getByContainerJidAndUserJidAndProblemJid(problemSetModel.jid, userJid, problemSetProblemModel.problemJid);
                    for (BundleSubmissionModel bundleSubmissionModel : bundleSubmissionModels) {
                        List<BundleGradingModel> gradingModels = bundleGradingDao.findSortedByFiltersEq("id", "asc", "", ImmutableMap.of(AbstractBundleGradingModel_.submissionJid, bundleSubmissionModel.jid), 0, -1);
                        BundleSubmission bundleSubmission = BundleSubmissionServiceUtils.createSubmissionFromModels(bundleSubmissionModel, gradingModels);

                        if (bundleSubmission.getLatestScore() > maxScore) {
                            maxScore = bundleSubmission.getLatestScore();
                        }
                    }
                } else if (problemSetProblemModel.type.equals(ProblemSetProblemType.PROGRAMMING.name())) {
                    List<ProgrammingSubmissionModel> programmingSubmissionModels = programmingSubmissionDao.getByContainerJidAndUserJidAndProblemJid(problemSetModel.jid, userJid, problemSetProblemModel.problemJid);
                    for (ProgrammingSubmissionModel programmingSubmissionModel : programmingSubmissionModels) {
                        List<ProgrammingGradingModel> gradingModels = programmingGradingDao.findSortedByFiltersEq("id", "asc", "", ImmutableMap.of(AbstractProgrammingGradingModel_.submissionJid, programmingSubmissionModel.jid), 0, -1);
                        ProgrammingSubmission programmingSubmission = ProgrammingSubmissionServiceUtils.createSubmissionFromModels(programmingSubmissionModel, gradingModels);

                        if (programmingSubmission.getLatestScore() > maxScore) {
                            maxScore = programmingSubmission.getLatestScore();
                        }
                    }
                }
                if (Double.compare(maxScore, -1) != 0) {
                    totalScore += maxScore;
                }
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
    public void createProblemSet(String archiveJid, String name, String description) {
        ProblemSetModel problemSetModel = new ProblemSetModel();
        problemSetModel.archiveJid = archiveJid;
        problemSetModel.name = name;
        problemSetModel.description = description;

        problemSetDao.persist(problemSetModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        String parentArchiveJid = problemSetModel.archiveJid;
        while ((parentArchiveJid != null) && !parentArchiveJid.isEmpty()) {
            ArchiveModel archiveModel = archiveDao.findByJid(parentArchiveJid);
            archiveDao.edit(archiveModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

            parentArchiveJid = archiveModel.parentJid;
        }
    }
}
