package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.commons.controllers.BaseController;
import org.iatoki.judgels.jerahmeel.Course;
import org.iatoki.judgels.jerahmeel.CourseNotFoundException;
import org.iatoki.judgels.jerahmeel.CourseService;
import org.iatoki.judgels.jerahmeel.CourseSession;
import org.iatoki.judgels.jerahmeel.CourseSessionNotFoundException;
import org.iatoki.judgels.jerahmeel.CourseSessionService;
import org.iatoki.judgels.jerahmeel.Curriculum;
import org.iatoki.judgels.jerahmeel.CurriculumCourse;
import org.iatoki.judgels.jerahmeel.CurriculumCourseNotFoundException;
import org.iatoki.judgels.jerahmeel.CurriculumCourseService;
import org.iatoki.judgels.jerahmeel.CurriculumNotFoundException;
import org.iatoki.judgels.jerahmeel.CurriculumService;
import org.iatoki.judgels.jerahmeel.Session;
import org.iatoki.judgels.jerahmeel.SessionNotFoundException;
import org.iatoki.judgels.jerahmeel.SessionProblem;
import org.iatoki.judgels.jerahmeel.SessionProblemNotFoundException;
import org.iatoki.judgels.jerahmeel.SessionProblemProgress;
import org.iatoki.judgels.jerahmeel.SessionProblemService;
import org.iatoki.judgels.jerahmeel.SessionProblemType;
import org.iatoki.judgels.jerahmeel.SessionService;
import org.iatoki.judgels.jerahmeel.SessionSessionService;
import org.iatoki.judgels.jerahmeel.UserItemService;
import org.iatoki.judgels.jerahmeel.UserItemStatus;
import org.iatoki.judgels.jerahmeel.controllers.security.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.security.HasRole;
import org.iatoki.judgels.jerahmeel.controllers.security.LoggedIn;
import org.iatoki.judgels.jerahmeel.views.html.session.problem.viewProblemView;
import org.iatoki.judgels.jerahmeel.views.html.training.course.session.problem.listSessionProblemsView;
import org.iatoki.judgels.sandalphon.commons.Sandalphon;
import org.iatoki.judgels.sandalphon.commons.programming.LanguageRestriction;
import play.data.DynamicForm;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.mvc.Result;

import java.net.URI;

@Transactional
@Authenticated(value = {LoggedIn.class, HasRole.class})
public final class TrainingProblemController extends BaseController {
    private static final long PAGE_SIZE = 20;

    private final Sandalphon sandalphon;
    private final CurriculumService curriculumService;
    private final CurriculumCourseService curriculumCourseService;
    private final CourseService courseService;
    private final CourseSessionService courseSessionService;
    private final SessionService sessionService;
    private final SessionSessionService sessionSessionService;
    private final SessionProblemService sessionProblemService;
    private final UserItemService userItemService;

    public TrainingProblemController(Sandalphon sandalphon, CurriculumService curriculumService, CurriculumCourseService curriculumCourseService, CourseService courseService, CourseSessionService courseSessionService, SessionService sessionService, SessionSessionService sessionSessionService, SessionProblemService sessionProblemService, UserItemService userItemService) {
        this.sandalphon = sandalphon;
        this.curriculumService = curriculumService;
        this.curriculumCourseService = curriculumCourseService;
        this.courseService = courseService;
        this.courseSessionService = courseSessionService;
        this.sessionService = sessionService;
        this.sessionSessionService = sessionSessionService;
        this.sessionProblemService = sessionProblemService;
        this.userItemService = userItemService;
    }

    public Result viewProblems(long curriculumId, long curriculumCourseId, long courseSessionId) throws CurriculumNotFoundException, CurriculumCourseNotFoundException, CourseNotFoundException, CourseSessionNotFoundException, SessionNotFoundException {
        return listProblems(curriculumId, curriculumCourseId, courseSessionId, 0, "id", "asc", "");
    }

    public Result listProblems(long curriculumId, long curriculumCourseId, long courseSessionId, long page, String orderBy, String orderDir, String filterString) throws CurriculumNotFoundException, CurriculumCourseNotFoundException, CourseNotFoundException, CourseSessionNotFoundException, SessionNotFoundException {
        Curriculum curriculum = curriculumService.findCurriculumByCurriculumId(curriculumId);
        CurriculumCourse curriculumCourse = curriculumCourseService.findCurriculumCourseByCurriculumCourseId(curriculumCourseId);
        CourseSession courseSession = courseSessionService.findByCourseSessionId(courseSessionId);

        if ((curriculum.getJid().equals(curriculumCourse.getCurriculumJid())) && (curriculumCourse.getCourseJid().equals(courseSession.getCourseJid()))) {
            Course course = courseService.findCourseByCourseJid(curriculumCourse.getCourseJid());
            Session session = sessionService.findSessionBySessionJid(courseSession.getSessionJid());
            Page<SessionProblemProgress> sessionProblemPage = sessionProblemService.findSessionProblems(IdentityUtils.getUserJid(), courseSession.getSessionJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);

            if ((!userItemService.isUserItemExist(IdentityUtils.getUserJid(), session.getJid(), UserItemStatus.VIEWED)) && (sessionSessionService.isDependenciesFulfilled(IdentityUtils.getUserJid(), session.getJid()))) {
                userItemService.upsertUserItem(IdentityUtils.getUserJid(), session.getJid(), UserItemStatus.VIEWED);
            }

            return showListProblems(curriculum, curriculumCourse, course, courseSession, session, sessionProblemPage, orderBy, orderDir, filterString);
        } else {
            return notFound();
        }
    }

    public Result viewProblem(long curriculumId, long curriculumCourseId, long courseSessionId, long sessionProblemId) throws CurriculumNotFoundException, CurriculumCourseNotFoundException, CourseNotFoundException, CourseSessionNotFoundException, SessionNotFoundException, SessionProblemNotFoundException {
        Curriculum curriculum = curriculumService.findCurriculumByCurriculumId(curriculumId);
        CurriculumCourse curriculumCourse = curriculumCourseService.findCurriculumCourseByCurriculumCourseId(curriculumCourseId);
        CourseSession courseSession = courseSessionService.findByCourseSessionId(courseSessionId);
        SessionProblem sessionProblem = sessionProblemService.findSessionProblemBySessionProblemId(sessionProblemId);

        if ((curriculum.getJid().equals(curriculumCourse.getCurriculumJid())) && (curriculumCourse.getCourseJid().equals(courseSession.getCourseJid())) && (sessionProblem.getSessionJid().equals(courseSession.getSessionJid()))) {
            Course course = courseService.findCourseByCourseJid(curriculumCourse.getCourseJid());
            Session session = sessionService.findSessionBySessionJid(courseSession.getSessionJid());

            String reasonNotAllowedToSubmit = null;
            if (!sessionSessionService.isDependenciesFulfilled(IdentityUtils.getUserJid(), session.getJid())) {
                reasonNotAllowedToSubmit = Messages.get("training.session.isLocked");
            }
            String postSubmitUri = null;
            if (SessionProblemType.BUNDLE.equals(sessionProblem.getType())) {
                postSubmitUri = routes.TrainingBundleSubmissionController.postSubmitProblem(curriculum.getId(), curriculumCourse.getId(), courseSession.getId(), sessionProblem.getProblemJid()).absoluteURL(request(), request().secure());
            } else if (SessionProblemType.PROGRAMMING.equals(sessionProblem.getType())) {
                postSubmitUri = routes.TrainingProgrammingSubmissionController.postSubmitProblem(curriculum.getId(), curriculumCourse.getId(), courseSession.getId(), sessionProblem.getProblemJid()).absoluteURL(request(), request().secure());
            }

            int tOTPCode = sandalphon.calculateTOTPCode(sessionProblem.getProblemSecret(), System.currentTimeMillis());
            String requestUrl = sandalphon.getProblemTOTPEndpoint().toString();
            String requestBody = sandalphon.getProblemTOTPRequestBody(sessionProblem.getProblemJid(), tOTPCode, SessionControllerUtils.getCurrentStatementLanguage(), postSubmitUri, routes.TrainingProblemController.switchLanguage().absoluteURL(request(), request().secure()), reasonNotAllowedToSubmit, LanguageRestriction.defaultRestriction());

            LazyHtml content = new LazyHtml(viewProblemView.render(requestUrl, requestBody));
            SessionControllerUtils.appendViewLayout(content, curriculum, curriculumCourse, courseSession, session);
            ControllerUtils.getInstance().appendSidebarLayout(content);
            ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                  new InternalLink(Messages.get("training.curriculums"), routes.TrainingController.jumpToCurriculums()),
                  new InternalLink(curriculum.getName(), routes.TrainingController.jumpToCourses(curriculum.getId())),
                  new InternalLink(course.getName(), routes.TrainingController.jumpToSessions(curriculum.getId(), curriculumCourse.getId())),
                  new InternalLink(session.getName(), routes.TrainingController.jumpToProblems(curriculum.getId(), curriculumCourse.getId(), courseSession.getId())),
                  new InternalLink(sessionProblem.getAlias(), routes.TrainingProblemController.viewProblem(curriculum.getId(), curriculumCourse.getId(), courseSession.getId(), sessionProblem.getId()))
            ));

            if (!userItemService.isUserItemExist(IdentityUtils.getUserJid(), sessionProblem.getProblemJid(), UserItemStatus.VIEWED)) {
                userItemService.upsertUserItem(IdentityUtils.getUserJid(), sessionProblem.getProblemJid(), UserItemStatus.VIEWED);
            }

            ControllerUtils.getInstance().appendTemplateLayout(content, "Training");

            return ControllerUtils.getInstance().lazyOk(content);
        } else {
            return notFound();
        }
    }

    public Result switchLanguage() {
        String languageCode = DynamicForm.form().bindFromRequest().get("langCode");
        SessionControllerUtils.setCurrentStatementLanguage(languageCode);

        return redirect(request().getHeader("Referer"));
    }

    public Result renderProblemMedia(long curriculumId, long curriculumCourseId, long courseSessionId, long sessionProblemId, String filename) throws CurriculumNotFoundException, CurriculumCourseNotFoundException, CourseNotFoundException, CourseSessionNotFoundException, SessionNotFoundException, SessionProblemNotFoundException {
        Curriculum curriculum = curriculumService.findCurriculumByCurriculumId(curriculumId);
        CurriculumCourse curriculumCourse = curriculumCourseService.findCurriculumCourseByCurriculumCourseId(curriculumCourseId);
        CourseSession courseSession = courseSessionService.findByCourseSessionId(courseSessionId);
        SessionProblem sessionProblem = sessionProblemService.findSessionProblemBySessionProblemId(sessionProblemId);

        if ((curriculum.getJid().equals(curriculumCourse.getCurriculumJid())) && (curriculumCourse.getCourseJid().equals(courseSession.getCourseJid())) && (sessionProblem.getSessionJid().equals(courseSession.getSessionJid()))) {
            Course course = courseService.findCourseByCourseJid(curriculumCourse.getCourseJid());
            Session session = sessionService.findSessionBySessionJid(courseSession.getSessionJid());

            URI uri = sandalphon.getProblemRenderUri(sessionProblem.getProblemJid(), filename);

            return redirect(uri.toString());
        } else {
            return notFound();
        }
    }

    private Result showListProblems(Curriculum curriculum, CurriculumCourse curriculumCourse, Course course, CourseSession courseSession, Session session, Page<SessionProblemProgress> currentPage, String orderBy, String orderDir, String filterString) {
        LazyHtml content = new LazyHtml(listSessionProblemsView.render(curriculum.getId(), curriculumCourse.getId(), courseSession.getId(), currentPage, orderBy, orderDir, filterString));
        SessionControllerUtils.appendViewLayout(content, curriculum, curriculumCourse, courseSession, session);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("training.curriculums"), routes.TrainingController.jumpToCurriculums()),
              new InternalLink(curriculum.getName(), routes.TrainingController.jumpToCourses(curriculum.getId())),
              new InternalLink(course.getName(), routes.TrainingController.jumpToSessions(curriculum.getId(), curriculumCourse.getId())),
              new InternalLink(session.getName(), routes.TrainingController.jumpToProblems(curriculum.getId(), curriculumCourse.getId(), courseSession.getId()))
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Training");

        return ControllerUtils.getInstance().lazyOk(content);
    }
}
