package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.commons.controllers.BaseController;
import org.iatoki.judgels.jerahmeel.Course;
import org.iatoki.judgels.jerahmeel.Curriculum;
import org.iatoki.judgels.jerahmeel.CurriculumNotFoundException;
import org.iatoki.judgels.jerahmeel.CurriculumService;
import org.iatoki.judgels.jerahmeel.CurriculumCourse;
import org.iatoki.judgels.jerahmeel.CurriculumCourseCreateForm;
import org.iatoki.judgels.jerahmeel.CurriculumCourseNotFoundException;
import org.iatoki.judgels.jerahmeel.CurriculumCourseService;
import org.iatoki.judgels.jerahmeel.CourseService;
import org.iatoki.judgels.jerahmeel.controllers.security.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.security.Authorized;
import org.iatoki.judgels.jerahmeel.controllers.security.HasRole;
import org.iatoki.judgels.jerahmeel.controllers.security.LoggedIn;
import org.iatoki.judgels.jerahmeel.views.html.curriculum.course.listCreateCurriculumCoursesView;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Result;

@Transactional
@Authenticated(value = {LoggedIn.class, HasRole.class})
@Authorized(value = {"admin"})
public final class CurriculumCourseController extends BaseController {

    private static final long PAGE_SIZE = 20;

    private final CurriculumService curriculumService;
    private final CurriculumCourseService curriculumCourseService;
    private final CourseService courseService;

    public CurriculumCourseController(CurriculumService curriculumService, CurriculumCourseService curriculumCourseService, CourseService courseService) {
        this.curriculumService = curriculumService;
        this.curriculumCourseService = curriculumCourseService;
        this.courseService = courseService;
    }

    @AddCSRFToken
    public Result viewCourses(long curriculumId) throws CurriculumNotFoundException {
        return listCreateCourses(curriculumId, 0, "id", "asc", "");
    }

    @AddCSRFToken
    public Result listCreateCourses(long curriculumId, long page, String orderBy, String orderDir, String filterString) throws CurriculumNotFoundException {
        Curriculum curriculum = curriculumService.findByCurriculumId(curriculumId);

        Page<CurriculumCourse> curriculumPage = curriculumCourseService.findCurriculumCourses(curriculum.getJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);
        Form<CurriculumCourseCreateForm> form = Form.form(CurriculumCourseCreateForm.class);

        return showListCreateCourses(curriculum, form, curriculumPage, orderBy, orderDir, filterString);
    }

    public Result jumpToCourse(long curriculumId, long curriculumCourseId) throws CurriculumNotFoundException, CurriculumCourseNotFoundException {
        Curriculum curriculum = curriculumService.findByCurriculumId(curriculumId);
        CurriculumCourse curriculumCourse = curriculumCourseService.findByCurriculumCourseId(curriculumCourseId);

        if (curriculum.getJid().equals(curriculumCourse.getCurriculumJid())) {
            Course course = courseService.findByCourseJid(curriculumCourse.getCourseJid());

            return redirect(routes.CourseController.updateCourseGeneral(course.getId()));
        } else {
            return notFound();
        }
    }

    @RequireCSRFCheck
    public Result postCreateCourse(long curriculumId, long page, String orderBy, String orderDir, String filterString) throws CurriculumNotFoundException {
        Curriculum curriculum = curriculumService.findByCurriculumId(curriculumId);
        Form<CurriculumCourseCreateForm> form = Form.form(CurriculumCourseCreateForm.class).bindFromRequest();

        if (form.hasErrors() || form.hasGlobalErrors()) {
            Page<CurriculumCourse> curriculumPage = curriculumCourseService.findCurriculumCourses(curriculum.getJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);

            return showListCreateCourses(curriculum, form, curriculumPage, orderBy, orderDir, filterString);
        } else {
            CurriculumCourseCreateForm data = form.get();
            if (courseService.existByCourseJid(data.courseJid)) {
                if (curriculumCourseService.existByCurriculumJidAndCourseJid(curriculum.getJid(), data.courseJid)) {
                    curriculumCourseService.addCurriculumCourse(curriculum.getJid(), data.courseJid);

                    return redirect(routes.CurriculumCourseController.viewCourses(curriculum.getId()));
                } else {
                    form.reject(Messages.get("error.curriculum.courseExist"));
                    Page<CurriculumCourse> curriculumPage = curriculumCourseService.findCurriculumCourses(curriculum.getJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);

                    return showListCreateCourses(curriculum, form, curriculumPage, orderBy, orderDir, filterString);
                }
            } else {
                form.reject(Messages.get("error.curriculum.invalidJid"));
                Page<CurriculumCourse> curriculumPage = curriculumCourseService.findCurriculumCourses(curriculum.getJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);

                return showListCreateCourses(curriculum, form, curriculumPage, orderBy, orderDir, filterString);
            }
        }
    }

    public Result deleteCourse(long curriculumId, long curriculumCourseId) throws CurriculumNotFoundException, CurriculumCourseNotFoundException {
        Curriculum curriculum = curriculumService.findByCurriculumId(curriculumId);
        CurriculumCourse curriculumCourse = curriculumCourseService.findByCurriculumCourseId(curriculumCourseId);

        if (curriculum.getJid().equals(curriculumCourse.getCurriculumJid())) {
            curriculumCourseService.removeCurriculumCourse(curriculumCourseId);

            return redirect(routes.CurriculumCourseController.viewCourses(curriculum.getId()));
        } else {
            return forbidden();
        }
    }

    private Result showListCreateCourses(Curriculum curriculum, Form<CurriculumCourseCreateForm> form, Page<CurriculumCourse> currentPage, String orderBy, String orderDir, String filterString) {
        LazyHtml content = new LazyHtml(listCreateCurriculumCoursesView.render(curriculum.getId(), currentPage, orderBy, orderDir, filterString, form));
        CurriculumControllerUtils.appendUpdateLayout(content, curriculum);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("curriculum.curriculums"), routes.CurriculumController.viewCurriculums()),
              new InternalLink(Messages.get("curriculum.courses"), routes.CurriculumController.jumpToCourses(curriculum.getId())),
              new InternalLink(Messages.get("commons.view"), routes.CurriculumCourseController.viewCourses(curriculum.getId()))
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Curriculums");

        return ControllerUtils.getInstance().lazyOk(content);
    }
}
