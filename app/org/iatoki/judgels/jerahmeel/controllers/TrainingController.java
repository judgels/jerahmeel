package org.iatoki.judgels.jerahmeel.controllers;

import org.iatoki.judgels.jerahmeel.Archive;
import org.iatoki.judgels.jerahmeel.ArchiveWithScore;
import org.iatoki.judgels.jerahmeel.Curriculum;
import org.iatoki.judgels.jerahmeel.JerahmeelUtils;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.securities.GuestView;
import org.iatoki.judgels.jerahmeel.services.ArchiveService;
import org.iatoki.judgels.jerahmeel.services.CurriculumService;
import org.iatoki.judgels.jerahmeel.views.html.training.listTrainingsView;
import org.iatoki.judgels.jerahmeel.views.html.training.listTrainingsWithScoreView;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.headingLayout;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;

@Singleton
@Named
public final class TrainingController extends AbstractJudgelsController {

    private final CurriculumService curriculumService;
    private final ArchiveService archiveService;

    @Inject
    public TrainingController(CurriculumService curriculumService, ArchiveService archiveService) {
        this.curriculumService = curriculumService;
        this.archiveService = archiveService;
    }

    @Authenticated(GuestView.class)
    @Transactional
    public Result index() {
        return listTrainings();
    }

    @Authenticated(GuestView.class)
    @Transactional
    public Result listTrainings() {
        List<Curriculum> curriculums = curriculumService.getAllCurriculums();

        LazyHtml content;
        if (JerahmeelUtils.isGuest()) {
            List<Archive> archives = archiveService.getChildArchives("");
            content = new LazyHtml(listTrainingsView.render(curriculums, archives));
        } else {
            List<ArchiveWithScore> archives = archiveService.getChildArchivesWithScore("", IdentityUtils.getUserJid());
            content = new LazyHtml(listTrainingsWithScoreView.render(curriculums, archives));
        }

        return showListTrainings(content);
    }

    private Result showListTrainings(LazyHtml content) {
        content.appendLayout(c -> headingLayout.render(Messages.get("training.home"), c));
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        JerahmeelControllerUtils.getInstance().appendBreadcrumbsLayout(content, TrainingControllerUtils.getBreadcrumbsBuilder().build());
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Home");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }
}
