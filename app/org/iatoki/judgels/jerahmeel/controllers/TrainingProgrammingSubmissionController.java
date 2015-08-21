package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.FileSystemProvider;
import org.iatoki.judgels.gabriel.SubmissionSource;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.subtabLayout;
import org.iatoki.judgels.play.views.html.layouts.heading3Layout;
import org.iatoki.judgels.gabriel.GradingLanguageRegistry;
import org.iatoki.judgels.jerahmeel.Course;
import org.iatoki.judgels.jerahmeel.CourseNotFoundException;
import org.iatoki.judgels.jerahmeel.config.ProgrammingSubmissionLocalFileSystemProvider;
import org.iatoki.judgels.jerahmeel.config.ProgrammingSubmissionRemoteFileSystemProvider;
import org.iatoki.judgels.jerahmeel.services.CourseService;
import org.iatoki.judgels.jerahmeel.CourseSession;
import org.iatoki.judgels.jerahmeel.CourseSessionNotFoundException;
import org.iatoki.judgels.jerahmeel.services.CourseSessionService;
import org.iatoki.judgels.jerahmeel.Curriculum;
import org.iatoki.judgels.jerahmeel.CurriculumCourse;
import org.iatoki.judgels.jerahmeel.CurriculumCourseNotFoundException;
import org.iatoki.judgels.jerahmeel.services.CurriculumCourseService;
import org.iatoki.judgels.jerahmeel.CurriculumNotFoundException;
import org.iatoki.judgels.jerahmeel.services.CurriculumService;
import org.iatoki.judgels.jerahmeel.services.impls.JidCacheServiceImpl;
import org.iatoki.judgels.jerahmeel.Session;
import org.iatoki.judgels.jerahmeel.SessionNotFoundException;
import org.iatoki.judgels.jerahmeel.SessionProblem;
import org.iatoki.judgels.jerahmeel.SessionProblemNotFoundException;
import org.iatoki.judgels.jerahmeel.services.SessionProblemService;
import org.iatoki.judgels.jerahmeel.services.SessionService;
import org.iatoki.judgels.jerahmeel.services.SessionDependencyService;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.securities.HasRole;
import org.iatoki.judgels.jerahmeel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jerahmeel.views.html.training.course.session.submission.programming.listSubmissionsView;
import org.iatoki.judgels.sandalphon.ProgrammingSubmission;
import org.iatoki.judgels.sandalphon.ProgrammingSubmissionException;
import org.iatoki.judgels.sandalphon.ProgrammingSubmissionNotFoundException;
import org.iatoki.judgels.sandalphon.ProgrammingSubmissionUtils;
import org.iatoki.judgels.sandalphon.adapters.GradingEngineAdapterRegistry;
import org.iatoki.judgels.sandalphon.services.ProgrammingSubmissionService;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.mvc.Http;
import play.mvc.Result;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Map;

@Authenticated(value = {LoggedIn.class, HasRole.class})
@Singleton
@Named
public final class TrainingProgrammingSubmissionController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;

    private final CourseService courseService;
    private final CourseSessionService courseSessionService;
    private final CurriculumCourseService curriculumCourseService;
    private final CurriculumService curriculumService;
    private final SessionDependencyService sessionDependencyService;
    private final SessionProblemService sessionProblemService;
    private final SessionService sessionService;
    private final FileSystemProvider programmingSubmissionLocalFileSystemProvider;
    private final FileSystemProvider programmingSubmissionRemoteFileSystemProvider;
    private final ProgrammingSubmissionService programmingSubmissionService;

    @Inject
    public TrainingProgrammingSubmissionController(CourseService courseService, CourseSessionService courseSessionService, CurriculumCourseService curriculumCourseService, CurriculumService curriculumService, SessionDependencyService sessionDependencyService, SessionProblemService sessionProblemService, SessionService sessionService, @ProgrammingSubmissionLocalFileSystemProvider FileSystemProvider programmingSubmissionLocalFileSystemProvider, @ProgrammingSubmissionRemoteFileSystemProvider @Nullable FileSystemProvider programmingSubmissionRemoteFileSystemProvider, ProgrammingSubmissionService programmingSubmissionService) {
        this.courseService = courseService;
        this.courseSessionService = courseSessionService;
        this.curriculumCourseService = curriculumCourseService;
        this.curriculumService = curriculumService;
        this.sessionDependencyService = sessionDependencyService;
        this.sessionProblemService = sessionProblemService;
        this.sessionService = sessionService;
        this.programmingSubmissionLocalFileSystemProvider = programmingSubmissionLocalFileSystemProvider;
        this.programmingSubmissionRemoteFileSystemProvider = programmingSubmissionRemoteFileSystemProvider;
        this.programmingSubmissionService = programmingSubmissionService;
    }

    @Transactional
    public Result postSubmitProblem(long curriculumId, long curriculumCourseId, long courseSessionId, String problemJid) throws CurriculumNotFoundException, CurriculumCourseNotFoundException, CourseSessionNotFoundException {
        Curriculum curriculum = curriculumService.findCurriculumById(curriculumId);
        CurriculumCourse curriculumCourse = curriculumCourseService.findCurriculumCourseByCurriculumCourseId(curriculumCourseId);
        CourseSession courseSession = courseSessionService.findCourseSessionById(courseSessionId);

        if (!curriculum.getJid().equals(curriculumCourse.getCurriculumJid()) || !curriculumCourse.getCourseJid().equals(courseSession.getCourseJid()) || !sessionDependencyService.isDependenciesFulfilled(IdentityUtils.getUserJid(), courseSession.getSessionJid())) {
            return notFound();
        }

        SessionProblem sessionProblem = sessionProblemService.findSessionProblemBySessionJidAndProblemJid(courseSession.getSessionJid(), problemJid);

        Http.MultipartFormData body = request().body().asMultipartFormData();

        String gradingLanguage = body.asFormUrlEncoded().get("language")[0];
        String gradingEngine = body.asFormUrlEncoded().get("engine")[0];

        try {
            SubmissionSource submissionSource = ProgrammingSubmissionUtils.createSubmissionSourceFromNewSubmission(body);
            String submissionJid = programmingSubmissionService.submit(problemJid, courseSession.getSessionJid(), gradingEngine, gradingLanguage, null, submissionSource, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
            ProgrammingSubmissionUtils.storeSubmissionFiles(programmingSubmissionLocalFileSystemProvider, programmingSubmissionRemoteFileSystemProvider, submissionJid, submissionSource);

            JerahmeelControllerUtils.getInstance().addActivityLog("Submit to problem " + sessionProblem.getAlias() + " in session " + courseSession.getSessionName() + ".");

        } catch (ProgrammingSubmissionException e) {
            flash("submissionError", e.getMessage());

            return redirect(routes.TrainingProblemController.viewProblem(curriculum.getId(), curriculumCourse.getId(), courseSession.getId(), sessionProblem.getId()));
        }

        return redirect(routes.TrainingProgrammingSubmissionController.viewSubmissions(curriculum.getId(), curriculumCourse.getId(), courseSession.getId()));
    }

    @Transactional(readOnly = true)
    public Result viewSubmissions(long curriculumId, long curriculumCourseId, long courseSessionId) throws CurriculumNotFoundException, CurriculumCourseNotFoundException, CourseNotFoundException, CourseSessionNotFoundException, SessionNotFoundException {
        return listSubmissions(curriculumId, curriculumCourseId, courseSessionId, 0, "id", "desc", null);
    }

    @Transactional(readOnly = true)
    public Result listSubmissions(long curriculumId, long curriculumCourseId, long courseSessionId, long pageIndex, String orderBy, String orderDir, String problemJid) throws CurriculumNotFoundException, CurriculumCourseNotFoundException, CourseNotFoundException, CourseSessionNotFoundException, SessionNotFoundException {
        Curriculum curriculum = curriculumService.findCurriculumById(curriculumId);
        CurriculumCourse curriculumCourse = curriculumCourseService.findCurriculumCourseByCurriculumCourseId(curriculumCourseId);
        CourseSession courseSession = courseSessionService.findCourseSessionById(courseSessionId);

        if (!curriculum.getJid().equals(curriculumCourse.getCurriculumJid()) || !curriculumCourse.getCourseJid().equals(courseSession.getCourseJid())) {
            return notFound();
        }

        Course course = courseService.findCourseByJid(curriculumCourse.getCourseJid());
        Session session = sessionService.findSessionByJid(courseSession.getSessionJid());

        String actualProblemJid = "(none)".equals(problemJid) ? null : problemJid;

        Page<ProgrammingSubmission> pageOfSubmissions = programmingSubmissionService.getPageOfProgrammingSubmissions(pageIndex, PAGE_SIZE, orderBy, orderDir, IdentityUtils.getUserJid(), actualProblemJid, session.getJid());
        Map<String, String> problemJidToAliasMap = sessionProblemService.findProgrammingProblemJidToAliasMapBySessionJid(session.getJid());
        Map<String, String> gradingLanguageToNameMap = GradingLanguageRegistry.getInstance().getGradingLanguages();

        LazyHtml content = new LazyHtml(listSubmissionsView.render(curriculum.getId(), curriculumCourse.getId(), courseSession.getId(), pageOfSubmissions, problemJidToAliasMap, gradingLanguageToNameMap, pageIndex, orderBy, orderDir, actualProblemJid));
        content.appendLayout(c -> heading3Layout.render(Messages.get("submission.submissions"), c));
        appendSubtabLayout(content, curriculum, curriculumCourse, course, courseSession);
        SessionControllerUtils.appendViewLayout(content, curriculum, curriculumCourse, courseSession, session);
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, curriculum, curriculumCourse, course, courseSession, session);
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Sessions - Programming Submissions");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    @Transactional(readOnly = true)
    public Result viewSubmission(long curriculumId, long curriculumCourseId, long courseSessionId, long submissionId) throws CurriculumNotFoundException, CurriculumCourseNotFoundException, CourseNotFoundException, CourseSessionNotFoundException, SessionNotFoundException, SessionProblemNotFoundException, ProgrammingSubmissionNotFoundException {
        Curriculum curriculum = curriculumService.findCurriculumById(curriculumId);
        CurriculumCourse curriculumCourse = curriculumCourseService.findCurriculumCourseByCurriculumCourseId(curriculumCourseId);
        CourseSession courseSession = courseSessionService.findCourseSessionById(courseSessionId);

        if (!curriculum.getJid().equals(curriculumCourse.getCurriculumJid()) || !curriculumCourse.getCourseJid().equals(courseSession.getCourseJid())) {
            return notFound();
        }

        Course course = courseService.findCourseByJid(curriculumCourse.getCourseJid());
        Session session = sessionService.findSessionByJid(courseSession.getSessionJid());

        ProgrammingSubmission submission = programmingSubmissionService.findProgrammingSubmissionById(submissionId);

        SubmissionSource submissionSource = ProgrammingSubmissionUtils.createSubmissionSourceFromPastSubmission(programmingSubmissionLocalFileSystemProvider, programmingSubmissionRemoteFileSystemProvider, submission.getJid());
        String authorName = JidCacheServiceImpl.getInstance().getDisplayName(submission.getAuthorJid());
        SessionProblem sessionProblem = sessionProblemService.findSessionProblemBySessionJidAndProblemJid(session.getJid(), submission.getProblemJid());
        String sessionProblemAlias = sessionProblem.getAlias();
        String sessionProblemName = JidCacheServiceImpl.getInstance().getDisplayName(sessionProblem.getProblemJid());
        String gradingLanguageName = GradingLanguageRegistry.getInstance().getLanguage(submission.getGradingLanguage()).getName();

        LazyHtml content = new LazyHtml(GradingEngineAdapterRegistry.getInstance().getByGradingEngineName(submission.getGradingEngine()).renderViewSubmission(submission, submissionSource, authorName, sessionProblemAlias, sessionProblemName, gradingLanguageName, session.getName()));
        appendSubtabLayout(content, curriculum, curriculumCourse, course, courseSession);
        SessionControllerUtils.appendViewLayout(content, curriculum, curriculumCourse, courseSession, session);
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, curriculum, curriculumCourse, course, courseSession, session,
                new InternalLink(submission.getId() + "", routes.TrainingProgrammingSubmissionController.viewSubmission(curriculum.getId(), curriculumCourse.getId(), courseSession.getId(), submission.getId()))
        );
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Sessions - Programming Submissions - View");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    private void appendSubtabLayout(LazyHtml content, Curriculum curriculum, CurriculumCourse curriculumCourse, Course course, CourseSession courseSession) {
        content.appendLayout(c -> subtabLayout.render(ImmutableList.of(
                        new InternalLink(Messages.get("training.submissions.programming"), routes.TrainingController.jumpToProgrammingSubmissions(curriculum.getId(), curriculumCourse.getId(), courseSession.getId())),
                        new InternalLink(Messages.get("training.submissions.bundle"), routes.TrainingController.jumpToBundleSubmissions(curriculum.getId(), curriculumCourse.getId(), courseSession.getId()))
                ), c)
        );
    }

    private void appendBreadcrumbsLayout(LazyHtml content, Curriculum curriculum, CurriculumCourse curriculumCourse, Course course, CourseSession courseSession, Session session, InternalLink... lastLinks) {
        ImmutableList.Builder<InternalLink> breadcrumbsBuilder = TrainingControllerUtils.getBreadcrumbsBuilder();
        breadcrumbsBuilder.add(new InternalLink(curriculum.getName(), routes.TrainingController.jumpToCourses(curriculum.getId())));
        breadcrumbsBuilder.add(new InternalLink(course.getName(), routes.TrainingController.jumpToSessions(curriculum.getId(), curriculumCourse.getId())));
        breadcrumbsBuilder.add(new InternalLink(session.getName(), routes.TrainingController.jumpToSubmissions(curriculum.getId(), curriculumCourse.getId(), courseSession.getId())));
        breadcrumbsBuilder.add(new InternalLink(Messages.get("training.submissions.programming"), routes.TrainingController.jumpToProgrammingSubmissions(curriculum.getId(), curriculumCourse.getId(), courseSession.getId())));
        breadcrumbsBuilder.add(lastLinks);

        JerahmeelControllerUtils.getInstance().appendBreadcrumbsLayout(content, breadcrumbsBuilder.build());
    }
}
