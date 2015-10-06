package org.iatoki.judgels.jerahmeel.services.impls;

import org.iatoki.judgels.jerahmeel.UserItemStatus;
import org.iatoki.judgels.jerahmeel.models.daos.BundleGradingDao;
import org.iatoki.judgels.jerahmeel.models.daos.BundleSubmissionDao;
import org.iatoki.judgels.jerahmeel.models.daos.ContainerProblemScoreCacheDao;
import org.iatoki.judgels.jerahmeel.models.daos.SessionProblemDao;
import org.iatoki.judgels.jerahmeel.models.daos.UserItemDao;
import org.iatoki.judgels.jerahmeel.models.entities.BundleGradingModel;
import org.iatoki.judgels.jerahmeel.models.entities.BundleSubmissionModel;
import org.iatoki.judgels.jerahmeel.models.entities.ContainerProblemScoreCacheModel;
import org.iatoki.judgels.jerahmeel.models.entities.SessionProblemModel;
import org.iatoki.judgels.jerahmeel.models.entities.UserItemModel;
import org.iatoki.judgels.sandalphon.BundleAnswer;
import org.iatoki.judgels.sandalphon.BundleSubmission;
import org.iatoki.judgels.sandalphon.services.BundleProblemGrader;
import org.iatoki.judgels.sandalphon.services.BundleSubmissionService;
import org.iatoki.judgels.sandalphon.services.impls.AbstractBundleSubmissionServiceImpl;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;

@Singleton
@Named("bundleSubmissionService")
public final class BundleSubmissionServiceImpl extends AbstractBundleSubmissionServiceImpl<BundleSubmissionModel, BundleGradingModel> implements BundleSubmissionService {

    private final BundleSubmissionDao submissionDao;
    private final BundleGradingDao gradingDao;
    private final ContainerProblemScoreCacheDao containerProblemScoreCacheDao;
    private final SessionProblemDao sessionProblemDao;
    private final UserItemDao userItemDao;

    @Inject
    public BundleSubmissionServiceImpl(BundleSubmissionDao submissionDao, BundleGradingDao gradingDao, ContainerProblemScoreCacheDao containerProblemScoreCacheDao, BundleProblemGrader bundleProblemGrader, SessionProblemDao sessionProblemDao, UserItemDao userItemDao) {
        super(submissionDao, gradingDao, bundleProblemGrader);
        this.submissionDao = submissionDao;
        this.gradingDao = gradingDao;
        this.containerProblemScoreCacheDao = containerProblemScoreCacheDao;
        this.sessionProblemDao = sessionProblemDao;
        this.userItemDao = userItemDao;
    }

    @Override
    public void afterGrade(String gradingJid, BundleAnswer answer) {
        BundleGradingModel gradingModel = gradingDao.findByJid(gradingJid);
        BundleSubmissionModel submissionModel = submissionDao.findByJid(gradingModel.submissionJid);

        String userJid = submissionModel.userCreate;
        String containerJid = submissionModel.containerJid;
        String problemJid = submissionModel.problemJid;

        List<BundleSubmission> submissionList = this.getBundleSubmissionsWithGradingsByContainerJidAndProblemJidAndUserJid(containerJid, problemJid, userJid);

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
        for (int i = 0; (!completed) && (i < submissionList.size()); ++i) {
            if (Double.compare(submissionList.get(i).getLatestScore(), 100) == 0) {
                completed = true;
            }
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
        List<SessionProblemModel> sessionProblemModels = sessionProblemDao.getBySessionJid(containerJid);
        for (SessionProblemModel sessionProblemModel : sessionProblemModels) {
            if (!userItemDao.existsByUserJidItemJidAndStatus(userJid, sessionProblemModel.problemJid, UserItemStatus.COMPLETED.name())) {
                completed = false;
                break;
            }
        }
        userItemModel = userItemDao.findByUserJidAndItemJid(userJid, containerJid);
        if (completed) {
            userItemModel.status = UserItemStatus.COMPLETED.name();
        } else {
            userItemModel.status = UserItemStatus.VIEWED.name();
        }
        userItemDao.edit(userItemModel, userJid, userItemModel.ipUpdate);
    }
}
