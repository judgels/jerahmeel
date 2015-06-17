package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.commons.FileSystemProvider;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.commons.controllers.BaseController;
import org.iatoki.judgels.commons.views.html.layouts.accessTypesLayout;
import org.iatoki.judgels.commons.views.html.layouts.heading3Layout;
import org.iatoki.judgels.gabriel.GradingLanguageRegistry;
import org.iatoki.judgels.gabriel.GradingSource;
import org.iatoki.judgels.jerahmeel.Course;
import org.iatoki.judgels.jerahmeel.CourseNotFoundException;
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
import org.iatoki.judgels.jerahmeel.services.JidCacheService;
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
import org.iatoki.judgels.sandalphon.Submission;
import org.iatoki.judgels.sandalphon.SubmissionAdapters;
import org.iatoki.judgels.sandalphon.SubmissionException;
import org.iatoki.judgels.sandalphon.SubmissionNotFoundException;
import org.iatoki.judgels.sandalphon.services.SubmissionService;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Map;

@Transactional
@Authenticated(value = {LoggedIn.class, HasRole.class})
public final class TrainingProgrammingSubmissionController extends BaseController {

    private static final long PAGE_SIZE = 20;

    private final CurriculumService curriculumService;
    private final CurriculumCourseService curriculumCourseService;
    private final CourseService courseService;
    private final CourseSessionService courseSessionService;
    private final SessionService sessionService;
    private final SessionDependencyService sessionDependencyService;
    private final SubmissionService submissionService;
    private final SessionProblemService sessionProblemService;
    private final FileSystemProvider submissionLocalFileProvider;
    private final FileSystemProvider submissionRemoteFileProvider;

    public TrainingProgrammingSubmissionController(CurriculumService curriculumService, CurriculumCourseService curriculumCourseService, CourseService courseService, CourseSessionService courseSessionService, SessionService sessionService, SessionDependencyService sessionDependencyService, SubmissionService submissionService, SessionProblemService sessionProblemService, FileSystemProvider submissionLocalFileProvider, FileSystemProvider submissionRemoteFileProvider) {
        this.curriculumService = curriculumService;
        this.curriculumCourseService = curriculumCourseService;
        this.courseService = courseService;
        this.courseSessionService = courseSessionService;
        this.sessionService = sessionService;
        this.sessionDependencyService = sessionDependencyService;
        this.submissionService = submissionService;
        this.sessionProblemService = sessionProblemService;
        this.submissionLocalFileProvider = submissionLocalFileProvider;
        this.submissionRemoteFileProvider = submissionRemoteFileProvider;
    }

    public Result postSubmitProblem(long curriculumId, long curriculumCourseId, long courseSessionId, String problemJid) throws CurriculumNotFoundException, CurriculumCourseNotFoundException, CourseSessionNotFoundException {
        Curriculum curriculum = curriculumService.findCurriculumByCurriculumId(curriculumId);
        CurriculumCourse curriculumCourse = curriculumCourseService.findCurriculumCourseByCurriculumCourseId(curriculumCourseId);
        CourseSession courseSession = courseSessionService.findByCourseSessionId(courseSessionId);

        if (((curriculum.getJid().equals(curriculumCourse.getCurriculumJid())) && (curriculumCourse.getCourseJid().equals(courseSession.getCourseJid()))) && (sessionDependencyService.isDependenciesFulfilled(IdentityUtils.getUserJid(), courseSession.getSessionJid()))) {
            SessionProblem sessionProblem = sessionProblemService.findSessionProblemBySessionJidAndProblemJid(courseSession.getSessionJid(), problemJid);

            Http.MultipartFormData body = request().body().asMultipartFormData();

            String gradingLanguage = body.asFormUrlEncoded().get("language")[0];
            String gradingEngine = body.asFormUrlEncoded().get("engine")[0];

            try {
                GradingSource source = SubmissionAdapters.fromGradingEngine(gradingEngine).createGradingSourceFromNewSubmission(body);
                String submissionJid = submissionService.submit(problemJid, courseSession.getSessionJid(), gradingEngine, gradingLanguage, null, source, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
                SubmissionAdapters.fromGradingEngine(gradingEngine).storeSubmissionFiles(submissionLocalFileProvider, submissionRemoteFileProvider, submissionJid, source);

                ControllerUtils.getInstance().addActivityLog("Submit to problem " + sessionProblem.getAlias() + " in session " + courseSession.getSessionName() + ".");

            } catch (SubmissionException e) {
                flash("submissionError", e.getMessage());

                return redirect(routes.TrainingProblemController.viewProblem(curriculum.getId(), curriculumCourse.getId(), courseSession.getId(), sessionProblem.getId()));
            }

            return redirect(routes.TrainingProgrammingSubmissionController.viewSubmissions(curriculum.getId(), curriculumCourse.getId(), courseSession.getId()));
        } else {
            return notFound();
        }
    }
    
    public Result viewSubmissions(long curriculumId, long curriculumCourseId, long courseSessionId) throws CurriculumNotFoundException, CurriculumCourseNotFoundException, CourseNotFoundException, CourseSessionNotFoundException, SessionNotFoundException {
        return listSubmissions(curriculumId, curriculumCourseId, courseSessionId, 0, "id", "desc", null);
    }

    public Result listSubmissions(long curriculumId, long curriculumCourseId, long courseSessionId, long pageIndex, String orderBy, String orderDir, String problemJid) throws CurriculumNotFoundException, CurriculumCourseNotFoundException, CourseNotFoundException, CourseSessionNotFoundException, SessionNotFoundException {
        Curriculum curriculum = curriculumService.findCurriculumByCurriculumId(curriculumId);
        CurriculumCourse curriculumCourse = curriculumCourseService.findCurriculumCourseByCurriculumCourseId(curriculumCourseId);
        CourseSession courseSession = courseSessionService.findByCourseSessionId(courseSessionId);

        if ((curriculum.getJid().equals(curriculumCourse.getCurriculumJid())) && (curriculumCourse.getCourseJid().equals(courseSession.getCourseJid()))) {
            Course course = courseService.findCourseByCourseJid(curriculumCourse.getCourseJid());
            Session session = sessionService.findSessionBySessionJid(courseSession.getSessionJid());

            String actualProblemJid = "(none)".equals(problemJid) ? null : problemJid;

            Page<Submission> submissions = submissionService.pageSubmissions(pageIndex, PAGE_SIZE, orderBy, orderDir, IdentityUtils.getUserJid(), actualProblemJid, session.getJid());
            Map<String, String> problemJidToAliasMap = sessionProblemService.findProgrammingProblemJidToAliasMapBySessionJid(session.getJid());
            Map<String, String> gradingLanguageToNameMap = GradingLanguageRegistry.getInstance().getGradingLanguages();

            LazyHtml content = new LazyHtml(listSubmissionsView.render(curriculum.getId(), curriculumCourse.getId(), courseSession.getId(), submissions, problemJidToAliasMap, gradingLanguageToNameMap, pageIndex, orderBy, orderDir, actualProblemJid));
            content.appendLayout(c -> heading3Layout.render(Messages.get("submission.submissions"), c));
            content.appendLayout(c -> accessTypesLayout.render(ImmutableList.of(
                        new InternalLink(Messages.get("training.submissions.bundle"), routes.TrainingController.jumpToBundleSubmissions(curriculum.getId(), curriculumCourse.getId(), courseSession.getId())),
                        new InternalLink(Messages.get("training.submissions.programming"), routes.TrainingController.jumpToProgrammingSubmissions(curriculum.getId(), curriculumCourse.getId(), courseSession.getId()))
                  ), c)
            );
            SessionControllerUtils.appendViewLayout(content, curriculum, curriculumCourse, courseSession, session);
            ControllerUtils.getInstance().appendSidebarLayout(content);
            ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                  new InternalLink(Messages.get("training.curriculums"), routes.TrainingController.jumpToCurriculums()),
                  new InternalLink(curriculum.getName(), routes.TrainingController.jumpToCourses(curriculum.getId())),
                  new InternalLink(course.getName(), routes.TrainingController.jumpToSessions(curriculum.getId(), curriculumCourse.getId())),
                  new InternalLink(session.getName(), routes.TrainingController.jumpToSubmissions(curriculum.getId(), curriculumCourse.getId(), courseSession.getId())),
                  new InternalLink(Messages.get("training.submissions.programming"), routes.TrainingController.jumpToProgrammingSubmissions(curriculum.getId(), curriculumCourse.getId(), courseSession.getId()))
            ));
            ControllerUtils.getInstance().appendTemplateLayout(content, "Sessions - Programming Submissions");

            return ControllerUtils.getInstance().lazyOk(content);
        } else {
            return notFound();
        }
    }

    public Result viewSubmission(long curriculumId, long curriculumCourseId, long courseSessionId, long submissionId) throws CurriculumNotFoundException, CurriculumCourseNotFoundException, CourseNotFoundException, CourseSessionNotFoundException, SessionNotFoundException, SessionProblemNotFoundException, SubmissionNotFoundException {
        Curriculum curriculum = curriculumService.findCurriculumByCurriculumId(curriculumId);
        CurriculumCourse curriculumCourse = curriculumCourseService.findCurriculumCourseByCurriculumCourseId(curriculumCourseId);
        CourseSession courseSession = courseSessionService.findByCourseSessionId(courseSessionId);

        if ((curriculum.getJid().equals(curriculumCourse.getCurriculumJid())) && (curriculumCourse.getCourseJid().equals(courseSession.getCourseJid()))) {
            Course course = courseService.findCourseByCourseJid(curriculumCourse.getCourseJid());
            Session session = sessionService.findSessionBySessionJid(courseSession.getSessionJid());

            Submission submission = submissionService.findSubmissionById(submissionId);

            GradingSource source = SubmissionAdapters.fromGradingEngine(submission.getGradingEngine()).createGradingSourceFromPastSubmission(submissionLocalFileProvider, submissionRemoteFileProvider, submission.getJid());
            String authorName = JidCacheService.getInstance().getDisplayName(submission.getAuthorJid());
            SessionProblem sessionProblem = sessionProblemService.findSessionProblemBySessionJidAndProblemJid(session.getJid(), submission.getProblemJid());
            String sessionProblemAlias = sessionProblem.getAlias();
            String sessionProblemName = JidCacheService.getInstance().getDisplayName(sessionProblem.getProblemJid());
            String gradingLanguageName = GradingLanguageRegistry.getInstance().getLanguage(submission.getGradingLanguage()).getName();

            LazyHtml content = new LazyHtml(SubmissionAdapters.fromGradingEngine(submission.getGradingEngine()).renderViewSubmission(submission, source, authorName, sessionProblemAlias, sessionProblemName, gradingLanguageName, session.getName()));
            content.appendLayout(c -> accessTypesLayout.render(ImmutableList.of(
                        new InternalLink(Messages.get("training.submissions.bundle"), routes.TrainingController.jumpToBundleSubmissions(curriculum.getId(), curriculumCourse.getId(), courseSession.getId())),
                        new InternalLink(Messages.get("training.submissions.programming"), routes.TrainingController.jumpToProgrammingSubmissions(curriculum.getId(), curriculumCourse.getId(), courseSession.getId()))
                  ), c)
            );
            SessionControllerUtils.appendViewLayout(content, curriculum, curriculumCourse, courseSession, session);
            ControllerUtils.getInstance().appendSidebarLayout(content);
            ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                  new InternalLink(Messages.get("training.curriculums"), routes.TrainingController.jumpToCurriculums()),
                  new InternalLink(curriculum.getName(), routes.TrainingController.jumpToCourses(curriculum.getId())),
                  new InternalLink(course.getName(), routes.TrainingController.jumpToSessions(curriculum.getId(), curriculumCourse.getId())),
                  new InternalLink(session.getName(), routes.TrainingController.jumpToSubmissions(curriculum.getId(), curriculumCourse.getId(), courseSession.getId())),
                  new InternalLink(Messages.get("training.submissions.programming"), routes.TrainingController.jumpToProgrammingSubmissions(curriculum.getId(), curriculumCourse.getId(), courseSession.getId())),
                  new InternalLink(submission.getId() + "", routes.TrainingProgrammingSubmissionController.viewSubmission(curriculum.getId(), curriculumCourse.getId(), courseSession.getId(), submission.getId()))
            ));
            ControllerUtils.getInstance().appendTemplateLayout(content, "Sessions - Programming Submissions - View");

            return ControllerUtils.getInstance().lazyOk(content);
        } else {
            return notFound();
        }
    }
}
