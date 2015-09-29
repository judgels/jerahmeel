package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.jerahmeel.Course;
import org.iatoki.judgels.jerahmeel.CourseNotFoundException;
import org.iatoki.judgels.jerahmeel.CourseSession;
import org.iatoki.judgels.jerahmeel.CourseSessionNotFoundException;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authorized;
import org.iatoki.judgels.jerahmeel.controllers.securities.HasRole;
import org.iatoki.judgels.jerahmeel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jerahmeel.forms.CourseSessionAddForm;
import org.iatoki.judgels.jerahmeel.forms.CourseSessionEditForm;
import org.iatoki.judgels.jerahmeel.services.CourseService;
import org.iatoki.judgels.jerahmeel.services.CourseSessionService;
import org.iatoki.judgels.jerahmeel.services.SessionService;
import org.iatoki.judgels.jerahmeel.views.html.course.session.editCourseSessionView;
import org.iatoki.judgels.jerahmeel.views.html.course.session.listAddCourseSessionsView;
import org.iatoki.judgels.jophiel.BasicActivityKeys;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
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
    private static final String SESSION = "session";
    private static final String COURSE = "course";

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
        return listAddSessions(courseId, 0, "id", "asc", "");
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result listAddSessions(long courseId, long page, String orderBy, String orderDir, String filterString) throws CourseNotFoundException {
        Course course = courseService.findCourseById(courseId);

        Page<CourseSession> pageOfCourseSessions = courseSessionService.getPageOfCourseSessions(course.getJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);
        Form<CourseSessionAddForm> courseSessionAddForm = Form.form(CourseSessionAddForm.class);

        return showListAddSessions(course, courseSessionAddForm, pageOfCourseSessions, orderBy, orderDir, filterString);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postAddSession(long courseId, long page, String orderBy, String orderDir, String filterString) throws CourseNotFoundException {
        Course course = courseService.findCourseById(courseId);
        Form<CourseSessionAddForm> courseSessionCreateForm = Form.form(CourseSessionAddForm.class).bindFromRequest();

        if (formHasErrors(courseSessionCreateForm)) {
            Page<CourseSession> pageOfCourseSessions = courseSessionService.getPageOfCourseSessions(course.getJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);

            return showListAddSessions(course, courseSessionCreateForm, pageOfCourseSessions, orderBy, orderDir, filterString);
        }

        CourseSessionAddForm courseSessionCreateData = courseSessionCreateForm.get();
        if (!sessionService.sessionExistsByJid(courseSessionCreateData.sessionJid)) {
            courseSessionCreateForm.reject(Messages.get("error.course.invalidJid"));
            Page<CourseSession> pageOfCourseSessions = courseSessionService.getPageOfCourseSessions(course.getJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);

            return showListAddSessions(course, courseSessionCreateForm, pageOfCourseSessions, orderBy, orderDir, filterString);
        }

        if (courseSessionService.existsByCourseJidAndAlias(course.getJid(), courseSessionCreateData.alias)) {
            courseSessionCreateForm.reject(Messages.get("error.course.session.duplicateAlias"));
            Page<CourseSession> pageOfCourseSessions = courseSessionService.getPageOfCourseSessions(course.getJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);

            return showListAddSessions(course, courseSessionCreateForm, pageOfCourseSessions, orderBy, orderDir, filterString);
        }

        if (courseSessionService.existsByCourseJidAndSessionJid(course.getJid(), courseSessionCreateData.sessionJid)) {
            courseSessionCreateForm.reject(Messages.get("error.course.sessionExist"));
            Page<CourseSession> pageOfCourseSessions = courseSessionService.getPageOfCourseSessions(course.getJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);

            return showListAddSessions(course, courseSessionCreateForm, pageOfCourseSessions, orderBy, orderDir, filterString);
        }

        CourseSession courseSession = courseSessionService.addCourseSession(course.getJid(), courseSessionCreateData.sessionJid, courseSessionCreateData.alias, courseSessionCreateData.completeable, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        JerahmeelControllerUtils.getInstance().addActivityLog(BasicActivityKeys.ADD_IN.construct(COURSE, course.getJid(), course.getName(), SESSION, courseSession.getSessionJid(), courseSession.getSessionName()));

        return redirect(routes.CourseSessionController.viewSessions(course.getId()));
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result editSession(long courseId, long courseSessionId) throws CourseNotFoundException, CourseSessionNotFoundException {
        Course course = courseService.findCourseById(courseId);
        CourseSession courseSession = courseSessionService.findCourseSessionById(courseSessionId);

        if (!courseSession.getCourseJid().equals(course.getJid())) {
            return notFound();
        }

        CourseSessionEditForm courseSessionEditData = new CourseSessionEditForm();
        courseSessionEditData.alias = courseSession.getAlias();
        courseSessionEditData.completeable = courseSession.isCompleteable();
        Form<CourseSessionEditForm> courseSessionEditForm = Form.form(CourseSessionEditForm.class).fill(courseSessionEditData);

        return showEditSession(course, courseSession, courseSessionEditForm);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postEditSession(long courseId, long courseSessionId) throws CourseNotFoundException, CourseSessionNotFoundException {
        Course course = courseService.findCourseById(courseId);
        CourseSession courseSession = courseSessionService.findCourseSessionById(courseSessionId);

        if (!courseSession.getCourseJid().equals(course.getJid())) {
            return notFound();
        }

        Form<CourseSessionEditForm> courseSessionEditForm = Form.form(CourseSessionEditForm.class).bindFromRequest();
        if (formHasErrors(courseSessionEditForm)) {
            return showEditSession(course, courseSession, courseSessionEditForm);
        }

        CourseSessionEditForm courseSessionEditData = courseSessionEditForm.get();
        if (!courseSessionEditData.alias.equals(courseSession.getAlias()) && courseSessionService.existsByCourseJidAndAlias(course.getJid(), courseSessionEditData.alias)) {
            courseSessionEditForm.reject(Messages.get("error.course.session.duplicateAlias"));
            return showEditSession(course, courseSession, courseSessionEditForm);
        }

        courseSessionService.updateCourseSession(courseSession.getId(), courseSessionEditData.alias, courseSessionEditData.completeable, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        JerahmeelControllerUtils.getInstance().addActivityLog(BasicActivityKeys.EDIT_IN.construct(COURSE, course.getJid(), course.getName(), SESSION, courseSession.getSessionJid(), courseSession.getSessionName()));

        return redirect(routes.CourseSessionController.viewSessions(course.getId()));
    }

    @Transactional
    public Result removeSession(long courseId, long courseSessionId) throws CourseNotFoundException, CourseSessionNotFoundException {
        Course course = courseService.findCourseById(courseId);
        CourseSession courseSession = courseSessionService.findCourseSessionById(courseSessionId);

        if (!course.getJid().equals(courseSession.getCourseJid())) {
            return forbidden();
        }

        courseSessionService.removeCourseSession(courseSessionId);

        JerahmeelControllerUtils.getInstance().addActivityLog(BasicActivityKeys.REMOVE_FROM.construct(COURSE, course.getJid(), course.getName(), SESSION, courseSession.getSessionJid(), courseSession.getSessionName()));

        return redirect(routes.CourseSessionController.viewSessions(course.getId()));
    }

    private Result showListAddSessions(Course course, Form<CourseSessionAddForm> courseSessionAddForm, Page<CourseSession> pageOfCourseSessions, String orderBy, String orderDir, String filterString) {
        LazyHtml content = new LazyHtml(listAddCourseSessionsView.render(course.getId(), pageOfCourseSessions, orderBy, orderDir, filterString, courseSessionAddForm));
        CourseControllerUtils.appendUpdateLayout(content, course);
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, course,
                new InternalLink(Messages.get("commons.view"), routes.CourseSessionController.viewSessions(course.getId()))
        );
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Courses");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    private Result showEditSession(Course course, CourseSession courseSession, Form<CourseSessionEditForm> courseSessionEditForm) {
        LazyHtml content = new LazyHtml(editCourseSessionView.render(courseSessionEditForm, course.getId(), courseSession.getId()));
        CourseControllerUtils.appendUpdateLayout(content, course);
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, course,
                new InternalLink(Messages.get("commons.update"), routes.CourseSessionController.editSession(course.getId(), courseSession.getId()))
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
