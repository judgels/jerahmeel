package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.jerahmeel.Chapter;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.views.html.layouts.headingLayout;
import org.iatoki.judgels.play.views.html.layouts.subtabLayout;
import play.i18n.Messages;

final class ChapterControllerUtils {

    private ChapterControllerUtils() {
        // prevent instantiation
    }

    static void appendTabLayout(LazyHtml content, Chapter chapter) {
        content.appendLayout(c -> subtabLayout.render(ImmutableList.of(
                    new InternalLink(Messages.get("chapter.update"), routes.ChapterController.editChapterGeneral(chapter.getId())),
                    new InternalLink(Messages.get("chapter.lessons"), routes.ChapterController.jumpToLessons(chapter.getId())),
                    new InternalLink(Messages.get("chapter.problems"), routes.ChapterController.jumpToProblems(chapter.getId())),
                    new InternalLink(Messages.get("chapter.dependencies"), routes.ChapterDependencyController.viewDependencies(chapter.getId())),
                    new InternalLink(Messages.get("chapter.submissions"), routes.ChapterController.jumpToSubmissions(chapter.getId()))
              ), c)
        );

        content.appendLayout(c -> headingLayout.render(Messages.get("chapter.chapter") + " #" + chapter.getId() + ": " + chapter.getName(), c));
    }

    static ImmutableList.Builder<InternalLink> getBreadcrumbsBuilder() {
        ImmutableList.Builder<InternalLink> breadcrumbsBuilder = ImmutableList.builder();
        breadcrumbsBuilder.add(new InternalLink(Messages.get("chapter.chapters"), routes.ChapterController.viewChapters()));

        return breadcrumbsBuilder;
    }
}
