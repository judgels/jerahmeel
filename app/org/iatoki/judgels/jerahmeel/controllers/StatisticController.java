package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.jerahmeel.PointStatistic;
import org.iatoki.judgels.jerahmeel.ProblemStatistic;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.securities.GuestView;
import org.iatoki.judgels.jerahmeel.services.PointStatisticService;
import org.iatoki.judgels.jerahmeel.services.ProblemStatisticService;
import org.iatoki.judgels.jerahmeel.views.html.statistic.listPointStatisticsView;
import org.iatoki.judgels.jerahmeel.views.html.statistic.listProblemStatisticsView;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.heading3Layout;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named
public final class StatisticController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;

    private final PointStatisticService pointStatisticService;
    private final ProblemStatisticService problemStatisticService;

    @Inject
    public StatisticController(PointStatisticService pointStatisticService, ProblemStatisticService problemStatisticService) {
        this.pointStatisticService = pointStatisticService;
        this.problemStatisticService = problemStatisticService;
    }

    @Authenticated(value = GuestView.class)
    @Transactional(readOnly = true)
    public Result index() {
        return viewPointStatistics();
    }

    @Authenticated(value = GuestView.class)
    @Transactional(readOnly = true)
    public Result viewPointStatistics() {
        return listPointStatistics(0, "id", "asc");
    }

    @Authenticated(value = GuestView.class)
    @Transactional(readOnly = true)
    public Result listPointStatistics(long pageIndex, String orderBy, String orderDir) {
        PointStatistic pointStatistic = pointStatisticService.getLatestPointStatisticWithPagination(pageIndex, PAGE_SIZE, orderBy, orderDir, "");

        LazyHtml content = new LazyHtml(listPointStatisticsView.render(pointStatistic, pageIndex, orderBy, orderDir));
        StatisticControllerUtils.appendTabLayout(content);
        content.appendLayout(c -> heading3Layout.render(Messages.get("statistic.point"), c));
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content,
                new InternalLink(Messages.get("statistic.point"), routes.StatisticController.viewPointStatistics())
        );
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Statistics - Hall of Fame");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    @Authenticated(value = GuestView.class)
    @Transactional(readOnly = true)
    public Result viewProblemStatistics() {
        return listProblemStatistics(0, "id", "asc");
    }

    @Authenticated(value = GuestView.class)
    @Transactional(readOnly = true)
    public Result listProblemStatistics(long pageIndex, String orderBy, String orderDir) {
        ProblemStatistic problemStatistic = problemStatisticService.getLatestProblemStatisticWithPagination(pageIndex, PAGE_SIZE, orderBy, orderDir, "");

        LazyHtml content = new LazyHtml(listProblemStatisticsView.render(problemStatistic, pageIndex, orderBy, orderDir));
        StatisticControllerUtils.appendTabLayout(content);
        content.appendLayout(c -> heading3Layout.render(Messages.get("statistic.problem") + " (" + Messages.get("statistic.problem.duration.week") + ")", c));
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content,
                new InternalLink(Messages.get("statistic.problem"), routes.StatisticController.viewProblemStatistics())
        );
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Statistics - Favorite Problems");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    private void appendBreadcrumbsLayout(LazyHtml content, InternalLink... lastLinks) {
        ImmutableList.Builder<InternalLink> breadcrumbsBuilder = StatisticControllerUtils.getBreadcrumbsBuilder();
        breadcrumbsBuilder.add(lastLinks);

        JerahmeelControllerUtils.getInstance().appendBreadcrumbsLayout(content, breadcrumbsBuilder.build());
    }
}
