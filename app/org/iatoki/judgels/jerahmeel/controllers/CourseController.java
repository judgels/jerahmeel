package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.commons.controllers.BaseController;
import org.iatoki.judgels.commons.views.html.layouts.headingLayout;
import org.iatoki.judgels.commons.views.html.layouts.headingWithActionLayout;
import org.iatoki.judgels.jerahmeel.Course;
import org.iatoki.judgels.jerahmeel.CourseNotFoundException;
import org.iatoki.judgels.jerahmeel.CourseService;
import org.iatoki.judgels.jerahmeel.CourseUpsertForm;
import org.iatoki.judgels.jerahmeel.controllers.security.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.security.Authorized;
import org.iatoki.judgels.jerahmeel.controllers.security.HasRole;
import org.iatoki.judgels.jerahmeel.controllers.security.LoggedIn;
import org.iatoki.judgels.jerahmeel.views.html.course.createCourseView;
import org.iatoki.judgels.jerahmeel.views.html.course.listCoursesView;
import org.iatoki.judgels.jerahmeel.views.html.course.updateCourseGeneralView;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Result;

@Transactional
@Authenticated(value = {LoggedIn.class, HasRole.class})
@Authorized(value = "admin")
public final class CourseController extends BaseController {

    private static final long PAGE_SIZE = 20;

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    public Result viewCourses() {
        return listCourses(0, "id", "asc", "");
    }

    public Result listCourses(long page, String orderBy, String orderDir, String filterString) {
        Page<Course> currentPage = courseService.pageCourses(page, PAGE_SIZE, orderBy, orderDir, filterString);

        LazyHtml content = new LazyHtml(listCoursesView.render(currentPage, orderBy, orderDir, filterString));
        content.appendLayout(c -> headingWithActionLayout.render(Messages.get("course.list"), new InternalLink(Messages.get("commons.create"), routes.CourseController.createCourse()), c));
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("course.courses"), routes.CourseController.viewCourses())
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Courses");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    public Result jumpToSessions(long courseId) {
        return redirect(routes.CourseSessionController.viewSessions(courseId));
    }

    @AddCSRFToken
    public Result createCourse() {
        Form<CourseUpsertForm> form = Form.form(CourseUpsertForm.class);

        return showCreateCourse(form);
    }

    @RequireCSRFCheck
    public Result postCreateCourse() {
        Form<CourseUpsertForm> form = Form.form(CourseUpsertForm.class).bindFromRequest();

        if (form.hasErrors() || form.hasGlobalErrors()) {
            return showCreateCourse(form);
        } else {
            CourseUpsertForm courseUpsertForm = form.get();
            courseService.createCourse(courseUpsertForm.name, courseUpsertForm.description);

            return redirect(routes.CourseController.viewCourses());
        }
    }

    @AddCSRFToken
    public Result updateCourseGeneral(long courseId) throws CourseNotFoundException {
        Course course = courseService.findByCourseId(courseId);
        CourseUpsertForm courseUpsertForm = new CourseUpsertForm();
        courseUpsertForm.name = course.getName();
        courseUpsertForm.description = course.getDescription();

        Form<CourseUpsertForm> form = Form.form(CourseUpsertForm.class).fill(courseUpsertForm);

        return showUpdateCourseGeneral(form, course);
    }

    @RequireCSRFCheck
    public Result postUpdateCourseGeneral(long courseId) throws CourseNotFoundException {
        Course course = courseService.findByCourseId(courseId);
        Form<CourseUpsertForm> form = Form.form(CourseUpsertForm.class).bindFromRequest();

        if (form.hasErrors()) {
            return showUpdateCourseGeneral(form, course);
        } else {
            CourseUpsertForm courseUpsertForm = form.get();
            courseService.updateCourse(course.getId(), courseUpsertForm.name, courseUpsertForm.description);

            return redirect(routes.CourseController.viewCourses());
        }
    }

    private Result showCreateCourse(Form<CourseUpsertForm> form) {
        LazyHtml content = new LazyHtml(createCourseView.render(form));
        content.appendLayout(c -> headingLayout.render(Messages.get("course.create"), c));
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("course.courses"), routes.CourseController.viewCourses()),
              new InternalLink(Messages.get("course.create"), routes.CourseController.createCourse())
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Course - Create");
        return ControllerUtils.getInstance().lazyOk(content);
    }

    private Result showUpdateCourseGeneral(Form<CourseUpsertForm> form, Course course) {
        LazyHtml content = new LazyHtml(updateCourseGeneralView.render(form, course.getId()));
        CourseControllerUtils.appendUpdateLayout(content, course);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("course.courses"), routes.CourseController.viewCourses()),
              new InternalLink(Messages.get("course.update"), routes.CourseController.updateCourseGeneral(course.getId()))
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Course - Update");
        return ControllerUtils.getInstance().lazyOk(content);
    }
}
