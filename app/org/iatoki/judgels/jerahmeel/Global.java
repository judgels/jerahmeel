package org.iatoki.judgels.jerahmeel;

import akka.actor.Scheduler;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.common.collect.ImmutableMap;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.iatoki.judgels.commons.AWSFileSystemProvider;
import org.iatoki.judgels.commons.FileSystemProvider;
import org.iatoki.judgels.commons.JudgelsProperties;
import org.iatoki.judgels.commons.LocalFileSystemProvider;
import org.iatoki.judgels.jerahmeel.controllers.ControllerUtils;
import org.iatoki.judgels.jerahmeel.controllers.TrainingBundleSubmissionController;
import org.iatoki.judgels.jerahmeel.controllers.TrainingProgrammingSubmissionController;
import org.iatoki.judgels.jerahmeel.services.AvatarCacheService;
import org.iatoki.judgels.jerahmeel.services.CourseService;
import org.iatoki.judgels.jerahmeel.services.CourseSessionService;
import org.iatoki.judgels.jerahmeel.services.CurriculumService;
import org.iatoki.judgels.jerahmeel.services.JidCacheService;
import org.iatoki.judgels.jerahmeel.services.impls.BundleSubmissionServiceImpl;
import org.iatoki.judgels.jerahmeel.services.impls.CourseServiceImpl;
import org.iatoki.judgels.jerahmeel.services.impls.CourseSessionServiceImpl;
import org.iatoki.judgels.jerahmeel.services.CurriculumCourseService;
import org.iatoki.judgels.jerahmeel.services.impls.CurriculumCourseServiceImpl;
import org.iatoki.judgels.jerahmeel.services.SessionDependencyService;
import org.iatoki.judgels.jerahmeel.services.impls.CurriculumServiceImpl;
import org.iatoki.judgels.jerahmeel.services.impls.SessionDependencyServiceImpl;
import org.iatoki.judgels.jerahmeel.services.SessionLessonService;
import org.iatoki.judgels.jerahmeel.services.impls.SessionLessonServiceImpl;
import org.iatoki.judgels.jerahmeel.services.SessionProblemService;
import org.iatoki.judgels.jerahmeel.services.impls.SessionProblemServiceImpl;
import org.iatoki.judgels.jerahmeel.services.SessionService;
import org.iatoki.judgels.jerahmeel.services.impls.SessionServiceImpl;
import org.iatoki.judgels.jerahmeel.services.impls.SubmissionServiceImpl;
import org.iatoki.judgels.jerahmeel.services.impls.UserActivityMessageServiceImpl;
import org.iatoki.judgels.jerahmeel.services.UserItemService;
import org.iatoki.judgels.jerahmeel.services.impls.UserItemServiceImpl;
import org.iatoki.judgels.jerahmeel.services.UserService;
import org.iatoki.judgels.jerahmeel.services.impls.UserServiceImpl;
import org.iatoki.judgels.jophiel.UserActivityMessagePusher;
import org.iatoki.judgels.jophiel.services.impls.DefaultUserActivityMessageServiceImpl;
import org.iatoki.judgels.sandalphon.GradingResponsePoller;
import org.iatoki.judgels.sandalphon.services.SubmissionService;
import org.iatoki.judgels.jerahmeel.controllers.CourseController;
import org.iatoki.judgels.jerahmeel.controllers.CourseSessionController;
import org.iatoki.judgels.jerahmeel.controllers.CurriculumController;
import org.iatoki.judgels.jerahmeel.controllers.CurriculumCourseController;
import org.iatoki.judgels.jerahmeel.controllers.SessionBundleSubmissionController;
import org.iatoki.judgels.jerahmeel.controllers.SessionController;
import org.iatoki.judgels.jerahmeel.controllers.SessionLessonController;
import org.iatoki.judgels.jerahmeel.controllers.SessionProblemController;
import org.iatoki.judgels.jerahmeel.controllers.SessionDependencyController;
import org.iatoki.judgels.jerahmeel.controllers.SessionProgrammingSubmissionController;
import org.iatoki.judgels.jerahmeel.controllers.TrainingController;
import org.iatoki.judgels.jerahmeel.controllers.TrainingCourseController;
import org.iatoki.judgels.jerahmeel.controllers.TrainingCurriculumController;
import org.iatoki.judgels.jerahmeel.controllers.TrainingLessonController;
import org.iatoki.judgels.jerahmeel.controllers.TrainingProblemController;
import org.iatoki.judgels.jerahmeel.controllers.TrainingSessionController;
import org.iatoki.judgels.jerahmeel.controllers.apis.CourseAPIController;
import org.iatoki.judgels.jerahmeel.controllers.apis.SessionAPIController;
import org.iatoki.judgels.jerahmeel.models.daos.impls.BundleGradingHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.impls.BundleSubmissionHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.impls.CourseHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.impls.CourseSessionHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.impls.CurriculumCourseHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.impls.CurriculumHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.impls.SessionHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.impls.SessionLessonHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.impls.SessionProblemHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.impls.SessionDependencyHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.impls.UserItemHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.BundleGradingDao;
import org.iatoki.judgels.jerahmeel.models.daos.BundleSubmissionDao;
import org.iatoki.judgels.jerahmeel.models.daos.CourseDao;
import org.iatoki.judgels.jerahmeel.models.daos.CourseSessionDao;
import org.iatoki.judgels.jerahmeel.models.daos.CurriculumCourseDao;
import org.iatoki.judgels.jerahmeel.models.daos.CurriculumDao;
import org.iatoki.judgels.jerahmeel.models.daos.SessionDao;
import org.iatoki.judgels.jerahmeel.models.daos.SessionLessonDao;
import org.iatoki.judgels.jerahmeel.models.daos.SessionProblemDao;
import org.iatoki.judgels.jerahmeel.models.daos.SessionDependencyDao;
import org.iatoki.judgels.jerahmeel.models.daos.UserItemDao;
import org.iatoki.judgels.jophiel.Jophiel;
import org.iatoki.judgels.jophiel.controllers.JophielClientController;
import org.iatoki.judgels.sandalphon.Sandalphon;
import org.iatoki.judgels.sealtiel.Sealtiel;
import org.iatoki.judgels.jerahmeel.controllers.ApplicationController;
import org.iatoki.judgels.jerahmeel.controllers.UserController;
import org.iatoki.judgels.jerahmeel.models.daos.impls.AvatarCacheHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.impls.GradingHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.impls.JidCacheHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.impls.SubmissionHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.impls.UserHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.AvatarCacheDao;
import org.iatoki.judgels.jerahmeel.models.daos.GradingDao;
import org.iatoki.judgels.jerahmeel.models.daos.JidCacheDao;
import org.iatoki.judgels.jerahmeel.models.daos.SubmissionDao;
import org.iatoki.judgels.jerahmeel.models.daos.UserDao;
import play.Application;
import play.libs.Akka;
import play.mvc.Controller;
import scala.concurrent.ExecutionContextExecutor;
import scala.concurrent.duration.Duration;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class Global extends org.iatoki.judgels.commons.Global {
    private AvatarCacheDao avatarCacheDao;
    private BundleGradingDao bundleGradingDao;
    private BundleSubmissionDao bundleSubmissionDao;
    private CourseDao courseDao;
    private CourseSessionDao courseSessionDao;
    private CurriculumCourseDao curriculumCourseDao;
    private CurriculumDao curriculumDao;
    private GradingDao gradingDao;
    private JidCacheDao jidCacheDao;
    private SessionDao sessionDao;
    private SessionLessonDao sessionLessonDao;
    private SessionProblemDao sessionProblemDao;
    private SessionDependencyDao sessionDependencyDao;
    private SubmissionDao submissionDao;
    private UserDao userDao;
    private UserItemDao userItemDao;
    private Jophiel jophiel;
    private Sandalphon sandalphon;

    private JerahmeelProperties jerahmeelProps;

    private Sealtiel sealtiel;

    private FileSystemProvider submissionLocalFileProvider;
    private FileSystemProvider submissionRemoteFileProvider;

    private BundleSubmissionServiceImpl bundleSubmissionService;
    private CourseService courseService;
    private CourseSessionService courseSessionService;
    private CurriculumCourseService curriculumCourseService;
    private CurriculumService curriculumService;
    private SessionLessonService sessionLessonService;
    private SessionProblemService sessionProblemService;
    private SessionService sessionService;
    private SessionDependencyService sessionDependencyService;
    private SubmissionService submissionService;
    private UserItemService userItemService;
    private UserService userService;

    private Map<Class<?>, Controller> controllersRegistry;

    @Override
    public void onStart(Application application) {
        buildProperties();
        buildDaos();
        buildCommons();
        buildFileProviders();
        buildServices();
        buildUtils();
        buildControllers();
        scheduleThreads();
    }

    @Override
    public <A> A getControllerInstance(Class<A> controllerClass) throws Exception {
        return controllerClass.cast(controllersRegistry.get(controllerClass));
    }

    private void buildDaos() {
        avatarCacheDao = new AvatarCacheHibernateDao();
        bundleGradingDao = new BundleGradingHibernateDao();
        bundleSubmissionDao = new BundleSubmissionHibernateDao();
        courseDao = new CourseHibernateDao();
        courseSessionDao = new CourseSessionHibernateDao();
        curriculumCourseDao = new CurriculumCourseHibernateDao();
        curriculumDao = new CurriculumHibernateDao();
        gradingDao = new GradingHibernateDao();
        jidCacheDao = new JidCacheHibernateDao();
        sessionDao = new SessionHibernateDao();
        sessionLessonDao = new SessionLessonHibernateDao();
        sessionProblemDao = new SessionProblemHibernateDao();
        sessionDependencyDao = new SessionDependencyHibernateDao();
        submissionDao = new SubmissionHibernateDao();
        userDao = new UserHibernateDao();
        userItemDao = new UserItemHibernateDao();
    }

    private void buildProperties() {
        Config config = ConfigFactory.load();

        org.iatoki.judgels.jerahmeel.BuildInfo$ buildInfo = org.iatoki.judgels.jerahmeel.BuildInfo$.MODULE$;
        JudgelsProperties.buildInstance(buildInfo.name(), buildInfo.version(), config);

        JerahmeelProperties.buildInstance(config);
        jerahmeelProps = JerahmeelProperties.getInstance();
    }

    private void buildCommons() {
        jophiel = new Jophiel(jerahmeelProps.getJophielBaseUrl(), jerahmeelProps.getJophielClientJid(), jerahmeelProps.getJophielClientSecret());
        sandalphon = new Sandalphon(jerahmeelProps.getSandalphonBaseUrl(), jerahmeelProps.getSandalphonClientJid(), jerahmeelProps.getSandalphonClientSecret());
        sealtiel = new Sealtiel(jerahmeelProps.getSealtielBaseUrl(), jerahmeelProps.getSealtielClientJid(), jerahmeelProps.getSealtielClientSecret());
    }

    private void buildFileProviders() {
        if (jerahmeelProps.isSubmissionUsingAWSS3()) {
            AmazonS3Client awsS3Client;
            if (jerahmeelProps.isSubmissionAWSUsingKeys()) {
                awsS3Client = new AmazonS3Client(new BasicAWSCredentials(jerahmeelProps.getSubmissionAWSAccessKey(), jerahmeelProps.getSubmissionAWSSecretKey()));
            } else {
                awsS3Client = new AmazonS3Client();
            }
            submissionRemoteFileProvider = new AWSFileSystemProvider(awsS3Client, jerahmeelProps.getSubmissionAWSS3BucketName(), jerahmeelProps.getSubmissionAWSS3BucketRegion());
        }
        submissionLocalFileProvider = new LocalFileSystemProvider(jerahmeelProps.getSubmissionLocalDir());
    }

    private void buildServices() {
        bundleSubmissionService = new BundleSubmissionServiceImpl(bundleSubmissionDao, bundleGradingDao, sandalphon, sessionProblemDao, userItemDao);
        courseService = new CourseServiceImpl(courseDao);
        courseSessionService = new CourseSessionServiceImpl(courseSessionDao, sessionDao, sessionDependencyDao, userItemDao);
        curriculumCourseService = new CurriculumCourseServiceImpl(curriculumCourseDao, courseDao, courseSessionDao, sessionDependencyDao, userItemDao);
        curriculumService = new CurriculumServiceImpl(curriculumDao);
        sessionLessonService = new SessionLessonServiceImpl(sessionLessonDao, userItemDao);
        sessionProblemService = new SessionProblemServiceImpl(sessionProblemDao, userItemDao);
        sessionService = new SessionServiceImpl(sessionDao);
        sessionDependencyService = new SessionDependencyServiceImpl(sessionDao, sessionDependencyDao, userItemDao);
        submissionService = new SubmissionServiceImpl(submissionDao, gradingDao, sealtiel, jerahmeelProps.getSealtielGabrielClientJid(), sessionProblemDao, userItemDao);
        userService = new UserServiceImpl(jophiel, userDao);
        userItemService = new UserItemServiceImpl(userItemDao);

        JidCacheService.buildInstance(jidCacheDao);
        AvatarCacheService.buildInstance(jophiel, avatarCacheDao);
        ControllerUtils.buildInstance(jophiel);
        DefaultUserActivityMessageServiceImpl.buildInstance(jophiel);
    }

    private void buildUtils() {
    }

    private void buildControllers() {
        controllersRegistry = ImmutableMap.<Class<?>, Controller> builder()
                .put(ApplicationController.class, new ApplicationController(jophiel, userService))
                .put(CourseController.class, new CourseController(courseService))
                .put(CourseSessionController.class, new CourseSessionController(courseService, courseSessionService, sessionService))
                .put(CurriculumController.class, new CurriculumController(curriculumService))
                .put(CurriculumCourseController.class, new CurriculumCourseController(curriculumService, curriculumCourseService, courseService))
                .put(SessionBundleSubmissionController.class, new SessionBundleSubmissionController(sessionService, bundleSubmissionService, sessionProblemService, submissionLocalFileProvider, submissionRemoteFileProvider, userItemService))
                .put(SessionController.class, new SessionController(sessionService, userItemService))
                .put(SessionLessonController.class, new SessionLessonController(sessionService, sessionLessonService, sandalphon))
                .put(SessionProblemController.class, new SessionProblemController(sessionService, sessionProblemService, sandalphon))
                .put(SessionDependencyController.class, new SessionDependencyController(sessionService, sessionDependencyService))
                .put(SessionProgrammingSubmissionController.class, new SessionProgrammingSubmissionController(sessionService, submissionService, sessionProblemService, submissionLocalFileProvider, submissionRemoteFileProvider, userItemService))
                .put(TrainingController.class, new TrainingController())
                .put(TrainingCurriculumController.class, new TrainingCurriculumController(curriculumService))
                .put(TrainingCourseController.class, new TrainingCourseController(curriculumService, curriculumCourseService, courseSessionService, userItemService))
                .put(TrainingSessionController.class, new TrainingSessionController(curriculumService, curriculumCourseService, courseService, courseSessionService, userItemService))
                .put(TrainingLessonController.class, new TrainingLessonController(sandalphon, curriculumService, curriculumCourseService, courseService, courseSessionService, sessionService, sessionDependencyService, sessionLessonService, userItemService))
                .put(TrainingProblemController.class, new TrainingProblemController(sandalphon, curriculumService, curriculumCourseService, courseService, courseSessionService, sessionService, sessionDependencyService, sessionProblemService, userItemService))
                .put(TrainingBundleSubmissionController.class, new TrainingBundleSubmissionController(curriculumService, curriculumCourseService, courseService, courseSessionService, sessionService, sessionDependencyService, bundleSubmissionService, sessionProblemService, submissionLocalFileProvider, submissionRemoteFileProvider))
                .put(TrainingProgrammingSubmissionController.class, new TrainingProgrammingSubmissionController(curriculumService, curriculumCourseService, courseService, courseSessionService, sessionService, sessionDependencyService, submissionService, sessionProblemService, submissionLocalFileProvider, submissionRemoteFileProvider))
                .put(JophielClientController.class, new JophielClientController(jophiel, userService))
                .put(UserController.class, new UserController(jophiel, userService))
                .put(CourseAPIController.class, new CourseAPIController(courseService))
                .put(SessionAPIController.class, new SessionAPIController(sessionService))
                .build();
    }

    private void scheduleThreads() {
        Scheduler scheduler = Akka.system().scheduler();
        ExecutionContextExecutor context = Akka.system().dispatcher();

        GradingResponsePoller poller = new GradingResponsePoller(scheduler, context, submissionService, sealtiel, TimeUnit.MILLISECONDS.convert(2, TimeUnit.SECONDS));
        UserActivityMessagePusher userActivityMessagePusher = new UserActivityMessagePusher(jophiel, userService, UserActivityMessageServiceImpl.getInstance());

        scheduler.schedule(Duration.create(1, TimeUnit.SECONDS), Duration.create(3, TimeUnit.SECONDS), poller, context);
        scheduler.schedule(Duration.create(1, TimeUnit.SECONDS), Duration.create(1, TimeUnit.MINUTES), userActivityMessagePusher, context);
    }
}
