package org.iatoki.judgels.jerahmeel;

import akka.actor.Scheduler;
import org.iatoki.judgels.api.jophiel.JophielClientAPI;
import org.iatoki.judgels.api.jophiel.JophielPublicAPI;
import org.iatoki.judgels.api.sealtiel.SealtielClientAPI;
import org.iatoki.judgels.jerahmeel.controllers.JerahmeelControllerUtils;
import org.iatoki.judgels.jerahmeel.models.daos.ActivityLogDao;
import org.iatoki.judgels.jerahmeel.models.daos.AvatarCacheDao;
import org.iatoki.judgels.jerahmeel.models.daos.JidCacheDao;
import org.iatoki.judgels.jerahmeel.runnables.StatisticUpdater;
import org.iatoki.judgels.jerahmeel.services.PointStatisticService;
import org.iatoki.judgels.jerahmeel.services.ProblemScoreStatisticService;
import org.iatoki.judgels.jerahmeel.services.ProblemStatisticService;
import org.iatoki.judgels.jerahmeel.services.impls.ActivityLogServiceImpl;
import org.iatoki.judgels.jerahmeel.services.impls.AvatarCacheServiceImpl;
import org.iatoki.judgels.jerahmeel.services.impls.JerahmeelDataMigrationServiceImpl;
import org.iatoki.judgels.jerahmeel.services.impls.JidCacheServiceImpl;
import org.iatoki.judgels.jophiel.JophielAuthAPI;
import org.iatoki.judgels.jophiel.controllers.JophielClientControllerUtils;
import org.iatoki.judgels.jophiel.runnables.UserActivityMessagePusher;
import org.iatoki.judgels.jophiel.services.impls.UserActivityMessageServiceImpl;
import org.iatoki.judgels.play.AbstractGlobal;
import org.iatoki.judgels.play.services.BaseDataMigrationService;
import org.iatoki.judgels.sandalphon.runnables.GradingResponsePoller;
import org.iatoki.judgels.sandalphon.services.BundleSubmissionService;
import org.iatoki.judgels.sandalphon.services.ProgrammingSubmissionService;
import play.Application;
import play.inject.Injector;
import play.libs.Akka;
import scala.concurrent.ExecutionContextExecutor;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

public final class Global extends AbstractGlobal {

    @Override
    public void onStart(Application application) {
        super.onStart(application);

        buildServices(application.injector());
        buildUtils(application.injector());
        scheduleThreads(application.injector());
    }

    @Override
    protected BaseDataMigrationService getDataMigrationService() {
        return new JerahmeelDataMigrationServiceImpl();
    }

    private void buildServices(Injector injector) {
        JidCacheServiceImpl.buildInstance(injector.instanceOf(JidCacheDao.class));
        AvatarCacheServiceImpl.buildInstance(injector.instanceOf(JophielAuthAPI.class), injector.instanceOf(AvatarCacheDao.class));
        ActivityLogServiceImpl.buildInstance(injector.instanceOf(ActivityLogDao.class));
        UserActivityMessageServiceImpl.buildInstance();
    }

    private void buildUtils(Injector injector) {
        JophielClientControllerUtils.buildInstance(JerahmeelProperties.getInstance().getJophielBaseUrl());
        JerahmeelControllerUtils.buildInstance(injector.instanceOf(JophielClientAPI.class), injector.instanceOf(JophielPublicAPI.class), injector.instanceOf(BundleSubmissionService.class), injector.instanceOf(PointStatisticService.class), injector.instanceOf(ProblemScoreStatisticService.class), injector.instanceOf(ProblemStatisticService.class), injector.instanceOf(ProgrammingSubmissionService.class));
    }

    private void scheduleThreads(Injector injector) {
        Scheduler scheduler = Akka.system().scheduler();
        ExecutionContextExecutor context = Akka.system().dispatcher();

        GradingResponsePoller poller = new GradingResponsePoller(scheduler, context, injector.instanceOf(ProgrammingSubmissionService.class), injector.instanceOf(SealtielClientAPI.class), TimeUnit.MILLISECONDS.convert(2, TimeUnit.SECONDS));
        UserActivityMessagePusher userActivityMessagePusher = new UserActivityMessagePusher(injector.instanceOf(JophielClientAPI.class), UserActivityMessageServiceImpl.getInstance());
        StatisticUpdater pointStatisticUpdater = new StatisticUpdater(injector.instanceOf(BundleSubmissionService.class), injector.instanceOf(PointStatisticService.class), injector.instanceOf(ProblemScoreStatisticService.class), injector.instanceOf(ProblemStatisticService.class), injector.instanceOf(ProgrammingSubmissionService.class));

        scheduler.schedule(Duration.create(1, TimeUnit.SECONDS), Duration.create(3, TimeUnit.SECONDS), poller, context);
        scheduler.schedule(Duration.create(1, TimeUnit.SECONDS), Duration.create(1, TimeUnit.MINUTES), userActivityMessagePusher, context);
        scheduler.schedule(Duration.create(10, TimeUnit.SECONDS), Duration.create(1, TimeUnit.HOURS), pointStatisticUpdater, context);
    }
}
