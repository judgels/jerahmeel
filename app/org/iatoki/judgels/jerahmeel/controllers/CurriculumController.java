package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.headingLayout;
import org.iatoki.judgels.play.views.html.layouts.headingWithActionLayout;
import org.iatoki.judgels.jerahmeel.Curriculum;
import org.iatoki.judgels.jerahmeel.CurriculumNotFoundException;
import org.iatoki.judgels.jerahmeel.services.CurriculumService;
import org.iatoki.judgels.jerahmeel.forms.CurriculumUpsertForm;
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
public final class CurriculumController extends AbstractJudgelsController {

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
        Page<Curriculum> pageOfCurriculums = curriculumService.getPageOfCurriculums(page, PAGE_SIZE, orderBy, orderDir, filterString);

        LazyHtml content = new LazyHtml(listCurriculumsView.render(pageOfCurriculums, orderBy, orderDir, filterString));
        content.appendLayout(c -> headingWithActionLayout.render(Messages.get("curriculum.list"), new InternalLink(Messages.get("commons.create"), routes.CurriculumController.createCurriculum()), c));
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content);
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Curriculums");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    public Result jumpToCourses(long curriculumId) {
        return redirect(routes.CurriculumCourseController.viewCourses(curriculumId));
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result createCurriculum() {
        Form<CurriculumUpsertForm> curriculumUpsertForm = Form.form(CurriculumUpsertForm.class);

        return showCreateCurriculum(curriculumUpsertForm);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postCreateCurriculum() {
        Form<CurriculumUpsertForm> curriculumUpsertForm = Form.form(CurriculumUpsertForm.class).bindFromRequest();

        if (formHasErrors(curriculumUpsertForm)) {
            return showCreateCurriculum(curriculumUpsertForm);
        }

        CurriculumUpsertForm curriculumUpsertData = curriculumUpsertForm.get();
        curriculumService.createCurriculum(curriculumUpsertData.name, curriculumUpsertData.description);

        return redirect(routes.CurriculumController.viewCurriculums());
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result updateCurriculumGeneral(long curriculumId) throws CurriculumNotFoundException {
        Curriculum curriculum = curriculumService.findCurriculumById(curriculumId);
        CurriculumUpsertForm curriculumUpsertData = new CurriculumUpsertForm();
        curriculumUpsertData.name = curriculum.getName();
        curriculumUpsertData.description = curriculum.getDescription();

        Form<CurriculumUpsertForm> curriculumUpsertForm = Form.form(CurriculumUpsertForm.class).fill(curriculumUpsertData);

        return showUpdateCurriculumGeneral(curriculumUpsertForm, curriculum);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postUpdateCurriculumGeneral(long curriculumId) throws CurriculumNotFoundException {
        Curriculum curriculum = curriculumService.findCurriculumById(curriculumId);
        Form<CurriculumUpsertForm> curriculumUpsertForm = Form.form(CurriculumUpsertForm.class).bindFromRequest();

        if (formHasErrors(curriculumUpsertForm)) {
            return showUpdateCurriculumGeneral(curriculumUpsertForm, curriculum);
        }

        CurriculumUpsertForm curriculumUpsertData = curriculumUpsertForm.get();
        curriculumService.updateCurriculum(curriculum.getId(), curriculumUpsertData.name, curriculumUpsertData.description);

        return redirect(routes.CurriculumController.viewCurriculums());
    }

    private Result showCreateCurriculum(Form<CurriculumUpsertForm> curriculumUpsertForm) {
        LazyHtml content = new LazyHtml(createCurriculumView.render(curriculumUpsertForm));
        content.appendLayout(c -> headingLayout.render(Messages.get("curriculum.create"), c));
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content,
                new InternalLink(Messages.get("curriculum.create"), routes.CurriculumController.createCurriculum())
        );
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Curriculum - Create");
        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    private Result showUpdateCurriculumGeneral(Form<CurriculumUpsertForm> curriculumUpsertForm, Curriculum curriculum) {
        LazyHtml content = new LazyHtml(updateCurriculumGeneralView.render(curriculumUpsertForm, curriculum.getId()));
        CurriculumControllerUtils.appendUpdateLayout(content, curriculum);
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content,
                new InternalLink(Messages.get("curriculum.update"), routes.CurriculumController.updateCurriculumGeneral(curriculum.getId()))
        );
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Curriculum - Update");
        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    private void appendBreadcrumbsLayout(LazyHtml content, InternalLink... lastLinks) {
        ImmutableList.Builder<InternalLink> breadcrumbsBuilder = CurriculumControllerUtils.getBreadcrumbsBuilder();
        breadcrumbsBuilder.add(lastLinks);

        JerahmeelControllerUtils.getInstance().appendBreadcrumbsLayout(content, breadcrumbsBuilder.build());
    }
}
