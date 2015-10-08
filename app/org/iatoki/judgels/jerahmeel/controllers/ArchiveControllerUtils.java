package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.jerahmeel.Archive;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.views.html.layouts.headingWithActionLayout;
import play.i18n.Messages;

import java.util.List;
import java.util.Stack;

public final class ArchiveControllerUtils {

    private ArchiveControllerUtils() {
        // prevent instantiation
    }

    static void appendUpdateLayout(LazyHtml content, Archive archive) {
        content.appendLayout(c -> headingWithActionLayout.render(Messages.get("archive.archive") + "#" + archive.getId() + ": " + archive.getName(), new InternalLink(Messages.get("archive.enter"), routes.ArchiveController.viewArchives(archive.getId())), c));
    }

    static ImmutableList.Builder<InternalLink> getBreadcrumbsBuilder() {
        ImmutableList.Builder<InternalLink> breadcrumbsBuilder = ImmutableList.builder();
        breadcrumbsBuilder.add(new InternalLink(Messages.get("training.home"), routes.TrainingController.index()));

        return breadcrumbsBuilder;
    }

    static void fillBreadcrumbsBuilder(ImmutableList.Builder<InternalLink> breadcrumbsBuilder, Archive archive) {
        if (archive != null) {
            Stack<InternalLink> internalLinkStack = new Stack<>();
            Archive currentParent = archive;
            while (currentParent != null) {
                internalLinkStack.push(new InternalLink(currentParent.getName(), routes.ArchiveController.viewArchives(currentParent.getId())));
                currentParent = currentParent.getParentArchive();
            }

            while (!internalLinkStack.isEmpty()) {
                breadcrumbsBuilder.add(internalLinkStack.pop());
            }
        }
    }

    static void appendBreadcrumbsLayout(LazyHtml content, List<InternalLink> lastLinks) {
        ImmutableList.Builder<InternalLink> breadcrumbsBuilder = getBreadcrumbsBuilder();
        breadcrumbsBuilder.addAll(lastLinks);

        JerahmeelControllerUtils.getInstance().appendBreadcrumbsLayout(content, breadcrumbsBuilder.build());
    }

    static void appendBreadcrumbsLayout(LazyHtml content, InternalLink... lastLinks) {
        ImmutableList.Builder<InternalLink> breadcrumbsBuilder = getBreadcrumbsBuilder();
        breadcrumbsBuilder.add(lastLinks);

        JerahmeelControllerUtils.getInstance().appendBreadcrumbsLayout(content, breadcrumbsBuilder.build());
    }
}
