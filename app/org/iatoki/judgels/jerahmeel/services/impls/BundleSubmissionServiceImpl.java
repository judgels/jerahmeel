package org.iatoki.judgels.jerahmeel.services.impls;

import org.iatoki.judgels.jerahmeel.models.daos.ArchiveDao;
import org.iatoki.judgels.jerahmeel.models.daos.BundleGradingDao;
import org.iatoki.judgels.jerahmeel.models.daos.BundleSubmissionDao;
import org.iatoki.judgels.jerahmeel.models.daos.ContainerProblemScoreCacheDao;
import org.iatoki.judgels.jerahmeel.models.daos.ContainerScoreCacheDao;
import org.iatoki.judgels.jerahmeel.models.daos.CourseSessionDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProblemSetDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProblemSetProblemDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProgrammingGradingDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProgrammingSubmissionDao;
import org.iatoki.judgels.jerahmeel.models.daos.SessionProblemDao;
import org.iatoki.judgels.jerahmeel.models.daos.UserItemDao;
import org.iatoki.judgels.jerahmeel.models.entities.BundleGradingModel;
import org.iatoki.judgels.jerahmeel.models.entities.BundleSubmissionModel;
import org.iatoki.judgels.sandalphon.BundleAnswer;
import org.iatoki.judgels.sandalphon.BundleSubmission;
import org.iatoki.judgels.sandalphon.services.BundleProblemGrader;
import org.iatoki.judgels.sandalphon.services.BundleSubmissionService;
import org.iatoki.judgels.sandalphon.services.impls.AbstractBundleSubmissionServiceImpl;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
@Named("bundleSubmissionService")
public final class BundleSubmissionServiceImpl extends AbstractBundleSubmissionServiceImpl<BundleSubmissionModel, BundleGradingModel> implements BundleSubmissionService {

    private final ArchiveDao archiveDao;
    private final BundleSubmissionDao bundleSubmissionDao;
    private final BundleGradingDao bundleGradingDao;
    private final ContainerScoreCacheDao containerScoreCacheDao;
    private final ContainerProblemScoreCacheDao containerProblemScoreCacheDao;
    private final CourseSessionDao courseSessionDao;
    private final ProblemSetDao problemSetDao;
    private final ProblemSetProblemDao problemSetProblemDao;
    private final ProgrammingGradingDao programmingGradingDao;
    private final ProgrammingSubmissionDao programmingSubmissionDao;
    private final SessionProblemDao sessionProblemDao;
    private final UserItemDao userItemDao;

    @Inject
    public BundleSubmissionServiceImpl(ArchiveDao archiveDao, BundleSubmissionDao bundleSubmissionDao, BundleGradingDao bundleGradingDao, BundleProblemGrader bundleProblemGrader, ContainerScoreCacheDao containerScoreCacheDao, ContainerProblemScoreCacheDao containerProblemScoreCacheDao, CourseSessionDao courseSessionDao, ProblemSetDao problemSetDao, ProblemSetProblemDao problemSetProblemDao, ProgrammingGradingDao programmingGradingDao, ProgrammingSubmissionDao programmingSubmissionDao, SessionProblemDao sessionProblemDao, UserItemDao userItemDao) {
        super(bundleSubmissionDao, bundleGradingDao, bundleProblemGrader);
        this.archiveDao = archiveDao;
        this.bundleSubmissionDao = bundleSubmissionDao;
        this.bundleGradingDao = bundleGradingDao;
        this.containerScoreCacheDao = containerScoreCacheDao;
        this.containerProblemScoreCacheDao = containerProblemScoreCacheDao;
        this.courseSessionDao = courseSessionDao;
        this.problemSetDao = problemSetDao;
        this.problemSetProblemDao = problemSetProblemDao;
        this.programmingGradingDao = programmingGradingDao;
        this.programmingSubmissionDao = programmingSubmissionDao;
        this.sessionProblemDao = sessionProblemDao;
        this.userItemDao = userItemDao;
    }

    @Override
    public void afterGrade(String gradingJid, BundleAnswer answer) {
        BundleGradingModel bundleGradingModel = bundleGradingDao.findByJid(gradingJid);
        BundleSubmissionModel bundleSubmissionModel = bundleSubmissionDao.findByJid(bundleGradingModel.submissionJid);

        String userJid = bundleSubmissionModel.userCreate;
        String containerJid = bundleSubmissionModel.containerJid;
        String problemJid = bundleSubmissionModel.problemJid;

        BundleSubmission gradedBundleSubmission = findBundleSubmissionByJid(bundleSubmissionModel.jid);

        List<BundleSubmission> submissions = this.getBundleSubmissionsWithGradingsByContainerJidAndProblemJidAndUserJid(containerJid, problemJid, userJid);
        List<BundleSubmission> submissionsWithoutCurrent = submissions.stream().filter(s -> s.getJid() != bundleSubmissionModel.jid).collect(Collectors.toList());

        double newScore = JerahmeelSubmissionServiceUtils.countBundleSubmissionsMaxScore(submissions);
        if (containerProblemScoreCacheDao.existsByUserJidContainerJidAndProblemJid(userJid, containerJid, problemJid)) {
            JerahmeelSubmissionServiceUtils.updateContainerProblemScoreCache(containerProblemScoreCacheDao, userJid, containerJid, problemJid, newScore);
        } else {
            JerahmeelSubmissionServiceUtils.createContainerProblemScoreCache(containerProblemScoreCacheDao, userJid, containerJid, problemJid, newScore);
        }

        double previousScore = JerahmeelSubmissionServiceUtils.countBundleSubmissionsMaxScore(submissionsWithoutCurrent);
        if (gradedBundleSubmission.getGradings().size() > 1) {
            previousScore = Math.max(previousScore, gradedBundleSubmission.getGradings().get(gradedBundleSubmission.getGradings().size() - 2).getScore());
        }
        boolean scoreChanged = previousScore != newScore;

        if (containerJid.startsWith(JerahmeelSubmissionServiceUtils.PROBLEM_SET_JID_PREFIX)) {
            if (scoreChanged) {
                JerahmeelSubmissionServiceUtils.updateProblemSetAndParentsScoreCache(containerScoreCacheDao, containerProblemScoreCacheDao, archiveDao, problemSetDao, problemSetProblemDao, bundleSubmissionDao, bundleGradingDao, programmingSubmissionDao, programmingGradingDao, userJid, containerJid, previousScore, newScore);
            }
            return;
        }

        if (scoreChanged) {
            JerahmeelSubmissionServiceUtils.updateSessionAndParentsScoreCache(containerScoreCacheDao, containerProblemScoreCacheDao, courseSessionDao, sessionProblemDao, bundleSubmissionDao, bundleGradingDao, programmingSubmissionDao, programmingGradingDao, userJid, containerJid, previousScore, newScore);
        }

        JerahmeelSubmissionServiceUtils.updateSessionProblemProgressWithBundleSubmissions(userItemDao, sessionProblemDao, userJid, containerJid, problemJid, submissions);
    }
}
