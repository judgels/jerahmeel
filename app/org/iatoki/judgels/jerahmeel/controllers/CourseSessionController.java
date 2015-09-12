package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.jerahmeel.Course;
import org.iatoki.judgels.jerahmeel.CourseNotFoundException;
import org.iatoki.judgels.jerahmeel.services.CourseService;
import org.iatoki.judgels.jerahmeel.CourseSession;
import org.iatoki.judgels.jerahmeel.forms.CourseSessionCreateForm;
import org.iatoki.judgels.jerahmeel.CourseSessionNotFoundException;
import org.iatoki.judgels.jerahmeel.services.CourseSessionService;
import org.iatoki.judgels.jerahmeel.forms.CourseSessionUpdateForm;
import org.iatoki.judgels.jerahmeel.services.SessionService;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authorized;
import org.iatoki.judgels.jerahmeel.controllers.securities.HasRole;
import org.iatoki.judgels.jerahmeel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jerahmeel.views.html.course.session.listCreateCourseSessionsView;
import org.iatoki.judgels.jerahmeel.views.html.course.session.updateCourseSessionView;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Authenticated(value = {LoggedIn.class, HasRole.class})
@Authorized(value = "admin")
@Singleton
@Named
public final class CourseSessionController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;

    private final CourseService courseService;
    private final CourseSessionService courseSessionService;
    private final SessionService sessionService;

    @Inject
    public CourseSessionController(CourseService courseService, CourseSessionService courseSessionService, SessionService sessionService) {
        this.courseService = courseService;
        this.courseSessionService = courseSessionService;
        this.sessionService = sessionService;
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result viewSessions(long courseId) throws CourseNotFoundException {
        return listCreateSessions(courseId, 0, "id", "asc", "");
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result listCreateSessions(long courseId, long page, String orderBy, String orderDir, String filterString) throws CourseNotFoundException {
        Course course = courseService.findCourseById(courseId);

        Page<CourseSession> pageOfCourseSessions = courseSessionService.getPageOfCourseSessions(course.getJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);
        Form<CourseSessionCreateForm> courseSessionCreateForm = Form.form(CourseSessionCreateForm.class);

        return showListCreateSessions(course, courseSessionCreateForm, pageOfCourseSessions, orderBy, orderDir, filterString);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postCreateSession(long courseId, long page, String orderBy, String orderDir, String filterString) throws CourseNotFoundException {
        Course course = courseService.findCourseById(courseId);
        Form<CourseSessionCreateForm> courseSessionCreateForm = Form.form(CourseSessionCreateForm.class).bindFromRequest();

        if (formHasErrors(courseSessionCreateForm)) {
            Page<CourseSession> pageOfCourseSessions = courseSessionService.getPageOfCourseSessions(course.getJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);

            return showListCreateSessions(course, courseSessionCreateForm, pageOfCourseSessions, orderBy, orderDir, filterString);
        }

        CourseSessionCreateForm courseSessionCreateData = courseSessionCreateForm.get();
        if (!sessionService.sessionExistsByJid(courseSessionCreateData.sessionJid)) {
            courseSessionCreateForm.reject(Messages.get("error.course.invalidJid"));
            Page<CourseSession> pageOfCourseSessions = courseSessionService.getPageOfCourseSessions(course.getJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);

            return showListCreateSessions(course, courseSessionCreateForm, pageOfCourseSessions, orderBy, orderDir, filterString);
        }

        if (courseSessionService.existsByCourseJidAndAlias(course.getJid(), courseSessionCreateData.alias)) {
            courseSessionCreateForm.reject(Messages.get("error.course.session.duplicateAlias"));
            Page<CourseSession> pageOfCourseSessions = courseSessionService.getPageOfCourseSessions(course.getJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);

            return showListCreateSessions(course, courseSessionCreateForm, pageOfCourseSessions, orderBy, orderDir, filterString);
        }

        if (courseSessionService.existsByCourseJidAndSessionJid(course.getJid(), courseSessionCreateData.sessionJid)) {
            courseSessionCreateForm.reject(Messages.get("error.course.sessionExist"));
            Page<CourseSession> pageOfCourseSessions = courseSessionService.getPageOfCourseSessions(course.getJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);

            return showListCreateSessions(course, courseSessionCreateForm, pageOfCourseSessions, orderBy, orderDir, filterString);
        }

        courseSessionService.addCourseSession(course.getJid(), courseSessionCreateData.sessionJid, courseSessionCreateData.alias, courseSessionCreateData.completeable, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        return redirect(routes.CourseSessionController.viewSessions(course.getId()));
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result updateSession(long courseId, long courseSessionId) throws CourseNotFoundException, CourseSessionNotFoundException {
        Course course = courseService.findCourseById(courseId);
        CourseSession courseSession = courseSessionService.findCourseSessionById(courseSessionId);

        if (!courseSession.getCourseJid().equals(course.getJid())) {
            return notFound();
        }

        CourseSessionUpdateForm courseSessionUpdateData = new CourseSessionUpdateForm();
        courseSessionUpdateData.alias = courseSession.getAlias();
        courseSessionUpdateData.completeable = courseSession.isCompleteable();
        Form<CourseSessionUpdateForm> courseSessionUpdateForm = Form.form(CourseSessionUpdateForm.class).fill(courseSessionUpdateData);

        return showUpdateSession(course, courseSession, courseSessionUpdateForm);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postUpdateSession(long courseId, long courseSessionId) throws CourseNotFoundException, CourseSessionNotFoundException {
        Course course = courseService.findCourseById(courseId);
        CourseSession courseSession = courseSessionService.findCourseSessionById(courseSessionId);

        if (!courseSession.getCourseJid().equals(course.getJid())) {
            return notFound();
        }

        Form<CourseSessionUpdateForm> courseSessionUpdateForm = Form.form(CourseSessionUpdateForm.class).bindFromRequest();
        if (formHasErrors(courseSessionUpdateForm)) {
            return showUpdateSession(course, courseSession, courseSessionUpdateForm);
        }

        CourseSessionUpdateForm courseSessionUpdateData = courseSessionUpdateForm.get();
        if (!courseSessionUpdateData.alias.equals(courseSession.getAlias()) && courseSessionService.existsByCourseJidAndAlias(course.getJid(), courseSessionUpdateData.alias)) {
            courseSessionUpdateForm.reject(Messages.get("error.course.session.duplicateAlias"));
            return showUpdateSession(course, courseSession, courseSessionUpdateForm);
        }

        courseSessionService.updateCourseSession(courseSession.getId(), courseSessionUpdateData.alias, courseSessionUpdateData.completeable, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        return redirect(routes.CourseSessionController.viewSessions(course.getId()));
    }

    @Transactional
    public Result deleteSession(long courseId, long courseSessionId) throws CourseNotFoundException, CourseSessionNotFoundException {
        Course course = courseService.findCourseById(courseId);
        CourseSession courseSession = courseSessionService.findCourseSessionById(courseSessionId);

        if (!course.getJid().equals(courseSession.getCourseJid())) {
            return forbidden();
        }

        courseSessionService.removeCourseSession(courseSessionId);

        return redirect(routes.CourseSessionController.viewSessions(course.getId()));
    }

    private Result showListCreateSessions(Course course, Form<CourseSessionCreateForm> courseSessionCreateForm, Page<CourseSession> pageOfCourseSessions, String orderBy, String orderDir, String filterString) {
        LazyHtml content = new LazyHtml(listCreateCourseSessionsView.render(course.getId(), pageOfCourseSessions, orderBy, orderDir, filterString, courseSessionCreateForm));
        CourseControllerUtils.appendUpdateLayout(content, course);
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, course,
                new InternalLink(Messages.get("commons.view"), routes.CourseSessionController.viewSessions(course.getId()))
        );
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Courses");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    private Result showUpdateSession(Course course, CourseSession courseSession, Form<CourseSessionUpdateForm> courseSessionUpdateForm) {
        LazyHtml content = new LazyHtml(updateCourseSessionView.render(courseSessionUpdateForm, course.getId(), courseSession.getId()));
        CourseControllerUtils.appendUpdateLayout(content, course);
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, course,
                new InternalLink(Messages.get("commons.update"), routes.CourseSessionController.updateSession(course.getId(), courseSession.getId()))
        );
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Courses");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    private void appendBreadcrumbsLayout(LazyHtml content, Course course, InternalLink... lastLinks) {
        ImmutableList.Builder<InternalLink> breadcrumbsBuilder = CourseControllerUtils.getBreadcrumbsBuilder();
        breadcrumbsBuilder.add(new InternalLink(Messages.get("course.sessions"), routes.CourseController.jumpToSessions(course.getId())));
        breadcrumbsBuilder.add(new InternalLink(Messages.get("commons.view"), routes.CourseSessionController.viewSessions(course.getId())));
        breadcrumbsBuilder.add(lastLinks);

        JerahmeelControllerUtils.getInstance().appendBreadcrumbsLayout(content, breadcrumbsBuilder.build());
    }
}
