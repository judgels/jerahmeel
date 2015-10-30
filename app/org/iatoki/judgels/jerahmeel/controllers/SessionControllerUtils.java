package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.jerahmeel.Session;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.views.html.layouts.headingLayout;
import org.iatoki.judgels.play.views.html.layouts.subtabLayout;
import play.i18n.Messages;

final class SessionControllerUtils {

    private SessionControllerUtils() {
        // prevent instantiation
    }

    static void appendTabLayout(LazyHtml content, Session session) {
        content.appendLayout(c -> subtabLayout.render(ImmutableList.of(
                    new InternalLink(Messages.get("session.update"), routes.SessionController.editSessionGeneral(session.getId())),
                    new InternalLink(Messages.get("session.lessons"), routes.SessionController.jumpToLessons(session.getId())),
                    new InternalLink(Messages.get("session.problems"), routes.SessionController.jumpToProblems(session.getId())),
                    new InternalLink(Messages.get("session.dependencies"), routes.SessionDependencyController.viewDependencies(session.getId())),
                    new InternalLink(Messages.get("session.submissions"), routes.SessionController.jumpToSubmissions(session.getId()))
              ), c)
        );

        content.appendLayout(c -> headingLayout.render(Messages.get("session.session") + " #" + session.getId() + ": " + session.getName(), c));
    }

    static ImmutableList.Builder<InternalLink> getBreadcrumbsBuilder() {
        ImmutableList.Builder<InternalLink> breadcrumbsBuilder = ImmutableList.builder();
        breadcrumbsBuilder.add(new InternalLink(Messages.get("session.sessions"), routes.SessionController.viewSessions()));

        return breadcrumbsBuilder;
    }
}
