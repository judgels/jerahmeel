package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.views.html.layouts.tabLayout;
import play.i18n.Messages;

final class StatisticControllerUtils {

    private StatisticControllerUtils() {
        // prevent instantiation
    }

    static void appendTabLayout(LazyHtml content) {
        content.appendLayout(c -> tabLayout.render(ImmutableList.of(
                        new InternalLink(Messages.get("statistic.point"), routes.StatisticController.viewPointStatistics()),
                        new InternalLink(Messages.get("statistic.problem"), routes.StatisticController.viewProblemStatistics())
                ), c)
        );
    }

    static ImmutableList.Builder<InternalLink> getBreadcrumbsBuilder() {
        ImmutableList.Builder<InternalLink> breadcrumbsBuilder = ImmutableList.builder();
        breadcrumbsBuilder.add(new InternalLink(Messages.get("statistic.statistics"), routes.StatisticController.index()));

        return breadcrumbsBuilder;
    }
}
