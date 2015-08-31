package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.jerahmeel.Curriculum;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.securities.GuestView;
import org.iatoki.judgels.jerahmeel.services.CurriculumService;
import org.iatoki.judgels.jerahmeel.views.html.training.listCurriculumsView;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.headingLayout;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named
public final class TrainingCurriculumController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;

    private final CurriculumService curriculumService;

    @Inject
    public TrainingCurriculumController(CurriculumService curriculumService) {
        this.curriculumService = curriculumService;
    }

    @Authenticated(value = GuestView.class)
    @Transactional(readOnly = true)
    public Result viewCurriculums() {
        return listCurriculums(0, "id", "asc", "");
    }

    @Authenticated(value = GuestView.class)
    @Transactional(readOnly = true)
    public Result listCurriculums(long page, String orderBy, String orderDir, String filterString) {
        Page<Curriculum> pageOfCurriculums = curriculumService.getPageOfCurriculums(page, PAGE_SIZE, orderBy, orderDir, filterString);

        LazyHtml content = new LazyHtml(listCurriculumsView.render(pageOfCurriculums, orderBy, orderDir, filterString));
        content.appendLayout(c -> headingLayout.render(Messages.get("training.home"), c));
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content);
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Home");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    private void appendBreadcrumbsLayout(LazyHtml content, InternalLink... lastLinks) {
        ImmutableList.Builder<InternalLink> breadcrumbsBuilder = TrainingControllerUtils.getBreadcrumbsBuilder();
        breadcrumbsBuilder.add(lastLinks);

        JerahmeelControllerUtils.getInstance().appendBreadcrumbsLayout(content, breadcrumbsBuilder.build());
    }
}
