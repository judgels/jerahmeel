package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.iatoki.judgels.commons.FileSystemProvider;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.commons.controllers.BaseController;
import org.iatoki.judgels.commons.views.html.layouts.accessTypesLayout;
import org.iatoki.judgels.commons.views.html.layouts.heading3Layout;
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
import org.iatoki.judgels.jerahmeel.views.html.training.course.session.submission.bundle.listSubmissionsView;
import org.iatoki.judgels.sandalphon.BundleAnswer;
import org.iatoki.judgels.sandalphon.BundleSubmission;
import org.iatoki.judgels.sandalphon.BundleSubmissionNotFoundException;
import org.iatoki.judgels.sandalphon.services.BundleSubmissionService;
import org.iatoki.judgels.sandalphon.views.html.bundleSubmissionView;
import play.data.DynamicForm;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.mvc.Result;

import java.io.IOException;
import java.util.Map;

@Authenticated(value = {LoggedIn.class, HasRole.class})
public final class TrainingBundleSubmissionController extends BaseController {

    private static final long PAGE_SIZE = 20;

    private final CurriculumService curriculumService;
    private final CurriculumCourseService curriculumCourseService;
    private final CourseService courseService;
    private final CourseSessionService courseSessionService;
    private final SessionService sessionService;
    private final SessionDependencyService sessionDependencyService;
    private final BundleSubmissionService bundleSubmissionService;
    private final SessionProblemService sessionProblemService;
    private final FileSystemProvider bundleSubmissionLocalFileProvider;
    private final FileSystemProvider bundleSubmissionRemoteFileProvider;

    public TrainingBundleSubmissionController(CurriculumService curriculumService, CurriculumCourseService curriculumCourseService, CourseService courseService, CourseSessionService courseSessionService, SessionService sessionService, SessionDependencyService sessionDependencyService, BundleSubmissionService bundleSubmissionService, SessionProblemService sessionProblemService, FileSystemProvider bundleSubmissionLocalFileProvider, FileSystemProvider bundleSubmissionRemoteFileProvider) {
        this.curriculumService = curriculumService;
        this.curriculumCourseService = curriculumCourseService;
        this.courseService = courseService;
        this.courseSessionService = courseSessionService;
        this.sessionService = sessionService;
        this.sessionDependencyService = sessionDependencyService;
        this.bundleSubmissionService = bundleSubmissionService;
        this.sessionProblemService = sessionProblemService;
        this.bundleSubmissionLocalFileProvider = bundleSubmissionLocalFileProvider;
        this.bundleSubmissionRemoteFileProvider = bundleSubmissionRemoteFileProvider;
    }

    @Transactional
    public Result postSubmitProblem(long curriculumId, long curriculumCourseId, long courseSessionId, String problemJid) throws CurriculumNotFoundException, CurriculumCourseNotFoundException, CourseSessionNotFoundException {
        Curriculum curriculum = curriculumService.findCurriculumByCurriculumId(curriculumId);
        CurriculumCourse curriculumCourse = curriculumCourseService.findCurriculumCourseByCurriculumCourseId(curriculumCourseId);
        CourseSession courseSession = courseSessionService.findByCourseSessionId(courseSessionId);

        if (((curriculum.getJid().equals(curriculumCourse.getCurriculumJid())) && (curriculumCourse.getCourseJid().equals(courseSession.getCourseJid()))) && (sessionDependencyService.isDependenciesFulfilled(IdentityUtils.getUserJid(), courseSession.getSessionJid()))) {
            SessionProblem sessionProblem = sessionProblemService.findSessionProblemBySessionJidAndProblemJid(courseSession.getSessionJid(), problemJid);

            DynamicForm dynamicForm = DynamicForm.form().bindFromRequest();

            BundleAnswer answer = bundleSubmissionService.createBundleAnswerFromNewSubmission(dynamicForm, SessionControllerUtils.getCurrentStatementLanguage());
            String submissionJid = bundleSubmissionService.submit(sessionProblem.getProblemJid(), courseSession.getSessionJid(), answer, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
            bundleSubmissionService.storeSubmissionFiles(bundleSubmissionLocalFileProvider, bundleSubmissionRemoteFileProvider, submissionJid, answer);

            return redirect(routes.TrainingBundleSubmissionController.viewSubmissions(curriculum.getId(), curriculumCourse.getId(), courseSession.getId()));
        } else {
            return notFound();
        }
    }

    @Transactional(readOnly = true)
    public Result viewSubmissions(long curriculumId, long curriculumCourseId, long courseSessionId) throws CurriculumNotFoundException, CurriculumCourseNotFoundException, CourseNotFoundException, CourseSessionNotFoundException, SessionNotFoundException {
        return listSubmissions(curriculumId, curriculumCourseId, courseSessionId, 0, "id", "desc", null);
    }

    @Transactional(readOnly = true)
    public Result listSubmissions(long curriculumId, long curriculumCourseId, long courseSessionId, long pageIndex, String orderBy, String orderDir, String problemJid) throws CurriculumNotFoundException, CurriculumCourseNotFoundException, CourseNotFoundException, CourseSessionNotFoundException, SessionNotFoundException {
        Curriculum curriculum = curriculumService.findCurriculumByCurriculumId(curriculumId);
        CurriculumCourse curriculumCourse = curriculumCourseService.findCurriculumCourseByCurriculumCourseId(curriculumCourseId);
        CourseSession courseSession = courseSessionService.findByCourseSessionId(courseSessionId);

        if ((curriculum.getJid().equals(curriculumCourse.getCurriculumJid())) && (curriculumCourse.getCourseJid().equals(courseSession.getCourseJid()))) {
            Course course = courseService.findCourseByCourseJid(curriculumCourse.getCourseJid());
            Session session = sessionService.findSessionBySessionJid(courseSession.getSessionJid());

            String actualProblemJid = "(none)".equals(problemJid) ? null : problemJid;

            Page<BundleSubmission> bundleSubmissions = bundleSubmissionService.pageSubmissions(pageIndex, PAGE_SIZE, orderBy, orderDir, IdentityUtils.getUserJid(), actualProblemJid, session.getJid());
            Map<String, String> problemJidToAliasMap = sessionProblemService.findBundleProblemJidToAliasMapBySessionJid(session.getJid());

            LazyHtml content = new LazyHtml(listSubmissionsView.render(curriculum.getId(), curriculumCourse.getId(), courseSession.getId(), bundleSubmissions, problemJidToAliasMap, pageIndex, orderBy, orderDir, actualProblemJid));
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
                  new InternalLink(Messages.get("training.submissions.bundle"), routes.TrainingController.jumpToBundleSubmissions(curriculum.getId(), curriculumCourse.getId(), courseSession.getId()))
            ));
            ControllerUtils.getInstance().appendTemplateLayout(content, "Sessions - Programming BundleSubmissions");

            return ControllerUtils.getInstance().lazyOk(content);
        } else {
            return notFound();
        }
    }

    @Transactional(readOnly = true)
    public Result viewSubmission(long curriculumId, long curriculumCourseId, long courseSessionId, long bundleSubmissionId) throws CurriculumNotFoundException, CurriculumCourseNotFoundException, CourseNotFoundException, CourseSessionNotFoundException, SessionNotFoundException, SessionProblemNotFoundException, BundleSubmissionNotFoundException {
        Curriculum curriculum = curriculumService.findCurriculumByCurriculumId(curriculumId);
        CurriculumCourse curriculumCourse = curriculumCourseService.findCurriculumCourseByCurriculumCourseId(curriculumCourseId);
        CourseSession courseSession = courseSessionService.findByCourseSessionId(courseSessionId);

        if ((curriculum.getJid().equals(curriculumCourse.getCurriculumJid())) && (curriculumCourse.getCourseJid().equals(courseSession.getCourseJid()))) {
            Course course = courseService.findCourseByCourseJid(curriculumCourse.getCourseJid());
            Session session = sessionService.findSessionBySessionJid(courseSession.getSessionJid());

            BundleSubmission bundleSubmission = bundleSubmissionService.findSubmissionById(bundleSubmissionId);
            try {
                BundleAnswer answer = bundleSubmissionService.createBundleAnswerFromPastSubmission(bundleSubmissionLocalFileProvider, bundleSubmissionRemoteFileProvider, bundleSubmission.getJid());
                SessionProblem sessionProblem = sessionProblemService.findSessionProblemBySessionJidAndProblemJid(session.getJid(), bundleSubmission.getProblemJid());
                String sessionProblemAlias = sessionProblem.getAlias();
                String sessionProblemName = JidCacheService.getInstance().getDisplayName(sessionProblem.getProblemJid());

                LazyHtml content = new LazyHtml(bundleSubmissionView.render(bundleSubmission, new Gson().fromJson(bundleSubmission.getLatestDetails(), new TypeToken<Map<String, Double>>() {}.getType()), answer, JidCacheService.getInstance().getDisplayName(bundleSubmission.getAuthorJid()), sessionProblemAlias, sessionProblemName, session.getName()));
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
                      new InternalLink(Messages.get("training.submissions.bundle"), routes.TrainingController.jumpToBundleSubmissions(curriculum.getId(), curriculumCourse.getId(), courseSession.getId())),
                      new InternalLink(bundleSubmission.getId() + "", routes.TrainingBundleSubmissionController.viewSubmission(curriculum.getId(), curriculumCourse.getId(), courseSession.getId(), bundleSubmission.getId()))
                ));
                ControllerUtils.getInstance().appendTemplateLayout(content, "Sessions - Programming BundleSubmissions - View");

                return ControllerUtils.getInstance().lazyOk(content);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return notFound();
        }
    }
}
