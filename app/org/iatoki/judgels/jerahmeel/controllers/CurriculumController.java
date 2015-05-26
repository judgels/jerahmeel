package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.commons.controllers.BaseController;
import org.iatoki.judgels.commons.views.html.layouts.headingLayout;
import org.iatoki.judgels.commons.views.html.layouts.headingWithActionLayout;
import org.iatoki.judgels.jerahmeel.Curriculum;
import org.iatoki.judgels.jerahmeel.CurriculumNotFoundException;
import org.iatoki.judgels.jerahmeel.CurriculumService;
import org.iatoki.judgels.jerahmeel.CurriculumUpsertForm;
import org.iatoki.judgels.jerahmeel.controllers.security.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.security.Authorized;
import org.iatoki.judgels.jerahmeel.controllers.security.HasRole;
import org.iatoki.judgels.jerahmeel.controllers.security.LoggedIn;
import org.iatoki.judgels.jerahmeel.views.html.curriculum.createCurriculumView;
import org.iatoki.judgels.jerahmeel.views.html.curriculum.listCurriculumsView;
import org.iatoki.judgels.jerahmeel.views.html.curriculum.updateCurriculumGeneralView;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Result;

@Transactional
@Authenticated(value = {LoggedIn.class, HasRole.class})
public final class CurriculumController extends BaseController {

    private static final long PAGE_SIZE = 20;

    private final CurriculumService curriculumService;

    public CurriculumController(CurriculumService curriculumService) {
        this.curriculumService = curriculumService;
    }

    public Result viewCurriculums() {
        return listCurriculums(0, "id", "asc", "");
    }

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

    public Result viewCurriculum(long curriculumId) throws CurriculumNotFoundException {
        Curriculum curriculum = curriculumService.findByCurriculumId(curriculumId);

//        LazyHtml content = new LazyHtml(viewCurriculumView.render(curriculum));
//        content.appendLayout(c -> headingWithActionLayout.render(Messages.get("curriculum.curriculum") + " #" + curriculum.getId() + ": " + curriculum.getName(), new InternalLink(Messages.get("commons.update"), routes.CurriculumController.updateCurriculumGeneral(curriculum.getId())), c));
//        ControllerUtils.getInstance().appendSidebarLayout(content);
//        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
//              new InternalLink(Messages.get("curriculum.curriculums"), routes.CurriculumController.index()),
//              new InternalLink(Messages.get("curriculum.view"), routes.CurriculumController.viewCurriculum(curriculum.getId()))
//        ));
//        ControllerUtils.getInstance().appendTemplateLayout(content, "Curriculum - View");
//
//        return ControllerUtils.getInstance().lazyOk(content);
        return TODO;
    }

    @AddCSRFToken
    public Result createCurriculum() {
        Form<CurriculumUpsertForm> form = Form.form(CurriculumUpsertForm.class);

        return showCreateCurriculum(form);
    }

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

    @AddCSRFToken
    public Result updateCurriculumGeneral(long curriculumId) throws CurriculumNotFoundException {
        Curriculum curriculum = curriculumService.findByCurriculumId(curriculumId);
        CurriculumUpsertForm curriculumUpsertForm = new CurriculumUpsertForm();
        curriculumUpsertForm.name = curriculum.getName();
        curriculumUpsertForm.description = curriculum.getDescription();

        Form<CurriculumUpsertForm> form = Form.form(CurriculumUpsertForm.class).fill(curriculumUpsertForm);

        return showUpdateCurriculumGeneral(form, curriculum);
    }

    @RequireCSRFCheck
    public Result postUpdateCurriculumGeneral(long curriculumId) throws CurriculumNotFoundException {
        Curriculum curriculum = curriculumService.findByCurriculumId(curriculumId);
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
        CurriculumControllerUtils.appendUpdateTabLayout(content, curriculum);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("curriculum.curriculums"), routes.CurriculumController.viewCurriculums()),
              new InternalLink(Messages.get("curriculum.update"), routes.CurriculumController.updateCurriculumGeneral(curriculum.getId()))
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Curriculum - Update");
        return ControllerUtils.getInstance().lazyOk(content);
    }}
