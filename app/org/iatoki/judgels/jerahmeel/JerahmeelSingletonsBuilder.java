package org.iatoki.judgels.jerahmeel;

import org.iatoki.judgels.api.jophiel.JophielClientAPI;
import org.iatoki.judgels.api.jophiel.JophielPublicAPI;
import org.iatoki.judgels.jerahmeel.models.daos.ArchiveDao;
import org.iatoki.judgels.jerahmeel.models.daos.BundleGradingDao;
import org.iatoki.judgels.jerahmeel.models.daos.BundleSubmissionDao;
import org.iatoki.judgels.jerahmeel.models.daos.ContainerProblemScoreCacheDao;
import org.iatoki.judgels.jerahmeel.models.daos.ContainerScoreCacheDao;
import org.iatoki.judgels.jerahmeel.models.daos.CourseChapterDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProblemSetDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProblemSetProblemDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProgrammingGradingDao;
import org.iatoki.judgels.jerahmeel.models.daos.ProgrammingSubmissionDao;
import org.iatoki.judgels.jerahmeel.models.daos.ChapterProblemDao;
import org.iatoki.judgels.jerahmeel.models.daos.UserItemDao;
import org.iatoki.judgels.jerahmeel.services.PointStatisticService;
import org.iatoki.judgels.jerahmeel.services.ProblemScoreStatisticService;
import org.iatoki.judgels.jerahmeel.services.ProblemStatisticService;
import org.iatoki.judgels.jerahmeel.services.impls.ProblemSetScoreCacheUtils;
import org.iatoki.judgels.jerahmeel.services.impls.ChapterProgressCacheUtils;
import org.iatoki.judgels.jerahmeel.services.impls.ChapterScoreCacheUtils;
import org.iatoki.judgels.jophiel.controllers.JophielClientControllerUtils;
import org.iatoki.judgels.jophiel.services.impls.UserActivityMessageServiceImpl;
import org.iatoki.judgels.jerahmeel.controllers.JerahmeelControllerUtils;
import org.iatoki.judgels.jerahmeel.models.daos.ActivityLogDao;
import org.iatoki.judgels.jerahmeel.models.daos.AvatarCacheDao;
import org.iatoki.judgels.jerahmeel.models.daos.JidCacheDao;
import org.iatoki.judgels.jerahmeel.services.impls.ActivityLogServiceImpl;
import org.iatoki.judgels.jerahmeel.services.impls.AvatarCacheServiceImpl;
import org.iatoki.judgels.jerahmeel.services.impls.JidCacheServiceImpl;
import org.iatoki.judgels.sandalphon.services.BundleSubmissionService;
import org.iatoki.judgels.sandalphon.services.ProgrammingSubmissionService;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @deprecated Temporary class. Will be restructured when new module system has been finalized.
 */
@Singleton
@Deprecated
public final class JerahmeelSingletonsBuilder {

    @Inject
    public JerahmeelSingletonsBuilder(
            JidCacheDao jidCacheDao, AvatarCacheDao avatarCacheDao, ActivityLogDao activityLogDao,
            JophielClientAPI jophielClientAPI, JophielPublicAPI jophielPublicAPI,
            BundleSubmissionService bundleSubmissionService, PointStatisticService pointStatisticService, ProblemScoreStatisticService problemScoreStatisticService, ProblemStatisticService problemStatisticService, ProgrammingSubmissionService programmingSubmissionService,
            ChapterProblemDao chapterProblemDao, UserItemDao userItemDao,
            ArchiveDao archiveDao, BundleSubmissionDao bundleSubmissionDao, BundleGradingDao bundleGradingDao, ContainerScoreCacheDao containerScoreCacheDao, ContainerProblemScoreCacheDao containerProblemScoreCacheDao, ProblemSetDao problemSetDao, ProblemSetProblemDao problemSetProblemDao, ProgrammingSubmissionDao programmingSubmissionDao, ProgrammingGradingDao programmingGradingDao,
            CourseChapterDao courseChapterDao) {

        JidCacheServiceImpl.buildInstance(jidCacheDao);
        AvatarCacheServiceImpl.buildInstance(avatarCacheDao);
        ActivityLogServiceImpl.buildInstance(activityLogDao);
        UserActivityMessageServiceImpl.buildInstance();

        JophielClientControllerUtils.buildInstance(JerahmeelProperties.getInstance().getJophielBaseUrl());
        JerahmeelControllerUtils.buildInstance(jophielClientAPI, jophielPublicAPI, bundleSubmissionService, pointStatisticService, problemScoreStatisticService, problemStatisticService, programmingSubmissionService);
        ChapterProgressCacheUtils.buildInstance(chapterProblemDao, userItemDao);
        ProblemSetScoreCacheUtils.buildInstance(archiveDao, bundleSubmissionDao, bundleGradingDao, containerScoreCacheDao, containerProblemScoreCacheDao, problemSetDao, problemSetProblemDao, programmingSubmissionDao, programmingGradingDao);
        ChapterScoreCacheUtils.buildInstance(bundleSubmissionDao, bundleGradingDao, containerScoreCacheDao, containerProblemScoreCacheDao, courseChapterDao, programmingSubmissionDao, programmingGradingDao, chapterProblemDao);
    }
}
