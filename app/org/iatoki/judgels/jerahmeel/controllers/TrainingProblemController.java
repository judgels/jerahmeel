package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.api.sandalphon.SandalphonBundleProblemStatementRenderRequestParam;
import org.iatoki.judgels.api.sandalphon.SandalphonClientAPI;
import org.iatoki.judgels.api.sandalphon.SandalphonProgrammingProblemStatementRenderRequestParam;
import org.iatoki.judgels.api.sandalphon.SandalphonResourceDisplayNameUtils;
import org.iatoki.judgels.jerahmeel.Course;
import org.iatoki.judgels.jerahmeel.CourseNotFoundException;
import org.iatoki.judgels.jerahmeel.CourseSession;
import org.iatoki.judgels.jerahmeel.CourseSessionNotFoundException;
import org.iatoki.judgels.jerahmeel.Curriculum;
import org.iatoki.judgels.jerahmeel.CurriculumCourse;
import org.iatoki.judgels.jerahmeel.CurriculumCourseNotFoundException;
import org.iatoki.judgels.jerahmeel.CurriculumNotFoundException;
import org.iatoki.judgels.jerahmeel.JerahmeelUtils;
import org.iatoki.judgels.jerahmeel.Session;
import org.iatoki.judgels.jerahmeel.SessionNotFoundException;
import org.iatoki.judgels.jerahmeel.SessionProblem;
import org.iatoki.judgels.jerahmeel.SessionProblemNotFoundException;
import org.iatoki.judgels.jerahmeel.SessionProblemProgress;
import org.iatoki.judgels.jerahmeel.SessionProblemStatus;
import org.iatoki.judgels.jerahmeel.SessionProblemType;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.securities.GuestView;
import org.iatoki.judgels.jerahmeel.services.CourseService;
import org.iatoki.judgels.jerahmeel.services.CourseSessionService;
import org.iatoki.judgels.jerahmeel.services.CurriculumCourseService;
import org.iatoki.judgels.jerahmeel.services.CurriculumService;
import org.iatoki.judgels.jerahmeel.services.SessionDependencyService;
import org.iatoki.judgels.jerahmeel.services.SessionProblemService;
import org.iatoki.judgels.jerahmeel.services.SessionService;
import org.iatoki.judgels.jerahmeel.services.UserItemService;
import org.iatoki.judgels.jerahmeel.services.impls.JidCacheServiceImpl;
import org.iatoki.judgels.jerahmeel.views.html.training.course.session.problem.viewProblemView;
import org.iatoki.judgels.jerahmeel.views.html.training.course.session.problem.listSessionProblemsView;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import play.data.DynamicForm;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
@Named
public final class TrainingProblemController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;

    private final CourseService courseService;
    private final CourseSessionService courseSessionService;
    private final CurriculumCourseService curriculumCourseService;
    private final CurriculumService curriculumService;
    private final SandalphonClientAPI sandalphonClientAPI;
    private final SessionDependencyService sessionDependencyService;
    private final SessionProblemService sessionProblemService;
    private final SessionService sessionService;
    private final UserItemService userItemService;

    @Inject
    public TrainingProblemController(CourseService courseService, CourseSessionService courseSessionService, CurriculumCourseService curriculumCourseService, CurriculumService curriculumService, SandalphonClientAPI sandalphonClientAPI, SessionDependencyService sessionDependencyService, SessionProblemService sessionProblemService, SessionService sessionService, UserItemService userItemService) {
        this.courseService = courseService;
        this.courseSessionService = courseSessionService;
        this.curriculumCourseService = curriculumCourseService;
        this.curriculumService = curriculumService;
        this.sandalphonClientAPI = sandalphonClientAPI;
        this.sessionDependencyService = sessionDependencyService;
        this.sessionProblemService = sessionProblemService;
        this.sessionService = sessionService;
        this.userItemService = userItemService;
    }

    @Authenticated(value = GuestView.class)
    @Transactional(readOnly = true)
    public Result viewProblems(long curriculumId, long curriculumCourseId, long courseSessionId) throws CurriculumNotFoundException, CurriculumCourseNotFoundException, CourseNotFoundException, CourseSessionNotFoundException, SessionNotFoundException {
        return listProblems(curriculumId, curriculumCourseId, courseSessionId, 0, "alias", "asc", "");
    }

    @Authenticated(value = GuestView.class)
    @Transactional(readOnly = true)
    public Result listProblems(long curriculumId, long curriculumCourseId, long courseSessionId, long page, String orderBy, String orderDir, String filterString) throws CurriculumNotFoundException, CurriculumCourseNotFoundException, CourseNotFoundException, CourseSessionNotFoundException, SessionNotFoundException {
        Curriculum curriculum = curriculumService.findCurriculumById(curriculumId);
        CurriculumCourse curriculumCourse = curriculumCourseService.findCurriculumCourseByCurriculumCourseId(curriculumCourseId);
        CourseSession courseSession = courseSessionService.findCourseSessionById(courseSessionId);

        if (!curriculum.getJid().equals(curriculumCourse.getCurriculumJid()) || !curriculumCourse.getCourseJid().equals(courseSession.getCourseJid())) {
            return notFound();
        }

        Course course = courseService.findCourseByJid(curriculumCourse.getCourseJid());
        Session session = sessionService.findSessionByJid(courseSession.getSessionJid());
        Page<SessionProblemProgress> pageOfSessionProblemsProgress = sessionProblemService.getPageOfSessionProblemsProgress(IdentityUtils.getUserJid(), courseSession.getSessionJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);
        List<String> problemJids = pageOfSessionProblemsProgress.getData().stream().map(cp -> cp.getSessionProblem().getProblemJid()).collect(Collectors.toList());
        Map<String, String> problemTitlesMap = SandalphonResourceDisplayNameUtils.buildTitlesMap(JidCacheServiceImpl.getInstance().getDisplayNames(problemJids), SessionControllerUtils.getCurrentStatementLanguage());

        return showListProblems(curriculum, curriculumCourse, course, courseSession, session, pageOfSessionProblemsProgress, orderBy, orderDir, filterString, problemTitlesMap);
    }

    @Authenticated(value = GuestView.class)
    @Transactional(readOnly = true)
    public Result viewProblem(long curriculumId, long curriculumCourseId, long courseSessionId, long sessionProblemId) throws CurriculumNotFoundException, CurriculumCourseNotFoundException, CourseNotFoundException, CourseSessionNotFoundException, SessionNotFoundException, SessionProblemNotFoundException {
        Curriculum curriculum = curriculumService.findCurriculumById(curriculumId);
        CurriculumCourse curriculumCourse = curriculumCourseService.findCurriculumCourseByCurriculumCourseId(curriculumCourseId);
        CourseSession courseSession = courseSessionService.findCourseSessionById(courseSessionId);
        SessionProblem sessionProblem = sessionProblemService.findSessionProblemById(sessionProblemId);

        if ((sessionProblem.getStatus() != SessionProblemStatus.VISIBLE) || !curriculum.getJid().equals(curriculumCourse.getCurriculumJid()) || !curriculumCourse.getCourseJid().equals(courseSession.getCourseJid()) || !sessionProblem.getSessionJid().equals(courseSession.getSessionJid())) {
            return notFound();
        }

        Course course = courseService.findCourseByJid(curriculumCourse.getCourseJid());
        Session session = sessionService.findSessionByJid(courseSession.getSessionJid());

        String reasonNotAllowedToSubmit = null;
        if (JerahmeelUtils.isGuest()) {
            reasonNotAllowedToSubmit = Messages.get("training.session.mustLogin");
        } else if (!sessionDependencyService.isDependenciesFulfilled(IdentityUtils.getUserJid(), session.getJid())) {
            reasonNotAllowedToSubmit = Messages.get("training.session.isLocked");
        }

        String requestUrl;
        String requestBody;

        if (SessionProblemType.BUNDLE.equals(sessionProblem.getType())) {
            SandalphonBundleProblemStatementRenderRequestParam param = new SandalphonBundleProblemStatementRenderRequestParam();

            param.setProblemSecret(sessionProblem.getProblemSecret());
            param.setCurrentMillis(System.currentTimeMillis());
            param.setStatementLanguage(SessionControllerUtils.getCurrentStatementLanguage());
            param.setSwitchStatementLanguageUrl(routes.TrainingProblemController.switchLanguage().absoluteURL(request(), request().secure()));
            param.setPostSubmitUrl(routes.TrainingBundleSubmissionController.postSubmitProblem(curriculum.getId(), curriculumCourse.getId(), courseSession.getId(), sessionProblem.getProblemJid()).absoluteURL(request(), request().secure()));
            param.setReasonNotAllowedToSubmit(reasonNotAllowedToSubmit);

            requestUrl = sandalphonClientAPI.getBundleProblemStatementRenderAPIEndpoint(sessionProblem.getProblemJid());
            requestBody = sandalphonClientAPI.constructBundleProblemStatementRenderAPIRequestBody(sessionProblem.getProblemJid(), param);
        } else if (SessionProblemType.PROGRAMMING.equals(sessionProblem.getType())) {
            SandalphonProgrammingProblemStatementRenderRequestParam param = new SandalphonProgrammingProblemStatementRenderRequestParam();

            param.setProblemSecret(sessionProblem.getProblemSecret());
            param.setCurrentMillis(System.currentTimeMillis());
            param.setStatementLanguage(SessionControllerUtils.getCurrentStatementLanguage());
            param.setSwitchStatementLanguageUrl(routes.TrainingProblemController.switchLanguage().absoluteURL(request(), request().secure()));
            param.setPostSubmitUrl(routes.TrainingProgrammingSubmissionController.postSubmitProblem(curriculum.getId(), curriculumCourse.getId(), courseSession.getId(), sessionProblem.getProblemJid()).absoluteURL(request(), request().secure()));
            param.setReasonNotAllowedToSubmit(reasonNotAllowedToSubmit);
            param.setAllowedGradingLanguages("");

            requestUrl = sandalphonClientAPI.getProgrammingProblemStatementRenderAPIEndpoint(sessionProblem.getProblemJid());
            requestBody = sandalphonClientAPI.constructProgrammingProblemStatementRenderAPIRequestBody(sessionProblem.getProblemJid(), param);
        } else {
            throw new IllegalStateException();
        }

        session("problemJid", sessionProblem.getProblemJid());

        LazyHtml content = new LazyHtml(viewProblemView.render(requestUrl, requestBody, sessionProblem.getId()));
        SessionControllerUtils.appendViewLayout(content, curriculum, curriculumCourse, courseSession, session);
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, curriculum, curriculumCourse, course, courseSession, session,
                new InternalLink(sessionProblem.getAlias(), routes.TrainingProblemController.viewProblem(curriculum.getId(), curriculumCourse.getId(), courseSession.getId(), sessionProblem.getId()))
        );

        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Training");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    @Authenticated(value = GuestView.class)
    public Result switchLanguage() {
        String languageCode = DynamicForm.form().bindFromRequest().get("langCode");
        SessionControllerUtils.setCurrentStatementLanguage(languageCode);

        return redirect(request().getHeader("Referer"));
    }

    @Authenticated(value = GuestView.class)
    @Transactional(readOnly = true)
    public Result renderProblemMedia(long curriculumId, long curriculumCourseId, long courseSessionId, long sessionProblemId, String filename) throws CurriculumNotFoundException, CurriculumCourseNotFoundException, CourseNotFoundException, CourseSessionNotFoundException, SessionNotFoundException, SessionProblemNotFoundException {
        Curriculum curriculum = curriculumService.findCurriculumById(curriculumId);
        CurriculumCourse curriculumCourse = curriculumCourseService.findCurriculumCourseByCurriculumCourseId(curriculumCourseId);
        CourseSession courseSession = courseSessionService.findCourseSessionById(courseSessionId);
        SessionProblem sessionProblem = sessionProblemService.findSessionProblemById(sessionProblemId);

        if ((sessionProblem.getStatus() != SessionProblemStatus.VISIBLE) || !curriculum.getJid().equals(curriculumCourse.getCurriculumJid()) || !curriculumCourse.getCourseJid().equals(courseSession.getCourseJid()) || !sessionProblem.getSessionJid().equals(courseSession.getSessionJid())) {
            return notFound();
        }

        Course course = courseService.findCourseByJid(curriculumCourse.getCourseJid());
        Session session = sessionService.findSessionByJid(courseSession.getSessionJid());

        String mediaUrl = sandalphonClientAPI.getProblemStatementMediaRenderAPIEndpoint(sessionProblem.getProblemJid(), filename);

        return redirect(mediaUrl);
    }

    private Result showListProblems(Curriculum curriculum, CurriculumCourse curriculumCourse, Course course, CourseSession courseSession, Session session, Page<SessionProblemProgress> pageOfSessionProblemsProgress, String orderBy, String orderDir, String filterString, Map<String, String> problemTitlesMap) {
        LazyHtml content = new LazyHtml(listSessionProblemsView.render(curriculum, curriculumCourse, courseSession, pageOfSessionProblemsProgress, orderBy, orderDir, filterString, problemTitlesMap));
        SessionControllerUtils.appendViewLayout(content, curriculum, curriculumCourse, courseSession, session);
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, curriculum, curriculumCourse, course, courseSession, session);
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Training");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    private void appendBreadcrumbsLayout(LazyHtml content, Curriculum curriculum, CurriculumCourse curriculumCourse, Course course, CourseSession courseSession, Session session, InternalLink... lastLinks) {
        ImmutableList.Builder<InternalLink> breadcrumbsBuilder = TrainingControllerUtils.getBreadcrumbsBuilder();
        breadcrumbsBuilder.add(new InternalLink(curriculum.getName(), routes.TrainingController.jumpToCourses(curriculum.getId())));
        breadcrumbsBuilder.add(new InternalLink(course.getName(), routes.TrainingController.jumpToSessions(curriculum.getId(), curriculumCourse.getId())));
        breadcrumbsBuilder.add(new InternalLink(session.getName(), routes.TrainingController.jumpToProblems(curriculum.getId(), curriculumCourse.getId(), courseSession.getId())));
        breadcrumbsBuilder.add(lastLinks);

        JerahmeelControllerUtils.getInstance().appendBreadcrumbsLayout(content, breadcrumbsBuilder.build());
    }
}
