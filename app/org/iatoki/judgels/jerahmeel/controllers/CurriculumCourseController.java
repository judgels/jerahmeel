package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.jerahmeel.services.CourseService;
import org.iatoki.judgels.jerahmeel.Curriculum;
import org.iatoki.judgels.jerahmeel.CurriculumCourse;
import org.iatoki.judgels.jerahmeel.forms.CurriculumCourseAddForm;
import org.iatoki.judgels.jerahmeel.CurriculumCourseNotFoundException;
import org.iatoki.judgels.jerahmeel.services.CurriculumCourseService;
import org.iatoki.judgels.jerahmeel.forms.CurriculumCourseEditForm;
import org.iatoki.judgels.jerahmeel.CurriculumNotFoundException;
import org.iatoki.judgels.jerahmeel.services.CurriculumService;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authorized;
import org.iatoki.judgels.jerahmeel.controllers.securities.HasRole;
import org.iatoki.judgels.jerahmeel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jerahmeel.views.html.curriculum.course.listAddCurriculumCoursesView;
import org.iatoki.judgels.jerahmeel.views.html.curriculum.course.editCurriculumCourseView;
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
public final class CurriculumCourseController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;

    private final CourseService courseService;
    private final CurriculumCourseService curriculumCourseService;
    private final CurriculumService curriculumService;

    @Inject
    public CurriculumCourseController(CourseService courseService, CurriculumCourseService curriculumCourseService, CurriculumService curriculumService) {
        this.courseService = courseService;
        this.curriculumCourseService = curriculumCourseService;
        this.curriculumService = curriculumService;
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result viewCourses(long curriculumId) throws CurriculumNotFoundException {
        return listAddCourses(curriculumId, 0, "alias", "asc", "");
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result listAddCourses(long curriculumId, long page, String orderBy, String orderDir, String filterString) throws CurriculumNotFoundException {
        Curriculum curriculum = curriculumService.findCurriculumById(curriculumId);

        Page<CurriculumCourse> pageOfCurriculumCourses = curriculumCourseService.getPageOfCurriculumCourses(curriculum.getJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);
        Form<CurriculumCourseAddForm> curriculumCourseAddForm = Form.form(CurriculumCourseAddForm.class);

        return showListAddCourses(curriculum, curriculumCourseAddForm, pageOfCurriculumCourses, orderBy, orderDir, filterString);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postAddCourse(long curriculumId, long page, String orderBy, String orderDir, String filterString) throws CurriculumNotFoundException {
        Curriculum curriculum = curriculumService.findCurriculumById(curriculumId);
        Form<CurriculumCourseAddForm> curriculumCourseCreateForm = Form.form(CurriculumCourseAddForm.class).bindFromRequest();

        if (formHasErrors(curriculumCourseCreateForm)) {
            Page<CurriculumCourse> pageOfCurriculumCourses = curriculumCourseService.getPageOfCurriculumCourses(curriculum.getJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);

            return showListAddCourses(curriculum, curriculumCourseCreateForm, pageOfCurriculumCourses, orderBy, orderDir, filterString);
        }

        CurriculumCourseAddForm curriculumCourseCreateData = curriculumCourseCreateForm.get();
        if (!courseService.courseExistsByJid(curriculumCourseCreateData.courseJid)) {
            curriculumCourseCreateForm.reject(Messages.get("error.curriculum.invalidJid"));
            Page<CurriculumCourse> pageOfCurriculumCourses = curriculumCourseService.getPageOfCurriculumCourses(curriculum.getJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);

            return showListAddCourses(curriculum, curriculumCourseCreateForm, pageOfCurriculumCourses, orderBy, orderDir, filterString);
        }

        if (curriculumCourseService.existsByCurriculumJidAndAlias(curriculum.getJid(), curriculumCourseCreateData.alias)) {
            curriculumCourseCreateForm.reject(Messages.get("error.curriculum.course.duplicateAlias"));
            Page<CurriculumCourse> pageOfCurriculumCourses = curriculumCourseService.getPageOfCurriculumCourses(curriculum.getJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);

            return showListAddCourses(curriculum, curriculumCourseCreateForm, pageOfCurriculumCourses, orderBy, orderDir, filterString);
        }

        if (curriculumCourseService.existsByCurriculumJidAndCourseJid(curriculum.getJid(), curriculumCourseCreateData.courseJid)) {
            curriculumCourseCreateForm.reject(Messages.get("error.curriculum.courseExist"));
            Page<CurriculumCourse> pageOfCurriculumCourses = curriculumCourseService.getPageOfCurriculumCourses(curriculum.getJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);

            return showListAddCourses(curriculum, curriculumCourseCreateForm, pageOfCurriculumCourses, orderBy, orderDir, filterString);
        }

        curriculumCourseService.addCurriculumCourse(curriculum.getJid(), curriculumCourseCreateData.courseJid, curriculumCourseCreateData.alias, curriculumCourseCreateData.completeable, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        return redirect(routes.CurriculumCourseController.viewCourses(curriculum.getId()));
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result editCourse(long curriculumId, long curriculumCourseId) throws CurriculumNotFoundException, CurriculumCourseNotFoundException {
        Curriculum curriculum = curriculumService.findCurriculumById(curriculumId);
        CurriculumCourse curriculumCourse = curriculumCourseService.findCurriculumCourseByCurriculumCourseId(curriculumCourseId);

        if (!curriculumCourse.getCurriculumJid().equals(curriculum.getJid())) {
            return notFound();
        }

        CurriculumCourseEditForm curriculumCourseEditData = new CurriculumCourseEditForm();
        curriculumCourseEditData.alias = curriculumCourse.getAlias();
        curriculumCourseEditData.completeable = curriculumCourse.isCompleteable();

        Form<CurriculumCourseEditForm> curriculumCourseEditForm = Form.form(CurriculumCourseEditForm.class).fill(curriculumCourseEditData);

        return showEditCourse(curriculum, curriculumCourse, curriculumCourseEditForm);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postEditCourse(long curriculumId, long curriculumCourseId) throws CurriculumNotFoundException, CurriculumCourseNotFoundException {
        Curriculum curriculum = curriculumService.findCurriculumById(curriculumId);
        CurriculumCourse curriculumCourse = curriculumCourseService.findCurriculumCourseByCurriculumCourseId(curriculumCourseId);

        if (!curriculumCourse.getCurriculumJid().equals(curriculum.getJid())) {
            return notFound();
        }

        Form<CurriculumCourseEditForm> curriculumCourseEditForm = Form.form(CurriculumCourseEditForm.class).bindFromRequest();
        if (formHasErrors(curriculumCourseEditForm)) {
            return showEditCourse(curriculum, curriculumCourse, curriculumCourseEditForm);
        }

        CurriculumCourseEditForm curriculumCourseEditData = curriculumCourseEditForm.get();
        if (!curriculumCourseEditData.alias.equals(curriculumCourse.getAlias()) && curriculumCourseService.existsByCurriculumJidAndAlias(curriculum.getJid(), curriculumCourseEditData.alias)) {
            curriculumCourseEditForm.reject(Messages.get("error.curriculum.course.duplicateAlias"));

            return showEditCourse(curriculum, curriculumCourse, curriculumCourseEditForm);
        }

        curriculumCourseService.updateCurriculumCourse(curriculumCourse.getId(), curriculumCourseEditData.alias, curriculumCourseEditData.completeable, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        return redirect(routes.CurriculumCourseController.viewCourses(curriculum.getId()));
    }

    @Transactional
    public Result removeCourse(long curriculumId, long curriculumCourseId) throws CurriculumNotFoundException, CurriculumCourseNotFoundException {
        Curriculum curriculum = curriculumService.findCurriculumById(curriculumId);
        CurriculumCourse curriculumCourse = curriculumCourseService.findCurriculumCourseByCurriculumCourseId(curriculumCourseId);

        if (!curriculum.getJid().equals(curriculumCourse.getCurriculumJid())) {
            return forbidden();
        }

        curriculumCourseService.removeCurriculumCourse(curriculumCourseId);

        return redirect(routes.CurriculumCourseController.viewCourses(curriculum.getId()));
    }

    private Result showListAddCourses(Curriculum curriculum, Form<CurriculumCourseAddForm> curriculumCourseAddForm, Page<CurriculumCourse> pageOfCurriculumCourses, String orderBy, String orderDir, String filterString) {
        LazyHtml content = new LazyHtml(listAddCurriculumCoursesView.render(curriculum.getId(), pageOfCurriculumCourses, orderBy, orderDir, filterString, curriculumCourseAddForm));
        CurriculumControllerUtils.appendUpdateLayout(content, curriculum);
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, curriculum);
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Curriculums");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    private Result showEditCourse(Curriculum curriculum, CurriculumCourse curriculumCourse, Form<CurriculumCourseEditForm> curriculumCourseEditForm) {
        LazyHtml content = new LazyHtml(editCurriculumCourseView.render(curriculumCourseEditForm, curriculum.getId(), curriculumCourse.getId()));
        CurriculumControllerUtils.appendUpdateLayout(content, curriculum);
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, curriculum,
                new InternalLink(Messages.get("commons.update"), routes.CurriculumCourseController.editCourse(curriculum.getId(), curriculumCourse.getId()))
        );
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Curriculums");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    private void appendBreadcrumbsLayout(LazyHtml content, Curriculum curriculum, InternalLink... lastLinks) {
        ImmutableList.Builder<InternalLink> breadcrumbsBuilder = CurriculumControllerUtils.getBreadcrumbsBuilder();
        breadcrumbsBuilder.add(new InternalLink(Messages.get("curriculum.courses"), routes.CurriculumController.jumpToCourses(curriculum.getId())));
        breadcrumbsBuilder.add(new InternalLink(Messages.get("commons.view"), routes.CurriculumCourseController.viewCourses(curriculum.getId())));
        breadcrumbsBuilder.add(lastLinks);

        JerahmeelControllerUtils.getInstance().appendBreadcrumbsLayout(content, breadcrumbsBuilder.build());
    }
}
