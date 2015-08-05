package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
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
@Authorized(value = {"admin"})
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
        Course course = courseService.findCourseByCourseId(courseId);

        Page<CourseSession> coursePage = courseSessionService.findCourseSessions(course.getJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);
        Form<CourseSessionCreateForm> form = Form.form(CourseSessionCreateForm.class);

        return showListCreateSessions(course, form, coursePage, orderBy, orderDir, filterString);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postCreateSession(long courseId, long page, String orderBy, String orderDir, String filterString) throws CourseNotFoundException {
        Course course = courseService.findCourseByCourseId(courseId);
        Form<CourseSessionCreateForm> form = Form.form(CourseSessionCreateForm.class).bindFromRequest();

        if (form.hasErrors() || form.hasGlobalErrors()) {
            Page<CourseSession> coursePage = courseSessionService.findCourseSessions(course.getJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);

            return showListCreateSessions(course, form, coursePage, orderBy, orderDir, filterString);
        } else {
            CourseSessionCreateForm data = form.get();
            if (sessionService.existBySessionJid(data.sessionJid)) {
                if (courseSessionService.existByCourseJidAndAlias(course.getJid(), data.alias)) {
                    form.reject(Messages.get("error.course.session.duplicateAlias"));
                    Page<CourseSession> coursePage = courseSessionService.findCourseSessions(course.getJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);

                    return showListCreateSessions(course, form, coursePage, orderBy, orderDir, filterString);
                } else if (!courseSessionService.existByCourseJidAndSessionJid(course.getJid(), data.sessionJid)) {
                    courseSessionService.addCourseSession(course.getJid(), data.sessionJid, data.alias, data.completeable);

                    return redirect(routes.CourseSessionController.viewSessions(course.getId()));
                } else {
                    form.reject(Messages.get("error.course.sessionExist"));
                    Page<CourseSession> coursePage = courseSessionService.findCourseSessions(course.getJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);

                    return showListCreateSessions(course, form, coursePage, orderBy, orderDir, filterString);
                }
            } else {
                form.reject(Messages.get("error.course.invalidJid"));
                Page<CourseSession> coursePage = courseSessionService.findCourseSessions(course.getJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);

                return showListCreateSessions(course, form, coursePage, orderBy, orderDir, filterString);
            }
        }
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result updateSession(long courseId, long courseSessionId) throws CourseNotFoundException, CourseSessionNotFoundException {
        Course course = courseService.findCourseByCourseId(courseId);
        CourseSession courseSession = courseSessionService.findByCourseSessionId(courseSessionId);

        if (courseSession.getCourseJid().equals(course.getJid())) {
            CourseSessionUpdateForm courseSessionUpdateForm = new CourseSessionUpdateForm();
            courseSessionUpdateForm.alias = courseSession.getAlias();
            courseSessionUpdateForm.completeable = courseSession.isCompleteable();
            Form<CourseSessionUpdateForm> form = Form.form(CourseSessionUpdateForm.class).fill(courseSessionUpdateForm);

            return showUpdateSession(course, courseSession, form);
        } else {
            return notFound();
        }
    }

    @Transactional
    @RequireCSRFCheck
    public Result postUpdateSession(long courseId, long courseSessionId) throws CourseNotFoundException, CourseSessionNotFoundException {
        Course course = courseService.findCourseByCourseId(courseId);
        CourseSession courseSession = courseSessionService.findByCourseSessionId(courseSessionId);

        if (courseSession.getCourseJid().equals(course.getJid())) {
            Form<CourseSessionUpdateForm> form = Form.form(CourseSessionUpdateForm.class).bindFromRequest();
            if (!((form.hasErrors()) || (form.hasGlobalErrors()))) {
                CourseSessionUpdateForm courseSessionUpdateForm = form.get();
                if ((courseSessionUpdateForm.alias.equals(courseSession.getAlias())) || (!courseSessionService.existByCourseJidAndAlias(course.getJid(), courseSessionUpdateForm.alias))) {
                    courseSessionService.updateCourseSession(courseSession.getId(), courseSessionUpdateForm.alias, courseSessionUpdateForm.completeable);

                    return redirect(routes.CourseSessionController.viewSessions(course.getId()));
                } else {
                    form.reject(Messages.get("error.course.session.duplicateAlias"));
                    return showUpdateSession(course, courseSession, form);
                }
            } else {
                return showUpdateSession(course, courseSession, form);
            }
        } else {
            return notFound();
        }
    }

    @Transactional
    public Result deleteSession(long courseId, long courseSessionId) throws CourseNotFoundException, CourseSessionNotFoundException {
        Course course = courseService.findCourseByCourseId(courseId);
        CourseSession courseSession = courseSessionService.findByCourseSessionId(courseSessionId);

        if (course.getJid().equals(courseSession.getCourseJid())) {
            courseSessionService.removeCourseSession(courseSessionId);

            return redirect(routes.CourseSessionController.viewSessions(course.getId()));
        } else {
            return forbidden();
        }
    }

    private Result showListCreateSessions(Course course, Form<CourseSessionCreateForm> form, Page<CourseSession> currentPage, String orderBy, String orderDir, String filterString) {
        LazyHtml content = new LazyHtml(listCreateCourseSessionsView.render(course.getId(), currentPage, orderBy, orderDir, filterString, form));
        CourseControllerUtils.appendUpdateLayout(content, course);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("course.courses"), routes.CourseController.viewCourses()),
              new InternalLink(Messages.get("course.sessions"), routes.CourseController.jumpToSessions(course.getId())),
              new InternalLink(Messages.get("commons.view"), routes.CourseSessionController.viewSessions(course.getId()))
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Courses");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    private Result showUpdateSession(Course course, CourseSession courseSession, Form<CourseSessionUpdateForm> form) {
        LazyHtml content = new LazyHtml(updateCourseSessionView.render(form, course.getId(), courseSession.getId()));
        CourseControllerUtils.appendUpdateLayout(content, course);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("course.courses"), routes.CourseController.viewCourses()),
              new InternalLink(Messages.get("course.sessions"), routes.CourseController.jumpToSessions(course.getId())),
              new InternalLink(Messages.get("commons.view"), routes.CourseSessionController.viewSessions(course.getId())),
              new InternalLink(Messages.get("commons.update"), routes.CourseSessionController.updateSession(course.getId(), courseSession.getId()))
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Courses");

        return ControllerUtils.getInstance().lazyOk(content);
    }
}
