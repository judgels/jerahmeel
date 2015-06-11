package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.commons.controllers.BaseController;
import org.iatoki.judgels.jerahmeel.Course;
import org.iatoki.judgels.jerahmeel.CourseNotFoundException;
import org.iatoki.judgels.jerahmeel.CourseService;
import org.iatoki.judgels.jerahmeel.CourseSession;
import org.iatoki.judgels.jerahmeel.CourseSessionCreateForm;
import org.iatoki.judgels.jerahmeel.CourseSessionNotFoundException;
import org.iatoki.judgels.jerahmeel.CourseSessionService;
import org.iatoki.judgels.jerahmeel.Session;
import org.iatoki.judgels.jerahmeel.SessionService;
import org.iatoki.judgels.jerahmeel.controllers.security.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.security.Authorized;
import org.iatoki.judgels.jerahmeel.controllers.security.HasRole;
import org.iatoki.judgels.jerahmeel.controllers.security.LoggedIn;
import org.iatoki.judgels.jerahmeel.views.html.course.session.listCreateCourseSessionsView;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Result;

@Transactional
@Authenticated(value = {LoggedIn.class, HasRole.class})
@Authorized(value = {"admin"})
public final class CourseSessionController extends BaseController {

    private static final long PAGE_SIZE = 20;

    private final CourseService courseService;
    private final CourseSessionService courseSessionService;
    private final SessionService sessionService;

    public CourseSessionController(CourseService courseService, CourseSessionService courseSessionService, SessionService sessionService) {
        this.courseService = courseService;
        this.courseSessionService = courseSessionService;
        this.sessionService = sessionService;
    }

    @AddCSRFToken
    public Result viewSessions(long courseId) throws CourseNotFoundException {
        return listCreateSessions(courseId, 0, "id", "asc", "");
    }

    @AddCSRFToken
    public Result listCreateSessions(long courseId, long page, String orderBy, String orderDir, String filterString) throws CourseNotFoundException {
        Course course = courseService.findCourseByCourseId(courseId);

        Page<CourseSession> coursePage = courseSessionService.findCourseSessions(course.getJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);
        Form<CourseSessionCreateForm> form = Form.form(CourseSessionCreateForm.class);

        return showListCreateSessions(course, form, coursePage, orderBy, orderDir, filterString);
    }

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
                if (!courseSessionService.existByCourseJidAndSessionJid(course.getJid(), data.sessionJid)) {
                    courseSessionService.addCourseSession(course.getJid(), data.sessionJid, data.completeable);

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

    public Result jumpToSession(long courseId, long courseSessionId) throws CourseNotFoundException, CourseSessionNotFoundException {
        Course course = courseService.findCourseByCourseId(courseId);
        CourseSession courseSession = courseSessionService.findByCourseSessionId(courseSessionId);

        if (course.getJid().equals(courseSession.getCourseJid())) {
            Session session = sessionService.findSessionBySessionJid(courseSession.getSessionJid());

            return redirect(routes.SessionController.updateSessionGeneral(session.getId()));
        } else {
            return notFound();
        }
    }

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
              new InternalLink(Messages.get("commons.update"), routes.CourseSessionController.viewSessions(course.getId()))
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Courses");

        return ControllerUtils.getInstance().lazyOk(content);
    }
}
