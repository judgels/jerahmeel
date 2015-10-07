package org.iatoki.judgels.jerahmeel.services.impls;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.iatoki.judgels.jerahmeel.Archive;
import org.iatoki.judgels.jerahmeel.ArchiveNotFoundException;
import org.iatoki.judgels.jerahmeel.ArchiveWithScore;
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
import org.iatoki.judgels.jerahmeel.models.entities.ArchiveModel_;
import org.iatoki.judgels.jerahmeel.services.ArchiveService;
import org.iatoki.judgels.play.IdentityUtils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.Stack;

@Singleton
@Named("archiveService")
public final class ArchiveServiceImpl implements ArchiveService {

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
    public ArchiveServiceImpl(ArchiveDao archiveDao, BundleGradingDao bundleGradingDao, BundleSubmissionDao bundleSubmissionDao, ContainerScoreCacheDao containerScoreCacheDao, ContainerProblemScoreCacheDao containerProblemScoreCacheDao, ProblemSetDao problemSetDao, ProblemSetProblemDao problemSetProblemDao, ProgrammingGradingDao programmingGradingDao, ProgrammingSubmissionDao programmingSubmissionDao) {
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
    public boolean archiveExistsByJid(String archiveJid) {
        return archiveDao.existsByJid(archiveJid);
    }

    @Override
    public List<Archive> getAllArchives() {
        Stack<Archive> archiveStack = new Stack<>();
        archiveStack.add(null);

        ImmutableList.Builder<Archive> archiveBuilder = ImmutableList.builder();

        while (!archiveStack.isEmpty()) {
            Archive currentArchive = archiveStack.pop();

            String currentJid;
            if (currentArchive != null) {
                currentJid = currentArchive.getJid();
                archiveBuilder.add(currentArchive);
            } else {
                currentJid = "";
            }

            List<ArchiveModel> archiveModels = archiveDao.findSortedByFiltersEq("id", "asc", "", ImmutableMap.of(ArchiveModel_.parentJid, currentJid), 0, -1);
            for (ArchiveModel archiveModel : archiveModels) {
                Archive archive = ArchiveServiceUtils.createArchiveFromModel(archiveModel, currentArchive);
                if (currentArchive != null) {
                    currentArchive.getSubArchives().add(archive);
                }
                archiveStack.push(archive);
            }
        }

        return archiveBuilder.build();
    }

    @Override
    public List<Archive> getChildArchives(String parentJid) {
        return ArchiveServiceUtils.getChildArchives(archiveDao, parentJid);
    }

    @Override
    public List<ArchiveWithScore> getChildArchivesWithScore(String parentJid, String userJid) {
        List<Archive> directSubArchives = getChildArchives(parentJid);

        ImmutableList.Builder<ArchiveWithScore> directSubArchivesWithScore = ImmutableList.builder();
        for (Archive subArchive : directSubArchives) {
            double totalScore = ArchiveServiceUtils.getArchiveScore(containerScoreCacheDao, containerProblemScoreCacheDao, archiveDao, problemSetDao, problemSetProblemDao, bundleSubmissionDao, bundleGradingDao, programmingSubmissionDao, programmingGradingDao, userJid, subArchive.getJid());

            directSubArchivesWithScore.add(new ArchiveWithScore(subArchive, totalScore));
        }

        return directSubArchivesWithScore.build();
    }

    @Override
    public Archive findArchiveById(long archiveId) throws ArchiveNotFoundException {
        ArchiveModel intendedArchiveModel = archiveDao.findById(archiveId);

        if (intendedArchiveModel == null) {
            throw new ArchiveNotFoundException("Archive Not Found.");
        }

        return ArchiveServiceUtils.createArchiveWithParentAndSubArchivesFromModel(archiveDao, intendedArchiveModel);
    }

    @Override
    public void createArchive(String parentJid, String name, String description) {
        ArchiveModel archiveModel = new ArchiveModel();
        archiveModel.parentJid = parentJid;
        archiveModel.name = name;
        archiveModel.description = description;

        archiveDao.persist(archiveModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void updateArchive(String archiveJid, String parentJid, String name, String description) {
        ArchiveModel archiveModel = archiveDao.findByJid(archiveJid);
        archiveModel.parentJid = parentJid;
        archiveModel.name = name;
        archiveModel.description = description;

        archiveDao.persist(archiveModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }
}
