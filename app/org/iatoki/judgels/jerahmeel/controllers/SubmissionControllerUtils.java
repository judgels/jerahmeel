package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.views.html.layouts.subtabLayout;
import org.iatoki.judgels.play.views.html.layouts.tabLayout;
import play.i18n.Messages;

final class SubmissionControllerUtils {

    private SubmissionControllerUtils() {
        // prevent instantiation
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
