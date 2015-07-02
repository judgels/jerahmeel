package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.commons.controllers.BaseController;
import org.iatoki.judgels.commons.views.html.layouts.headingLayout;
import org.iatoki.judgels.jerahmeel.Curriculum;
import org.iatoki.judgels.jerahmeel.services.CurriculumService;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.securities.HasRole;
import org.iatoki.judgels.jerahmeel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jerahmeel.views.html.training.listCurriculumsView;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.mvc.Result;

@Authenticated(value = {LoggedIn.class, HasRole.class})
public final class TrainingCurriculumController extends BaseController {
    private static final long PAGE_SIZE = 20;

    private final CurriculumService curriculumService;

    public TrainingCurriculumController(CurriculumService curriculumService) {
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
        content.appendLayout(c -> headingLayout.render(Messages.get("training.home"), c));
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("training.home"), routes.TrainingController.jumpToCurriculums())
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Home");

        return ControllerUtils.getInstance().lazyOk(content);
    }
}
