package org.iatoki.judgels.jerahmeel.services.impls;

import org.iatoki.judgels.gabriel.GradingResult;
import org.iatoki.judgels.jerahmeel.UserItemStatus;
import org.iatoki.judgels.jerahmeel.models.daos.GradingDao;
import org.iatoki.judgels.jerahmeel.models.daos.SessionProblemDao;
import org.iatoki.judgels.jerahmeel.models.daos.SubmissionDao;
import org.iatoki.judgels.jerahmeel.models.daos.UserItemDao;
import org.iatoki.judgels.jerahmeel.models.entities.GradingModel;
import org.iatoki.judgels.jerahmeel.models.entities.SessionProblemModel;
import org.iatoki.judgels.jerahmeel.models.entities.SubmissionModel;
import org.iatoki.judgels.jerahmeel.models.entities.UserItemModel;
import org.iatoki.judgels.sandalphon.commons.AbstractSubmissionServiceImpl;
import org.iatoki.judgels.sandalphon.commons.Submission;
import org.iatoki.judgels.sealtiel.Sealtiel;

import java.util.List;

public final class SubmissionServiceImpl extends AbstractSubmissionServiceImpl<SubmissionModel, GradingModel> {

    private final SubmissionDao submissionDao;
    private final GradingDao gradingDao;
    private final SessionProblemDao sessionProblemDao;
    private final UserItemDao userItemDao;

    public SubmissionServiceImpl(SubmissionDao submissionDao, GradingDao gradingDao, Sealtiel sealtiel, String gabrielClientJid, SessionProblemDao sessionProblemDao, UserItemDao userItemDao) {
        super(submissionDao, gradingDao, sealtiel, gabrielClientJid);
        this.submissionDao = submissionDao;
        this.gradingDao = gradingDao;
        this.sessionProblemDao = sessionProblemDao;
        this.userItemDao = userItemDao;
    }

    @Override
    public void afterGrade(String gradingJid, GradingResult result) {
        GradingModel gradingModel = gradingDao.findByJid(gradingJid);
        SubmissionModel submissionModel = submissionDao.findByJid(gradingModel.submissionJid);

        String userJid = submissionModel.userCreate;
        String sessionJid = submissionModel.contestJid;
        String problemJid = submissionModel.problemJid;
        List<Submission> submissionList = this.findAllSubmissionsByContestJid(sessionJid);
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

        completed = true;
        List<SessionProblemModel> sessionProblemModels = sessionProblemDao.findBySessionJid(sessionJid);
        for (SessionProblemModel sessionProblemModel : sessionProblemModels) {
            if (!userItemDao.existByUserJidItemJidAndStatus(userJid, sessionProblemModel.problemJid, UserItemStatus.COMPLETED.name())) {
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
