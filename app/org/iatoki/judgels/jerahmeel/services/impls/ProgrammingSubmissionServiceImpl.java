package org.iatoki.judgels.jerahmeel.services.impls;

import org.iatoki.judgels.api.sealtiel.SealtielClientAPI;
import org.iatoki.judgels.gabriel.GradingResult;
import org.iatoki.judgels.jerahmeel.config.GabrielClientJid;
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
import org.iatoki.judgels.jerahmeel.models.entities.ProgrammingGradingModel;
import org.iatoki.judgels.jerahmeel.models.entities.ProgrammingSubmissionModel;
import org.iatoki.judgels.sandalphon.ProgrammingSubmission;
import org.iatoki.judgels.sandalphon.services.ProgrammingSubmissionService;
import org.iatoki.judgels.sandalphon.services.impls.AbstractProgrammingSubmissionServiceImpl;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
@Named("programmingSubmissionService")
public final class ProgrammingSubmissionServiceImpl extends AbstractProgrammingSubmissionServiceImpl<ProgrammingSubmissionModel, ProgrammingGradingModel> implements ProgrammingSubmissionService {

    private final ArchiveDao archiveDao;
    private final BundleSubmissionDao bundleSubmissionDao;
    private final BundleGradingDao bundleGradingDao;
    private final ContainerScoreCacheDao containerScoreCacheDao;
    private final ContainerProblemScoreCacheDao containerProblemScoreCacheDao;
    private final CourseSessionDao courseSessionDao;
    private final ProblemSetDao problemSetDao;
    private final ProblemSetProblemDao problemSetProblemDao;
    private final ProgrammingSubmissionDao programmingSubmissionDao;
    private final ProgrammingGradingDao programmingGradingDao;
    private final SessionProblemDao sessionProblemDao;
    private final UserItemDao userItemDao;

    @Inject
    public ProgrammingSubmissionServiceImpl(ArchiveDao archiveDao, BundleSubmissionDao bundleSubmissionDao, BundleGradingDao bundleGradingDao, ContainerScoreCacheDao containerScoreCacheDao, ContainerProblemScoreCacheDao containerProblemScoreCacheDao, CourseSessionDao courseSessionDao, ProblemSetDao problemSetDao, ProblemSetProblemDao problemSetProblemDao, ProgrammingSubmissionDao programmingSubmissionDao, ProgrammingGradingDao programmingGradingDao, SealtielClientAPI sealtielClientAPI, @GabrielClientJid String gabrielClientJid, SessionProblemDao sessionProblemDao, UserItemDao userItemDao) {
        super(programmingSubmissionDao, programmingGradingDao, sealtielClientAPI, gabrielClientJid);
        this.archiveDao = archiveDao;
        this.bundleSubmissionDao = bundleSubmissionDao;
        this.bundleGradingDao = bundleGradingDao;
        this.containerScoreCacheDao = containerScoreCacheDao;
        this.containerProblemScoreCacheDao = containerProblemScoreCacheDao;
        this.courseSessionDao = courseSessionDao;
        this.problemSetDao = problemSetDao;
        this.problemSetProblemDao = problemSetProblemDao;
        this.programmingSubmissionDao = programmingSubmissionDao;
        this.programmingGradingDao = programmingGradingDao;
        this.sessionProblemDao = sessionProblemDao;
        this.userItemDao = userItemDao;
    }

    @Override
    public void afterGrade(String gradingJid, GradingResult result) {
        ProgrammingGradingModel programmingGradingModel = programmingGradingDao.findByJid(gradingJid);
        ProgrammingSubmissionModel programmingSubmissionModel = programmingSubmissionDao.findByJid(programmingGradingModel.submissionJid);

        String userJid = programmingSubmissionModel.userCreate;
        String containerJid = programmingSubmissionModel.containerJid;
        String problemJid = programmingSubmissionModel.problemJid;

        ProgrammingSubmission gradedProgrammingSubmission = this.findProgrammingSubmissionByJid(programmingSubmissionModel.jid);

        List<ProgrammingSubmission> submissions = this.getProgrammingSubmissionsWithGradingsByContainerJidAndProblemJidAndUserJid(containerJid, problemJid, userJid);
        List<ProgrammingSubmission> submissionsWithoutCurrent = submissions.stream().filter(s -> s.getJid() != programmingSubmissionModel.jid).collect(Collectors.toList());

        double newScore = JerahmeelSubmissionServiceUtils.countProgrammingSubmissionsMaxScore(submissions);
        if (containerProblemScoreCacheDao.existsByUserJidContainerJidAndProblemJid(userJid, containerJid, problemJid)) {
            JerahmeelSubmissionServiceUtils.updateContainerProblemScoreCache(containerProblemScoreCacheDao, userJid, containerJid, problemJid, newScore);
        } else {
            JerahmeelSubmissionServiceUtils.createContainerProblemScoreCache(containerProblemScoreCacheDao, userJid, containerJid, problemJid, newScore);
        }

        double previousScore = JerahmeelSubmissionServiceUtils.countProgrammingSubmissionsMaxScore(submissionsWithoutCurrent);
        if (gradedProgrammingSubmission.getGradings().size() > 1) {
            previousScore = Math.max(previousScore, gradedProgrammingSubmission.getGradings().get(gradedProgrammingSubmission.getGradings().size() - 2).getScore());
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

        JerahmeelSubmissionServiceUtils.updateSessionProblemProgressWithProgrammingSubmissions(userItemDao, sessionProblemDao, userJid, containerJid, problemJid, submissions);
    }
}
