package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.iatoki.judgels.jerahmeel.services.ProblemSetService;
import org.iatoki.judgels.jerahmeel.services.SessionService;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.views.html.layouts.subtabLayout;
import org.iatoki.judgels.play.views.html.layouts.tabLayout;
import play.i18n.Messages;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

final class SubmissionControllerUtils {

    private SubmissionControllerUtils() {
        // prevent instantiation
    }

    static Map<String, String> getJidToNameMap(SessionService sessionService, ProblemSetService problemSetService, List<String> jids) {
        Map<String, String> sessionJidToNameMap = sessionService.getSessionJidToNameMapBySessionJids(jids.stream().filter(s -> s.startsWith("JIDSESS")).collect(Collectors.toList()));
        Map<String, String> problemSetJidToNameMap = problemSetService.getProblemSetJidToNameMapByProblemSetJids(jids.stream().filter(s -> s.startsWith("JIDPRSE")).collect(Collectors.toList()));

        ImmutableMap.Builder<String, String> jidToNameMapBuilder = ImmutableMap.builder();
        jidToNameMapBuilder.putAll(sessionJidToNameMap);
        jidToNameMapBuilder.putAll(problemSetJidToNameMap);

        return jidToNameMapBuilder.build();
    }

    static void appendTabLayout(LazyHtml content) {
        content.appendLayout(c -> tabLayout.render(ImmutableList.of(
                        new InternalLink(Messages.get("submission.own"), routes.SubmissionController.jumpToOwnSubmissions()),
                        new InternalLink(Messages.get("submission.all"), routes.SubmissionController.jumpToAllSubmissions())
                ), c)
        );
    }

    static void appendOwnSubtabLayout(LazyHtml content) {
        content.appendLayout(c -> subtabLayout.render(ImmutableList.of(
                        new InternalLink(Messages.get("submission.programming"), routes.ProgrammingSubmissionController.viewOwnSubmissions()),
                        new InternalLink(Messages.get("submission.bundle"), routes.BundleSubmissionController.viewOwnSubmissions())
                ), c)
        );
    }

    static void appendAllSubtabLayout(LazyHtml content) {
        content.appendLayout(c -> subtabLayout.render(ImmutableList.of(
                        new InternalLink(Messages.get("submission.programming"), routes.ProgrammingSubmissionController.viewSubmissions()),
                        new InternalLink(Messages.get("submission.bundle"), routes.BundleSubmissionController.viewSubmissions())
                ), c)
        );
    }

    static ImmutableList.Builder<InternalLink> getBreadcrumbsBuilder() {
        ImmutableList.Builder<InternalLink> breadcrumbsBuilder = ImmutableList.builder();
        breadcrumbsBuilder.add(new InternalLink(Messages.get("submission.submissions"), routes.SubmissionController.jumpToSubmissions()));

        return breadcrumbsBuilder;
    }
}
