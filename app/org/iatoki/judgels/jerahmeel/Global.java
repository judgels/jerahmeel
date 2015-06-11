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
import org.iatoki.judgels.jophiel.commons.DefaultUserActivityServiceImpl;
import org.iatoki.judgels.sandalphon.commons.GradingResponsePoller;
import org.iatoki.judgels.sandalphon.commons.SubmissionService;
import org.iatoki.judgels.jerahmeel.controllers.CourseController;
import org.iatoki.judgels.jerahmeel.controllers.CourseSessionController;
import org.iatoki.judgels.jerahmeel.controllers.CurriculumController;
import org.iatoki.judgels.jerahmeel.controllers.CurriculumCourseController;
import org.iatoki.judgels.jerahmeel.controllers.SessionBundleSubmissionController;
import org.iatoki.judgels.jerahmeel.controllers.SessionController;
import org.iatoki.judgels.jerahmeel.controllers.SessionLessonController;
import org.iatoki.judgels.jerahmeel.controllers.SessionProblemController;
import org.iatoki.judgels.jerahmeel.controllers.SessionSessionController;
import org.iatoki.judgels.jerahmeel.controllers.SessionProgrammingSubmissionController;
import org.iatoki.judgels.jerahmeel.controllers.TrainingController;
import org.iatoki.judgels.jerahmeel.controllers.TrainingCourseController;
import org.iatoki.judgels.jerahmeel.controllers.TrainingCurriculumController;
import org.iatoki.judgels.jerahmeel.controllers.TrainingLessonController;
import org.iatoki.judgels.jerahmeel.controllers.TrainingProblemController;
import org.iatoki.judgels.jerahmeel.controllers.TrainingSessionController;
import org.iatoki.judgels.jerahmeel.controllers.apis.CourseAPIController;
import org.iatoki.judgels.jerahmeel.controllers.apis.SessionAPIController;
import org.iatoki.judgels.jerahmeel.models.daos.hibernate.BundleGradingHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.hibernate.BundleSubmissionHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.hibernate.CourseHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.hibernate.CourseSessionHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.hibernate.CurriculumCourseHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.hibernate.CurriculumHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.hibernate.SessionHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.hibernate.SessionLessonHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.hibernate.SessionProblemHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.hibernate.SessionSessionHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.hibernate.UserItemHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.BundleGradingDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.BundleSubmissionDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.CourseDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.CourseSessionDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.CurriculumCourseDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.CurriculumDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.SessionDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.SessionLessonDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.SessionProblemDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.SessionSessionDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.UserItemDao;
import org.iatoki.judgels.jophiel.commons.Jophiel;
import org.iatoki.judgels.jophiel.commons.UserActivityPusher;
import org.iatoki.judgels.jophiel.commons.controllers.JophielClientController;
import org.iatoki.judgels.sandalphon.commons.Sandalphon;
import org.iatoki.judgels.sealtiel.client.Sealtiel;
import org.iatoki.judgels.jerahmeel.controllers.ApplicationController;
import org.iatoki.judgels.jerahmeel.controllers.UserController;
import org.iatoki.judgels.jerahmeel.models.daos.hibernate.AvatarCacheHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.hibernate.GradingHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.hibernate.JidCacheHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.hibernate.SubmissionHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.hibernate.UserHibernateDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.AvatarCacheDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.GradingDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.JidCacheDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.SubmissionDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.UserDao;
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
    private SessionSessionDao sessionSessionDao;
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
    private SessionSessionService sessionSessionService;
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
        sessionSessionDao = new SessionSessionHibernateDao();
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
        jophiel = new Jophiel(jerahmeelProps.getJophielClientJid(), jerahmeelProps.getJophielClientSecret(), jerahmeelProps.getJophielBaseUrl());
        sandalphon = new Sandalphon(jerahmeelProps.getSandalphonBaseUrl(), jerahmeelProps.getSandalphonClientJid(), jerahmeelProps.getSandalphonClientSecret());
        sealtiel = new Sealtiel(jerahmeelProps.getSealtielClientJid(), jerahmeelProps.getSealtielClientSecret(), jerahmeelProps.getSealtielBaseUrl());
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
        courseSessionService = new CourseSessionServiceImpl(courseSessionDao, sessionDao, sessionSessionDao, userItemDao);
        curriculumCourseService = new CurriculumCourseServiceImpl(curriculumCourseDao, courseDao, courseSessionDao, sessionSessionDao, userItemDao);
        curriculumService = new CurriculumServiceImpl(curriculumDao);
        sessionLessonService = new SessionLessonServiceImpl(sessionLessonDao, userItemDao);
        sessionProblemService = new SessionProblemServiceImpl(sessionProblemDao, userItemDao);
        sessionService = new SessionServiceImpl(sessionDao);
        sessionSessionService = new SessionSessionServiceImpl(sessionDao, sessionSessionDao, userItemDao);
        submissionService = new SubmissionServiceImpl(submissionDao, gradingDao, sealtiel, jerahmeelProps.getSealtielGabrielClientJid(), sessionProblemDao, userItemDao);
        userService = new UserServiceImpl(jophiel, userDao);
        userItemService = new UserItemServiceImpl(userItemDao);

        JidCacheService.buildInstance(jidCacheDao);
        AvatarCacheService.buildInstance(jophiel, avatarCacheDao);
        ControllerUtils.buildInstance(jophiel);
        DefaultUserActivityServiceImpl.buildInstance(jophiel);
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
                .put(SessionSessionController.class, new SessionSessionController(sessionService, sessionSessionService))
                .put(SessionProgrammingSubmissionController.class, new SessionProgrammingSubmissionController(sessionService, submissionService, sessionProblemService, submissionLocalFileProvider, submissionRemoteFileProvider, userItemService))
                .put(TrainingController.class, new TrainingController())
                .put(TrainingCurriculumController.class, new TrainingCurriculumController(curriculumService))
                .put(TrainingCourseController.class, new TrainingCourseController(curriculumService, curriculumCourseService, courseSessionService, userItemService))
                .put(TrainingSessionController.class, new TrainingSessionController(curriculumService, curriculumCourseService, courseService, courseSessionService, userItemService))
                .put(TrainingLessonController.class, new TrainingLessonController(sandalphon, curriculumService, curriculumCourseService, courseService, courseSessionService, sessionService, sessionSessionService, sessionLessonService, userItemService))
                .put(TrainingProblemController.class, new TrainingProblemController(sandalphon, curriculumService, curriculumCourseService, courseService, courseSessionService, sessionService, sessionSessionService, sessionProblemService, userItemService))
                .put(TrainingBundleSubmissionController.class, new TrainingBundleSubmissionController(curriculumService, curriculumCourseService, courseService, courseSessionService, sessionService, sessionSessionService, bundleSubmissionService, sessionProblemService, submissionLocalFileProvider, submissionRemoteFileProvider))
                .put(TrainingProgrammingSubmissionController.class, new TrainingProgrammingSubmissionController(curriculumService, curriculumCourseService, courseService, courseSessionService, sessionService, sessionSessionService, submissionService, sessionProblemService, submissionLocalFileProvider, submissionRemoteFileProvider))
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
        UserActivityPusher userActivityPusher = new UserActivityPusher(jophiel, userService, UserActivityServiceImpl.getInstance());

        scheduler.schedule(Duration.create(1, TimeUnit.SECONDS), Duration.create(3, TimeUnit.SECONDS), poller, context);
        scheduler.schedule(Duration.create(1, TimeUnit.SECONDS), Duration.create(1, TimeUnit.MINUTES), userActivityPusher, context);
    }
}
