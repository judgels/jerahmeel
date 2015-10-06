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
import org.iatoki.judgels.jerahmeel.models.daos.ProblemSetDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProblemSetProblemDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProgrammingGradingDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProgrammingSubmissionDao;
import org.iatoki.judgels.jerahmeel.models.entities.ArchiveModel;
import org.iatoki.judgels.jerahmeel.models.entities.ArchiveModel_;
import org.iatoki.judgels.jerahmeel.models.entities.ProblemSetModel;
import org.iatoki.judgels.jerahmeel.models.entities.ProblemSetModel_;
import org.iatoki.judgels.jerahmeel.models.entities.ProblemSetProblemModel;
import org.iatoki.judgels.jerahmeel.models.entities.ProblemSetProblemModel_;
import org.iatoki.judgels.jerahmeel.services.ArchiveService;
import org.iatoki.judgels.play.IdentityUtils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

@Singleton
@Named("archiveService")
public final class ArchiveServiceImpl implements ArchiveService {

    private final ArchiveDao archiveDao;
    private final BundleGradingDao bundleGradingDao;
    private final BundleSubmissionDao bundleSubmissionDao;
    private final ContainerProblemScoreCacheDao containerProblemScoreCacheDao;
    private final ProblemSetDao problemSetDao;
    private final ProblemSetProblemDao problemSetProblemDao;
    private final ProgrammingGradingDao programmingGradingDao;
    private final ProgrammingSubmissionDao programmingSubmissionDao;

    @Inject
    public ArchiveServiceImpl(ArchiveDao archiveDao, BundleGradingDao bundleGradingDao, BundleSubmissionDao bundleSubmissionDao, ContainerProblemScoreCacheDao containerProblemScoreCacheDao, ProblemSetDao problemSetDao, ProblemSetProblemDao problemSetProblemDao, ProgrammingGradingDao programmingGradingDao, ProgrammingSubmissionDao programmingSubmissionDao) {
        this.archiveDao = archiveDao;
        this.bundleGradingDao = bundleGradingDao;
        this.bundleSubmissionDao = bundleSubmissionDao;
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
        List<ArchiveModel> archiveModels = archiveDao.findSortedByFiltersEq("id", "asc", "", ImmutableMap.of(ArchiveModel_.parentJid, parentJid), 0, -1);

        return archiveModels.stream().map(m -> createArchiveWithParentAndSubArchivesFromModel(m)).collect(Collectors.toList());
    }

    @Override
    public List<ArchiveWithScore> getChildArchivesWithScore(String parentJid, String userJid) {
        List<Archive> directSubArchives = getChildArchives(parentJid);

        ImmutableList.Builder<ArchiveWithScore> directSubArchivesWithScore = ImmutableList.builder();
        for (Archive archive : directSubArchives) {
            ImmutableList.Builder<String> subArchiveJids = ImmutableList.builder();
            Stack<String> subArchivesJidStack = new Stack<>();
            subArchivesJidStack.push(archive.getJid());

            while (!subArchivesJidStack.empty()) {
                String subArchiveJid = subArchivesJidStack.pop();

                List<ArchiveModel> subArchiveModels = archiveDao.findSortedByFiltersEq("id", "asc", "", ImmutableMap.of(ArchiveModel_.parentJid, subArchiveJid), 0, -1);
                for (ArchiveModel subArchiveModel : subArchiveModels) {
                    subArchivesJidStack.push(subArchiveModel.jid);
                }

                subArchiveJids.add(subArchiveJid);
            }

            List<ProblemSetModel> problemSetModels = problemSetDao.findSortedByFiltersIn("id", "asc", "", ImmutableMap.of(ProblemSetModel_.archiveJid, subArchiveJids.build()), 0, -1);
            List<ProblemSetProblemModel> problemSetProblemModels = problemSetProblemDao.findSortedByFiltersIn("id", "asc", "", ImmutableMap.of(ProblemSetProblemModel_.problemSetJid, problemSetModels.stream().map(ca -> ca.jid).collect(Collectors.toList())), 0, -1);

            double totalScore = ProblemSetProblemServiceUtils.getUserTotalScoreFromProblemSetProblemModels(containerProblemScoreCacheDao, bundleSubmissionDao, bundleGradingDao, programmingSubmissionDao, programmingGradingDao, userJid, problemSetProblemModels);

            directSubArchivesWithScore.add(new ArchiveWithScore(archive, totalScore));
        }

        return directSubArchivesWithScore.build();
    }

    @Override
    public Archive findArchiveById(long archiveId) throws ArchiveNotFoundException {
        ArchiveModel intendedArchiveModel = archiveDao.findById(archiveId);

        if (intendedArchiveModel == null) {
            throw new ArchiveNotFoundException("Archive Not Found.");
        }

        return createArchiveWithParentAndSubArchivesFromModel(intendedArchiveModel);
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

    private Archive createArchiveWithParentAndSubArchivesFromModel(ArchiveModel intendedArchiveModel) {
        Stack<ArchiveModel> archiveModelStack = new Stack<>();
        archiveModelStack.push(intendedArchiveModel);
        while (!archiveModelStack.peek().parentJid.equals("")) {
            archiveModelStack.push(archiveDao.findByJid(archiveModelStack.peek().parentJid));
        }

        Archive parentArchive = null;
        Archive intendedArchive = null;
        while (!archiveModelStack.isEmpty()) {
            ArchiveModel currentArchiveModel = archiveModelStack.pop();

            if (currentArchiveModel.jid.equals(intendedArchiveModel.jid) && (intendedArchive == null)) {
                intendedArchive = ArchiveServiceUtils.createArchiveFromModel(currentArchiveModel, parentArchive);
            } else {
                Archive currentArchive = ArchiveServiceUtils.createArchiveFromModel(currentArchiveModel, parentArchive);

                if (parentArchive != null) {
                    parentArchive.getSubArchives().add(currentArchive);
                }
                parentArchive = currentArchive;
            }
        }

        Stack<Archive> archiveStack = new Stack<>();
        archiveStack.add(intendedArchive);

        while (!archiveStack.isEmpty()) {
            Archive currentArchive = archiveStack.pop();

            List<ArchiveModel> archiveModels = archiveDao.findSortedByFiltersEq("id", "asc", "", ImmutableMap.of(ArchiveModel_.parentJid, currentArchive.getJid()), 0, -1);
            for (ArchiveModel archiveModel : archiveModels) {
                Archive archive = ArchiveServiceUtils.createArchiveFromModel(archiveModel, currentArchive);
                currentArchive.getSubArchives().add(archive);
                archiveStack.push(archive);
            }
        }

        return intendedArchive;
    }
}
