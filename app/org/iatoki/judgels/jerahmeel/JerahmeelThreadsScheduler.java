package org.iatoki.judgels.jerahmeel;

import akka.actor.ActorSystem;
import akka.actor.Scheduler;
import org.iatoki.judgels.api.jophiel.JophielClientAPI;
import org.iatoki.judgels.api.sealtiel.SealtielClientAPI;
import org.iatoki.judgels.jerahmeel.runnables.StatisticUpdater;
import org.iatoki.judgels.jerahmeel.services.PointStatisticService;
import org.iatoki.judgels.jerahmeel.services.ProblemScoreStatisticService;
import org.iatoki.judgels.jerahmeel.services.ProblemStatisticService;
import org.iatoki.judgels.jophiel.runnables.UserActivityMessagePusher;
import org.iatoki.judgels.jophiel.services.impls.UserActivityMessageServiceImpl;
import org.iatoki.judgels.sandalphon.runnables.GradingResponsePoller;
import org.iatoki.judgels.sandalphon.services.BundleSubmissionService;
import org.iatoki.judgels.sandalphon.services.ProgrammingSubmissionService;
import play.db.jpa.JPAApi;
import scala.concurrent.ExecutionContextExecutor;
import scala.concurrent.duration.Duration;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

/**
 * @deprecated Temporary class. Will be restructured when new module system has been finalized.
 */
@Singleton
@Deprecated
public final class JerahmeelThreadsScheduler {

    @Inject
    public JerahmeelThreadsScheduler(ActorSystem actorSystem, JPAApi jpaApi, ProgrammingSubmissionService programmingSubmissionService, SealtielClientAPI sealtielClientAPI, JophielClientAPI jophielClientAPI, BundleSubmissionService bundleSubmissionService, PointStatisticService pointStatisticService, ProblemScoreStatisticService problemScoreStatisticService, ProblemStatisticService problemStatisticService) {
        Scheduler scheduler = actorSystem.scheduler();
        ExecutionContextExecutor context = actorSystem.dispatcher();

        GradingResponsePoller poller = new GradingResponsePoller(scheduler, context, programmingSubmissionService, sealtielClientAPI, TimeUnit.MILLISECONDS.convert(2, TimeUnit.SECONDS));
        UserActivityMessagePusher userActivityMessagePusher = new UserActivityMessagePusher(jpaApi, jophielClientAPI, UserActivityMessageServiceImpl.getInstance());
        StatisticUpdater pointStatisticUpdater = new StatisticUpdater(bundleSubmissionService, pointStatisticService, problemScoreStatisticService, problemStatisticService, programmingSubmissionService);

        scheduler.schedule(Duration.create(1, TimeUnit.SECONDS), Duration.create(3, TimeUnit.SECONDS), poller, context);
        scheduler.schedule(Duration.create(1, TimeUnit.SECONDS), Duration.create(1, TimeUnit.MINUTES), userActivityMessagePusher, context);
        scheduler.schedule(Duration.create(10, TimeUnit.SECONDS), Duration.create(1, TimeUnit.HOURS), pointStatisticUpdater, context);
    }
}
