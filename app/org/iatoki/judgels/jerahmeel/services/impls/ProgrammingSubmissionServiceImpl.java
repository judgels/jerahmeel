package org.iatoki.judgels.jerahmeel.services.impls;

import org.iatoki.judgels.api.sealtiel.SealtielClientAPI;
import org.iatoki.judgels.gabriel.GradingResult;
import org.iatoki.judgels.jerahmeel.UserItemStatus;
import org.iatoki.judgels.jerahmeel.config.GabrielClientJid;
import org.iatoki.judgels.jerahmeel.models.daos.ProgrammingGradingDao;
import org.iatoki.judgels.jerahmeel.models.daos.SessionProblemDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProgrammingSubmissionDao;
import org.iatoki.judgels.jerahmeel.models.daos.UserItemDao;
import org.iatoki.judgels.jerahmeel.models.entities.ProgrammingGradingModel;
import org.iatoki.judgels.jerahmeel.models.entities.SessionProblemModel;
import org.iatoki.judgels.jerahmeel.models.entities.ProgrammingSubmissionModel;
import org.iatoki.judgels.jerahmeel.models.entities.UserItemModel;
import org.iatoki.judgels.sandalphon.services.ProgrammingSubmissionService;
import org.iatoki.judgels.sandalphon.services.impls.AbstractProgrammingSubmissionServiceImpl;
import org.iatoki.judgels.sandalphon.ProgrammingSubmission;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;

@Singleton
@Named("programmingSubmissionService")
public final class ProgrammingSubmissionServiceImpl extends AbstractProgrammingSubmissionServiceImpl<ProgrammingSubmissionModel, ProgrammingGradingModel> implements ProgrammingSubmissionService {

    private final ProgrammingSubmissionDao programmingSubmissionDao;
    private final ProgrammingGradingDao programmingGradingDao;
    private final SessionProblemDao sessionProblemDao;
    private final UserItemDao userItemDao;

    @Inject
    public ProgrammingSubmissionServiceImpl(ProgrammingSubmissionDao programmingSubmissionDao, ProgrammingGradingDao programmingGradingDao, SealtielClientAPI sealtielAPI, @GabrielClientJid String gabrielClientJid, SessionProblemDao sessionProblemDao, UserItemDao userItemDao) {
        super(programmingSubmissionDao, programmingGradingDao, sealtielAPI, gabrielClientJid);
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
        String sessionJid = programmingSubmissionModel.containerJid;
        String problemJid = programmingSubmissionModel.problemJid;
        List<ProgrammingSubmission> submissionList = this.getProgrammingSubmissionsWithGradingsByContainerJidAndProblemJidAndUserJid(sessionJid, problemJid, userJid);
        boolean completed = false;
        int i = 0;
        while ((!completed) && (i < submissionList.size())) {
            if (submissionList.get(i).getLatestScore() == 100) {
                completed = true;
            }
            ++i;
        }

        UserItemModel userItemModel = userItemDao.findByUserJidAndItemJid(userJid, problemJid);
        if (completed) {
            userItemModel.status = UserItemStatus.COMPLETED.name();
        } else {
            userItemModel.status = UserItemStatus.VIEWED.name();
        }
        userItemDao.edit(userItemModel, userJid, userItemModel.ipUpdate);

        userItemDao.flush();

        completed = true;
        List<SessionProblemModel> sessionProblemModels = sessionProblemDao.getBySessionJid(sessionJid);
        for (SessionProblemModel sessionProblemModel : sessionProblemModels) {
            if (!userItemDao.existsByUserJidItemJidAndStatus(userJid, sessionProblemModel.problemJid, UserItemStatus.COMPLETED.name())) {
                completed = false;
                break;
            }
        }
        userItemModel = userItemDao.findByUserJidAndItemJid(userJid, sessionJid);
        if (completed) {
            userItemModel.status = UserItemStatus.COMPLETED.name();
        } else {
            userItemModel.status = UserItemStatus.VIEWED.name();
        }
        userItemDao.edit(userItemModel, userJid, userItemModel.ipUpdate);
    }
}
