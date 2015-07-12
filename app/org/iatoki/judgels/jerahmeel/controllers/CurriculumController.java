package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.BaseController;
import org.iatoki.judgels.play.views.html.layouts.headingLayout;
import org.iatoki.judgels.play.views.html.layouts.headingWithActionLayout;
import org.iatoki.judgels.jerahmeel.Curriculum;
import org.iatoki.judgels.jerahmeel.CurriculumNotFoundException;
import org.iatoki.judgels.jerahmeel.services.CurriculumService;
import org.iatoki.judgels.jerahmeel.controllers.forms.CurriculumUpsertForm;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authorized;
import org.iatoki.judgels.jerahmeel.controllers.securities.HasRole;
import org.iatoki.judgels.jerahmeel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jerahmeel.views.html.curriculum.createCurriculumView;
import org.iatoki.judgels.jerahmeel.views.html.curriculum.listCurriculumsView;
import org.iatoki.judgels.jerahmeel.views.html.curriculum.updateCurriculumGeneralView;
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
public final class CurriculumController extends BaseController {

    private static final long PAGE_SIZE = 20;

    private final CurriculumService curriculumService;

    @Inject
    public CurriculumController(CurriculumService curriculumService) {
        this.curriculumService = curriculumService;
    }

    @Transactional(readOnly = true)
    public Result viewCurriculums() {
        return listCurriculums(0, "id", "asc", "");
    }

    @Transactional(readOnly = true)
    public Result listCurriculums(long page, String orderBy, String orderDir, String filterString) {
        Page<Curriculum> currentPage = curriculumService.pageCurriculums(page, PAGE_SIZE, orderBy, orderDir, filterString);

        LazyHtml content = new LazyHtml(listCurriculumsView.render(currentPage, orderBy, orderDir, filterString));
        content.appendLayout(c -> headingWithActionLayout.render(Messages.get("curriculum.list"), new InternalLink(Messages.get("commons.create"), routes.CurriculumController.createCurriculum()), c));
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("curriculum.curriculums"), routes.CurriculumController.viewCurriculums())
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Curriculums");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    public Result jumpToCourses(long curriculumId) {
        return redirect(routes.CurriculumCourseController.viewCourses(curriculumId));
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result createCurriculum() {
        Form<CurriculumUpsertForm> form = Form.form(CurriculumUpsertForm.class);

        return showCreateCurriculum(form);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postCreateCurriculum() {
        Form<CurriculumUpsertForm> form = Form.form(CurriculumUpsertForm.class).bindFromRequest();

        if (form.hasErrors() || form.hasGlobalErrors()) {
            return showCreateCurriculum(form);
        } else {
            CurriculumUpsertForm curriculumUpsertForm = form.get();
            curriculumService.createCurriculum(curriculumUpsertForm.name, curriculumUpsertForm.description);

            return redirect(routes.CurriculumController.viewCurriculums());
        }
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result updateCurriculumGeneral(long curriculumId) throws CurriculumNotFoundException {
        Curriculum curriculum = curriculumService.findCurriculumByCurriculumId(curriculumId);
        CurriculumUpsertForm curriculumUpsertForm = new CurriculumUpsertForm();
        curriculumUpsertForm.name = curriculum.getName();
        curriculumUpsertForm.description = curriculum.getDescription();

        Form<CurriculumUpsertForm> form = Form.form(CurriculumUpsertForm.class).fill(curriculumUpsertForm);

        return showUpdateCurriculumGeneral(form, curriculum);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postUpdateCurriculumGeneral(long curriculumId) throws CurriculumNotFoundException {
        Curriculum curriculum = curriculumService.findCurriculumByCurriculumId(curriculumId);
        Form<CurriculumUpsertForm> form = Form.form(CurriculumUpsertForm.class).bindFromRequest();

        if (form.hasErrors()) {
            return showUpdateCurriculumGeneral(form, curriculum);
        } else {
            CurriculumUpsertForm curriculumUpsertForm = form.get();
            curriculumService.updateCurriculum(curriculum.getId(), curriculumUpsertForm.name, curriculumUpsertForm.description);

            return redirect(routes.CurriculumController.viewCurriculums());
        }
    }

    private Result showCreateCurriculum(Form<CurriculumUpsertForm> form) {
        LazyHtml content = new LazyHtml(createCurriculumView.render(form));
        content.appendLayout(c -> headingLayout.render(Messages.get("curriculum.create"), c));
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("curriculum.curriculums"), routes.CurriculumController.viewCurriculums()),
              new InternalLink(Messages.get("curriculum.create"), routes.CurriculumController.createCurriculum())
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Curriculum - Create");
        return ControllerUtils.getInstance().lazyOk(content);
    }

    private Result showUpdateCurriculumGeneral(Form<CurriculumUpsertForm> form, Curriculum curriculum) {
        LazyHtml content = new LazyHtml(updateCurriculumGeneralView.render(form, curriculum.getId()));
        CurriculumControllerUtils.appendUpdateLayout(content, curriculum);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("curriculum.curriculums"), routes.CurriculumController.viewCurriculums()),
              new InternalLink(Messages.get("curriculum.update"), routes.CurriculumController.updateCurriculumGeneral(curriculum.getId()))
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Curriculum - Update");
        return ControllerUtils.getInstance().lazyOk(content);
    }
}
