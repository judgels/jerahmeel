package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.iatoki.judgels.FileSystemProvider;
import org.iatoki.judgels.api.sandalphon.SandalphonResourceDisplayNameUtils;
import org.iatoki.judgels.jerahmeel.Course;
import org.iatoki.judgels.jerahmeel.CourseNotFoundException;
import org.iatoki.judgels.jerahmeel.CourseSession;
import org.iatoki.judgels.jerahmeel.CourseSessionNotFoundException;
import org.iatoki.judgels.jerahmeel.Curriculum;
import org.iatoki.judgels.jerahmeel.CurriculumCourse;
import org.iatoki.judgels.jerahmeel.CurriculumCourseNotFoundException;
import org.iatoki.judgels.jerahmeel.CurriculumNotFoundException;
import org.iatoki.judgels.jerahmeel.JerahmeelActivityKeys;
import org.iatoki.judgels.jerahmeel.Session;
import org.iatoki.judgels.jerahmeel.SessionNotFoundException;
import org.iatoki.judgels.jerahmeel.SessionProblem;
import org.iatoki.judgels.jerahmeel.SessionProblemNotFoundException;
import org.iatoki.judgels.jerahmeel.config.BundleSubmissionLocalFileSystemProvider;
import org.iatoki.judgels.jerahmeel.config.BundleSubmissionRemoteFileSystemProvider;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.securities.HasRole;
import org.iatoki.judgels.jerahmeel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jerahmeel.services.CourseService;
import org.iatoki.judgels.jerahmeel.services.CourseSessionService;
import org.iatoki.judgels.jerahmeel.services.CurriculumCourseService;
import org.iatoki.judgels.jerahmeel.services.CurriculumService;
import org.iatoki.judgels.jerahmeel.services.SessionDependencyService;
import org.iatoki.judgels.jerahmeel.services.SessionProblemService;
import org.iatoki.judgels.jerahmeel.services.SessionService;
import org.iatoki.judgels.jerahmeel.services.impls.JidCacheServiceImpl;
import org.iatoki.judgels.jerahmeel.views.html.training.course.session.submission.bundle.listSubmissionsView;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.heading3Layout;
import org.iatoki.judgels.sandalphon.BundleAnswer;
import org.iatoki.judgels.sandalphon.BundleDetailResult;
import org.iatoki.judgels.sandalphon.BundleSubmission;
import org.iatoki.judgels.sandalphon.BundleSubmissionNotFoundException;
import org.iatoki.judgels.sandalphon.services.BundleSubmissionService;
import org.iatoki.judgels.sandalphon.views.html.problem.bundle.submission.bundleSubmissionView;
import play.data.DynamicForm;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.mvc.Result;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Map;

@Authenticated(value = {LoggedIn.class, HasRole.class})
@Singleton
@Named
public final class TrainingBundleSubmissionController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;
    private static final String SUBMISSION = "submission";
    private static final String BUNDLE_ANSWER = "bundle answer";
    private static final String PROBLEM = "problem";
    private static final String SESSION = "session";

    private final FileSystemProvider bundleSubmissionLocalFileSystemProvider;
    private final FileSystemProvider bundleSubmissionRemoteFileSystemProvider;
    private final BundleSubmissionService bundleSubmissionService;
    private final CourseService courseService;
    private final CourseSessionService courseSessionService;
    private final CurriculumCourseService curriculumCourseService;
    private final CurriculumService curriculumService;
    private final SessionDependencyService sessionDependencyService;
    private final SessionService sessionService;
    private final SessionProblemService sessionProblemService;

    @Inject
    public TrainingBundleSubmissionController(@BundleSubmissionLocalFileSystemProvider FileSystemProvider bundleSubmissionLocalFileSystemProvider, @BundleSubmissionRemoteFileSystemProvider @Nullable FileSystemProvider bundleSubmissionRemoteFileSystemProvider, BundleSubmissionService bundleSubmissionService, CourseService courseService, CourseSessionService courseSessionService, CurriculumCourseService curriculumCourseService, CurriculumService curriculumService, SessionDependencyService sessionDependencyService, SessionService sessionService, SessionProblemService sessionProblemService) {
        this.bundleSubmissionLocalFileSystemProvider = bundleSubmissionLocalFileSystemProvider;
        this.bundleSubmissionRemoteFileSystemProvider = bundleSubmissionRemoteFileSystemProvider;
        this.bundleSubmissionService = bundleSubmissionService;
        this.courseService = courseService;
        this.courseSessionService = courseSessionService;
        this.curriculumCourseService = curriculumCourseService;
        this.curriculumService = curriculumService;
        this.sessionDependencyService = sessionDependencyService;
        this.sessionService = sessionService;
        this.sessionProblemService = sessionProblemService;
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

        DynamicForm dForm = DynamicForm.form().bindFromRequest();

        BundleAnswer bundleAnswer = bundleSubmissionService.createBundleAnswerFromNewSubmission(dForm, SessionControllerUtils.getCurrentStatementLanguage());
        String submissionJid = bundleSubmissionService.submit(sessionProblem.getProblemJid(), courseSession.getSessionJid(), bundleAnswer, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        bundleSubmissionService.storeSubmissionFiles(bundleSubmissionLocalFileSystemProvider, bundleSubmissionRemoteFileSystemProvider, submissionJid, bundleAnswer);

        JerahmeelControllerUtils.getInstance().addActivityLog(JerahmeelActivityKeys.SUBMIT.construct(SESSION, courseSession.getSessionJid(), courseSession.getSessionName(), PROBLEM, sessionProblem.getProblemJid(), SandalphonResourceDisplayNameUtils.parseSlugByLanguage(JidCacheServiceImpl.getInstance().getDisplayName(sessionProblem.getProblemJid())), SUBMISSION, submissionJid, BUNDLE_ANSWER));

        return redirect(routes.TrainingBundleSubmissionController.viewSubmissions(curriculum.getId(), curriculumCourse.getId(), courseSession.getId()));
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

        Page<BundleSubmission> pageOfBundleSubmissions = bundleSubmissionService.getPageOfBundleSubmissions(pageIndex, PAGE_SIZE, orderBy, orderDir, IdentityUtils.getUserJid(), actualProblemJid, session.getJid());
        Map<String, String> problemJidToAliasMap = sessionProblemService.getBundleProblemJidToAliasMapBySessionJid(session.getJid());

        LazyHtml content = new LazyHtml(listSubmissionsView.render(curriculum.getId(), curriculumCourse.getId(), courseSession.getId(), pageOfBundleSubmissions, problemJidToAliasMap, pageIndex, orderBy, orderDir, actualProblemJid));
        content.appendLayout(c -> heading3Layout.render(Messages.get("submission.submissions"), c));
        TrainingSubmissionControllerUtils.appendSubtabLayout(content, curriculum, curriculumCourse, course, courseSession);
        SessionControllerUtils.appendViewLayout(content, curriculum, curriculumCourse, courseSession, session);
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, curriculum, curriculumCourse, course, courseSession, session);
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Sessions - Bundle Submissions");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    @Transactional(readOnly = true)
    public Result viewSubmission(long curriculumId, long curriculumCourseId, long courseSessionId, long bundleSubmissionId) throws CurriculumNotFoundException, CurriculumCourseNotFoundException, CourseNotFoundException, CourseSessionNotFoundException, SessionNotFoundException, SessionProblemNotFoundException, BundleSubmissionNotFoundException {
        Curriculum curriculum = curriculumService.findCurriculumById(curriculumId);
        CurriculumCourse curriculumCourse = curriculumCourseService.findCurriculumCourseByCurriculumCourseId(curriculumCourseId);
        CourseSession courseSession = courseSessionService.findCourseSessionById(courseSessionId);

        if (!curriculum.getJid().equals(curriculumCourse.getCurriculumJid()) || !curriculumCourse.getCourseJid().equals(courseSession.getCourseJid())) {
            return notFound();
        }

        Course course = courseService.findCourseByJid(curriculumCourse.getCourseJid());
        Session session = sessionService.findSessionByJid(courseSession.getSessionJid());

        BundleSubmission bundleSubmission = bundleSubmissionService.findBundleSubmissionById(bundleSubmissionId);
        BundleAnswer bundleAnswer;
        try {
            bundleAnswer = bundleSubmissionService.createBundleAnswerFromPastSubmission(bundleSubmissionLocalFileSystemProvider, bundleSubmissionRemoteFileSystemProvider, bundleSubmission.getJid());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        SessionProblem sessionProblem = sessionProblemService.findSessionProblemBySessionJidAndProblemJid(session.getJid(), bundleSubmission.getProblemJid());
        String sessionProblemAlias = sessionProblem.getAlias();
        String sessionProblemName = JidCacheServiceImpl.getInstance().getDisplayName(sessionProblem.getProblemJid());

        LazyHtml content = new LazyHtml(bundleSubmissionView.render(bundleSubmission, new Gson().fromJson(bundleSubmission.getLatestDetails(), new TypeToken<Map<String, BundleDetailResult>>() { }.getType()), bundleAnswer, JidCacheServiceImpl.getInstance().getDisplayName(bundleSubmission.getAuthorJid()), sessionProblemAlias, sessionProblemName, session.getName()));
        TrainingSubmissionControllerUtils.appendSubtabLayout(content, curriculum, curriculumCourse, course, courseSession);
        SessionControllerUtils.appendViewLayout(content, curriculum, curriculumCourse, courseSession, session);
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, curriculum, curriculumCourse, course, courseSession, session,
                new InternalLink(bundleSubmission.getId() + "", routes.TrainingBundleSubmissionController.viewSubmission(curriculum.getId(), curriculumCourse.getId(), courseSession.getId(), bundleSubmission.getId()))
        );
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Sessions - Bundle Submissions - View");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    private void appendBreadcrumbsLayout(LazyHtml content, Curriculum curriculum, CurriculumCourse curriculumCourse, Course course, CourseSession courseSession, Session session, InternalLink... lastLinks) {
        ImmutableList.Builder<InternalLink> breadcrumbsBuilder = TrainingControllerUtils.getBreadcrumbsBuilder();
        breadcrumbsBuilder.add(new InternalLink(curriculum.getName(), routes.TrainingController.jumpToCourses(curriculum.getId())));
        breadcrumbsBuilder.add(new InternalLink(course.getName(), routes.TrainingController.jumpToSessions(curriculum.getId(), curriculumCourse.getId())));
        breadcrumbsBuilder.add(new InternalLink(session.getName(), routes.TrainingLessonController.viewLessons(curriculum.getId(), curriculumCourse.getId(), courseSession.getId())));
        breadcrumbsBuilder.add(new InternalLink(Messages.get("training.submissions.bundle"), routes.TrainingBundleSubmissionController.viewSubmissions(curriculum.getId(), curriculumCourse.getId(), courseSession.getId())));
        breadcrumbsBuilder.add(lastLinks);

        JerahmeelControllerUtils.getInstance().appendBreadcrumbsLayout(content, breadcrumbsBuilder.build());
    }
}
