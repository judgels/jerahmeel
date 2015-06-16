package org.iatoki.judgels.jerahmeel;

import org.iatoki.judgels.jerahmeel.models.daos.interfaces.BundleGradingDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.BundleSubmissionDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.SessionProblemDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.UserItemDao;
import org.iatoki.judgels.jerahmeel.models.domains.BundleGradingModel;
import org.iatoki.judgels.jerahmeel.models.domains.BundleSubmissionModel;
import org.iatoki.judgels.jerahmeel.models.domains.SessionProblemModel;
import org.iatoki.judgels.jerahmeel.models.domains.UserItemModel;
import org.iatoki.judgels.sandalphon.commons.AbstractBundleSubmissionServiceImpl;
import org.iatoki.judgels.sandalphon.commons.BundleAnswer;
import org.iatoki.judgels.sandalphon.commons.BundleProblemGrader;
import org.iatoki.judgels.sandalphon.commons.BundleSubmission;

import java.util.List;

public final class BundleSubmissionServiceImpl extends AbstractBundleSubmissionServiceImpl<BundleSubmissionModel, BundleGradingModel> {

    private final BundleSubmissionDao submissionDao;
    private final BundleGradingDao gradingDao;
    private final SessionProblemDao sessionProblemDao;
    private final UserItemDao userItemDao;

    public BundleSubmissionServiceImpl(BundleSubmissionDao submissionDao, BundleGradingDao gradingDao, BundleProblemGrader bundleProblemGrader, SessionProblemDao sessionProblemDao, UserItemDao userItemDao) {
        super(submissionDao, gradingDao, bundleProblemGrader);
        this.submissionDao = submissionDao;
        this.gradingDao = gradingDao;
        this.sessionProblemDao = sessionProblemDao;
        this.userItemDao = userItemDao;
    }

    @Override
    public void afterGrade(String gradingJid, BundleAnswer answer) {
        BundleGradingModel gradingModel = gradingDao.findByJid(gradingJid);
        BundleSubmissionModel submissionModel = submissionDao.findByJid(gradingModel.submissionJid);

        String userJid = submissionModel.userCreate;
        String sessionJid = submissionModel.contestJid;
        String problemJid = submissionModel.problemJid;
        List<BundleSubmission> submissionList = this.findAllSubmissionsByContestJidAndProblemJid(sessionJid, problemJid);
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
        userItemDao.edit(userItemModel, userJid, userItemModel.ipUpdate);    }
}
