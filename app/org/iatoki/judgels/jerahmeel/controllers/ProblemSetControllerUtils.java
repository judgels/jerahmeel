package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.jerahmeel.JerahmeelUtils;
import org.iatoki.judgels.jerahmeel.ProblemSet;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.views.html.layouts.descriptionLayout;
import org.iatoki.judgels.play.views.html.layouts.headingWithBackLayout;
import org.iatoki.judgels.play.views.html.layouts.subtabLayout;
import org.iatoki.judgels.play.views.html.layouts.tabLayout;
import play.i18n.Messages;
import play.mvc.Controller;

final class ProblemSetControllerUtils {

    private ProblemSetControllerUtils() {
        // prevent instantiation
    }

    static void appendTabLayout(LazyHtml content, ProblemSet problemSet) {
        ImmutableList.Builder<InternalLink> tabLinksBuilder = ImmutableList.builder();
        tabLinksBuilder.add(new InternalLink(Messages.get("archive.problemSet.problems"), routes.ProblemSetController.jumpToProblems(problemSet.getId())));
        if (!JerahmeelUtils.isGuest()) {
            tabLinksBuilder.add(new InternalLink(Messages.get("archive.problemSet.submissions"), routes.ProblemSetController.jumpToSubmissions(problemSet.getId())));
        }
        content.appendLayout(c -> tabLayout.render(tabLinksBuilder.build(), c));
        if (problemSet.getDescription().isEmpty()) {
            content.appendLayout(c -> descriptionLayout.render(problemSet.getDescription(), c));
        }
        content.appendLayout(c -> headingWithBackLayout.render(
                        problemSet.getName(),
                        new InternalLink(Messages.get("archive.problemSet.backTo") + " " + problemSet.getParentArchive().getName(), routes.ArchiveController.viewArchives(problemSet.getParentArchive().getId())),
                        c)
        );
    }

    static void appendSubtabLayout(LazyHtml content, ProblemSet problemSet) {
        content.appendLayout(c -> subtabLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("commons.view"), routes.ProblemSetProblemController.viewVisibleProblemSetProblems(problemSet.getId())),
                new InternalLink(Messages.get("commons.manage"), routes.ProblemSetProblemController.viewProblemSetProblems(problemSet.getId()))
        ), c));
    }

    static ImmutableList.Builder<InternalLink> getBreadcrumbsBuilder(ProblemSet problemSet) {
        ImmutableList.Builder<InternalLink> breadcrumbsBuilder = ArchiveControllerUtils.getBreadcrumbsBuilder();
        ArchiveControllerUtils.fillBreadcrumbsBuilder(breadcrumbsBuilder, problemSet.getParentArchive());

        return breadcrumbsBuilder;
    }

    static void setCurrentStatementLanguage(String languageCode) {
        Controller.session("currentStatementLanguage", languageCode);
    }

    static String getCurrentStatementLanguage() {
        String lang = Controller.session("currentStatementLanguage");
        if (lang == null) {
            return "en-US";
        } else {
            return lang;
        }
    }
}
