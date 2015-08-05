package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.jerahmeel.services.CourseService;
import org.iatoki.judgels.jerahmeel.Curriculum;
import org.iatoki.judgels.jerahmeel.CurriculumCourse;
import org.iatoki.judgels.jerahmeel.forms.CurriculumCourseCreateForm;
import org.iatoki.judgels.jerahmeel.CurriculumCourseNotFoundException;
import org.iatoki.judgels.jerahmeel.services.CurriculumCourseService;
import org.iatoki.judgels.jerahmeel.forms.CurriculumCourseUpdateForm;
import org.iatoki.judgels.jerahmeel.CurriculumNotFoundException;
import org.iatoki.judgels.jerahmeel.services.CurriculumService;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authorized;
import org.iatoki.judgels.jerahmeel.controllers.securities.HasRole;
import org.iatoki.judgels.jerahmeel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jerahmeel.views.html.curriculum.course.listCreateCurriculumCoursesView;
import org.iatoki.judgels.jerahmeel.views.html.curriculum.course.updateCurriculumCourseView;
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
public final class CurriculumCourseController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;

    private final CurriculumService curriculumService;
    private final CurriculumCourseService curriculumCourseService;
    private final CourseService courseService;

    @Inject
    public CurriculumCourseController(CurriculumService curriculumService, CurriculumCourseService curriculumCourseService, CourseService courseService) {
        this.curriculumService = curriculumService;
        this.curriculumCourseService = curriculumCourseService;
        this.courseService = courseService;
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result viewCourses(long curriculumId) throws CurriculumNotFoundException {
        return listCreateCourses(curriculumId, 0, "alias", "asc", "");
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result listCreateCourses(long curriculumId, long page, String orderBy, String orderDir, String filterString) throws CurriculumNotFoundException {
        Curriculum curriculum = curriculumService.findCurriculumByCurriculumId(curriculumId);

        Page<CurriculumCourse> curriculumPage = curriculumCourseService.findCurriculumCourses(curriculum.getJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);
        Form<CurriculumCourseCreateForm> form = Form.form(CurriculumCourseCreateForm.class);

        return showListCreateCourses(curriculum, form, curriculumPage, orderBy, orderDir, filterString);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postCreateCourse(long curriculumId, long page, String orderBy, String orderDir, String filterString) throws CurriculumNotFoundException {
        Curriculum curriculum = curriculumService.findCurriculumByCurriculumId(curriculumId);
        Form<CurriculumCourseCreateForm> form = Form.form(CurriculumCourseCreateForm.class).bindFromRequest();

        if (form.hasErrors() || form.hasGlobalErrors()) {
            Page<CurriculumCourse> curriculumPage = curriculumCourseService.findCurriculumCourses(curriculum.getJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);

            return showListCreateCourses(curriculum, form, curriculumPage, orderBy, orderDir, filterString);
        } else {
            CurriculumCourseCreateForm data = form.get();
            if (courseService.existByCourseJid(data.courseJid)) {
                if (curriculumCourseService.existByCurriculumJidAndAlias(curriculum.getJid(), data.alias)) {
                    form.reject(Messages.get("error.curriculum.course.duplicateAlias"));
                    Page<CurriculumCourse> curriculumPage = curriculumCourseService.findCurriculumCourses(curriculum.getJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);

                    return showListCreateCourses(curriculum, form, curriculumPage, orderBy, orderDir, filterString);
                } else if (!curriculumCourseService.existByCurriculumJidAndCourseJid(curriculum.getJid(), data.courseJid)) {
                    curriculumCourseService.addCurriculumCourse(curriculum.getJid(), data.courseJid, data.alias, data.completeable);

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

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result updateCourse(long curriculumId, long curriculumCourseId) throws CurriculumNotFoundException, CurriculumCourseNotFoundException {
        Curriculum curriculum = curriculumService.findCurriculumByCurriculumId(curriculumId);
        CurriculumCourse curriculumCourse = curriculumCourseService.findCurriculumCourseByCurriculumCourseId(curriculumCourseId);

        if (curriculumCourse.getCurriculumJid().equals(curriculum.getJid())) {
            CurriculumCourseUpdateForm curriculumCourseUpdateForm = new CurriculumCourseUpdateForm();
            curriculumCourseUpdateForm.alias = curriculumCourse.getAlias();
            curriculumCourseUpdateForm.completeable = curriculumCourse.isCompleteable();

            Form<CurriculumCourseUpdateForm> form = Form.form(CurriculumCourseUpdateForm.class).fill(curriculumCourseUpdateForm);

            return showUpdateCourse(curriculum, curriculumCourse, form);
        } else {
            return notFound();
        }
    }

    @Transactional
    @RequireCSRFCheck
    public Result postUpdateCourse(long curriculumId, long curriculumCourseId) throws CurriculumNotFoundException, CurriculumCourseNotFoundException {
        Curriculum curriculum = curriculumService.findCurriculumByCurriculumId(curriculumId);
        CurriculumCourse curriculumCourse = curriculumCourseService.findCurriculumCourseByCurriculumCourseId(curriculumCourseId);

        if (curriculumCourse.getCurriculumJid().equals(curriculum.getJid())) {
            Form<CurriculumCourseUpdateForm> form = Form.form(CurriculumCourseUpdateForm.class).bindFromRequest();
            if (!((form.hasErrors()) || (form.hasGlobalErrors()))) {
                CurriculumCourseUpdateForm curriculumCourseUpdateForm = form.get();
                if ((curriculumCourseUpdateForm.alias.equals(curriculumCourse.getAlias())) || (!curriculumCourseService.existByCurriculumJidAndAlias(curriculum.getJid(), curriculumCourseUpdateForm.alias))) {
                    curriculumCourseService.updateCurriculumCourse(curriculumCourse.getId(), curriculumCourseUpdateForm.alias, curriculumCourseUpdateForm.completeable);

                    return redirect(routes.CurriculumCourseController.viewCourses(curriculum.getId()));
                } else {
                    form.reject(Messages.get("error.curriculum.course.duplicateAlias"));

                    return showUpdateCourse(curriculum, curriculumCourse, form);
                }
            } else {
                return showUpdateCourse(curriculum, curriculumCourse, form);
            }
        } else {
            return notFound();
        }
    }

    @Transactional
    public Result deleteCourse(long curriculumId, long curriculumCourseId) throws CurriculumNotFoundException, CurriculumCourseNotFoundException {
        Curriculum curriculum = curriculumService.findCurriculumByCurriculumId(curriculumId);
        CurriculumCourse curriculumCourse = curriculumCourseService.findCurriculumCourseByCurriculumCourseId(curriculumCourseId);

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

    private Result showUpdateCourse(Curriculum curriculum, CurriculumCourse curriculumCourse, Form<CurriculumCourseUpdateForm> form) {
        LazyHtml content = new LazyHtml(updateCurriculumCourseView.render(form, curriculum.getId(), curriculumCourse.getId()));
        CurriculumControllerUtils.appendUpdateLayout(content, curriculum);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("curriculum.curriculums"), routes.CurriculumController.viewCurriculums()),
              new InternalLink(Messages.get("curriculum.courses"), routes.CurriculumController.jumpToCourses(curriculum.getId())),
              new InternalLink(Messages.get("commons.view"), routes.CurriculumCourseController.viewCourses(curriculum.getId())),
              new InternalLink(Messages.get("commons.update"), routes.CurriculumCourseController.updateCourse(curriculum.getId(), curriculumCourse.getId()))
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Curriculums");

        return ControllerUtils.getInstance().lazyOk(content);
    }
}
