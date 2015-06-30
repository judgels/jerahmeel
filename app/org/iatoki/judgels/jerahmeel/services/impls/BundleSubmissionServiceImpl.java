package org.iatoki.judgels.jerahmeel.services.impls;

import org.iatoki.judgels.jerahmeel.UserItemStatus;
import org.iatoki.judgels.jerahmeel.models.daos.BundleGradingDao;
import org.iatoki.judgels.jerahmeel.models.daos.BundleSubmissionDao;
import org.iatoki.judgels.jerahmeel.models.daos.SessionProblemDao;
import org.iatoki.judgels.jerahmeel.models.daos.UserItemDao;
import org.iatoki.judgels.jerahmeel.models.entities.BundleGradingModel;
import org.iatoki.judgels.jerahmeel.models.entities.BundleSubmissionModel;
import org.iatoki.judgels.jerahmeel.models.entities.SessionProblemModel;
import org.iatoki.judgels.jerahmeel.models.entities.UserItemModel;
import org.iatoki.judgels.sandalphon.services.impls.AbstractBundleSubmissionServiceImpl;
import org.iatoki.judgels.sandalphon.BundleAnswer;
import org.iatoki.judgels.sandalphon.services.BundleProblemGrader;
import org.iatoki.judgels.sandalphon.BundleSubmission;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;

@Singleton
@Named("bundleSubmissionService")
public final class BundleSubmissionServiceImpl extends AbstractBundleSubmissionServiceImpl<BundleSubmissionModel, BundleGradingModel> {

    private final BundleSubmissionDao submissionDao;
    private final BundleGradingDao gradingDao;
    private final SessionProblemDao sessionProblemDao;
    private final UserItemDao userItemDao;

    @Inject
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
        List<BundleSubmission> submissionList = this.findAllSubmissionsByContestJidProblemJidAndUserJid(sessionJid, problemJid, userJid);
        boolean completed = false;
        int i = 0;
        while ((!completed) && (i < submissionList.size())) {
            if (Double.compare(submissionList.get(i).getLatestScore(), 100) == 0) {
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
