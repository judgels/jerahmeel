package org.iatoki.judgels.jerahmeel.services.impls;

import org.iatoki.judgels.api.sealtiel.SealtielClientAPI;
import org.iatoki.judgels.gabriel.GradingResult;
import org.iatoki.judgels.jerahmeel.UserItemStatus;
import org.iatoki.judgels.jerahmeel.config.GabrielClientJid;
import org.iatoki.judgels.jerahmeel.models.daos.ContainerProblemScoreCacheDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProgrammingGradingDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProgrammingSubmissionDao;
import org.iatoki.judgels.jerahmeel.models.daos.SessionProblemDao;
import org.iatoki.judgels.jerahmeel.models.daos.UserItemDao;
import org.iatoki.judgels.jerahmeel.models.entities.ContainerProblemScoreCacheModel;
import org.iatoki.judgels.jerahmeel.models.entities.ProgrammingGradingModel;
import org.iatoki.judgels.jerahmeel.models.entities.ProgrammingSubmissionModel;
import org.iatoki.judgels.jerahmeel.models.entities.SessionProblemModel;
import org.iatoki.judgels.jerahmeel.models.entities.UserItemModel;
import org.iatoki.judgels.sandalphon.ProgrammingSubmission;
import org.iatoki.judgels.sandalphon.services.ProgrammingSubmissionService;
import org.iatoki.judgels.sandalphon.services.impls.AbstractProgrammingSubmissionServiceImpl;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;

@Singleton
@Named("programmingSubmissionService")
public final class ProgrammingSubmissionServiceImpl extends AbstractProgrammingSubmissionServiceImpl<ProgrammingSubmissionModel, ProgrammingGradingModel> implements ProgrammingSubmissionService {

    private final ContainerProblemScoreCacheDao containerProblemScoreCacheDao;
    private final ProgrammingSubmissionDao programmingSubmissionDao;
    private final ProgrammingGradingDao programmingGradingDao;
    private final SessionProblemDao sessionProblemDao;
    private final UserItemDao userItemDao;

    @Inject
    public ProgrammingSubmissionServiceImpl(ContainerProblemScoreCacheDao containerProblemScoreCacheDao, ProgrammingSubmissionDao programmingSubmissionDao, ProgrammingGradingDao programmingGradingDao, SealtielClientAPI sealtielClientAPI, @GabrielClientJid String gabrielClientJid, SessionProblemDao sessionProblemDao, UserItemDao userItemDao) {
        super(programmingSubmissionDao, programmingGradingDao, sealtielClientAPI, gabrielClientJid);
        this.containerProblemScoreCacheDao = containerProblemScoreCacheDao;
        this.programmingSubmissionDao = programmingSubmissionDao;
        this.programmingGradingDao = programmingGradingDao;
        this.sessionProblemDao = sessionProblemDao;
        this.userItemDao = userItemDao;
    }

    @Override
    public void afterGrade(String gradingJid, GradingResult result) {
        ProgrammingGradingModel gradingModel = programmingGradingDao.findByJid(gradingJid);
        ProgrammingSubmissionModel programmingSubmissionModel = programmingSubmissionDao.findByJid(gradingModel.submissionJid);

        String userJid = programmingSubmissionModel.userCreate;
        String containerJid = programmingSubmissionModel.containerJid;
        String problemJid = programmingSubmissionModel.problemJid;

        List<ProgrammingSubmission> submissionList = this.getProgrammingSubmissionsWithGradingsByContainerJidAndProblemJidAndUserJid(containerJid, problemJid, userJid);

        if (containerProblemScoreCacheDao.existsByUserJidContainerJidAndProblemJid(userJid, containerJid, problemJid)) {
            ContainerProblemScoreCacheModel containerProblemScoreCacheModel = containerProblemScoreCacheDao.getByUserJidContainerJidAndProblemJid(userJid, containerJid, problemJid);
            double maxScore = 0;
            for (int i = 0; i < submissionList.size(); ++i) {
                if (submissionList.get(i).getLatestScore() > maxScore) {
                    maxScore = submissionList.get(i).getLatestScore();
                }
            }
            containerProblemScoreCacheModel.score = maxScore;
            containerProblemScoreCacheDao.edit(containerProblemScoreCacheModel, "cacheUpdater", "localhost");
        }

        if (!containerJid.startsWith("JIDSESS")) {
            return;
        }

        boolean completed = false;
        int i = 0;
        while ((!completed) && (i < submissionList.size())) {
            if (submissionList.get(i).getLatestScore() == 100) {
                completed = true;
            }
            ++i;
        }

        if (userItemDao.existsByUserJidAndItemJid(userJid, problemJid)) {
            UserItemModel userItemModel = userItemDao.findByUserJidAndItemJid(userJid, problemJid);
            if (completed) {
                userItemModel.status = UserItemStatus.COMPLETED.name();
            } else {
                userItemModel.status = UserItemStatus.VIEWED.name();
            }
            userItemDao.edit(userItemModel, userJid, userItemModel.ipUpdate);
        } else {
            UserItemModel userItemModel = new UserItemModel();
            userItemModel.userJid = userJid;
            userItemModel.itemJid = problemJid;
            if (completed) {
                userItemModel.status = UserItemStatus.COMPLETED.name();
            } else {
                userItemModel.status = UserItemStatus.VIEWED.name();
            }
            userItemDao.persist(userItemModel, userJid, "localhost");
        }

        userItemDao.flush();

        completed = true;
        List<SessionProblemModel> sessionProblemModels = sessionProblemDao.getBySessionJid(containerJid);
        for (SessionProblemModel sessionProblemModel : sessionProblemModels) {
            if (!userItemDao.existsByUserJidItemJidAndStatus(userJid, sessionProblemModel.problemJid, UserItemStatus.COMPLETED.name())) {
                completed = false;
                break;
            }
        }

        if (userItemDao.existsByUserJidAndItemJid(userJid, containerJid)) {
            UserItemModel userItemModel = userItemDao.findByUserJidAndItemJid(userJid, containerJid);
            if (completed) {
                userItemModel.status = UserItemStatus.COMPLETED.name();
            } else {
                userItemModel.status = UserItemStatus.VIEWED.name();
            }
            userItemDao.edit(userItemModel, userJid, userItemModel.ipUpdate);
        } else {
            UserItemModel userItemModel = new UserItemModel();
            userItemModel.userJid = userJid;
            userItemModel.itemJid = containerJid;
            if (completed) {
                userItemModel.status = UserItemStatus.COMPLETED.name();
            } else {
                userItemModel.status = UserItemStatus.VIEWED.name();
            }
            userItemDao.persist(userItemModel, userJid, "localhost");
        }
    }
}
