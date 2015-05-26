package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.views.html.layouts.headingWithActionLayout;
import org.iatoki.judgels.commons.views.html.layouts.tabLayout;
import org.iatoki.judgels.jerahmeel.Session;
import play.i18n.Messages;
import play.mvc.Controller;

public final class SessionControllerUtils {

    private SessionControllerUtils() {
        // prevent instantiation
    }

    static void appendViewTabLayout(LazyHtml content, Session session) {
        content.appendLayout(c -> tabLayout.render(ImmutableList.of(
                    new InternalLink(Messages.get("session.lessons"), routes.SessionLessonController.viewVisibleLessons(session.getId())),
                    new InternalLink(Messages.get("session.problems"), routes.SessionProblemController.viewVisibleProblems(session.getId()))
              ), c)
        );
        content.appendLayout(c -> headingWithActionLayout.render(Messages.get("session.session") + " #" + session.getId() + ": " + session.getName(), new InternalLink(Messages.get("commons.update"), routes.SessionController.updateSessionGeneral(session.getId())), c));
    }

    static void appendUpdateTabLayout(LazyHtml content, Session session) {
        content.appendLayout(c -> tabLayout.render(ImmutableList.of(
                    new InternalLink(Messages.get("session.update"), routes.SessionController.updateSessionGeneral(session.getId())),
                    new InternalLink(Messages.get("session.lessons"), routes.SessionLessonController.viewLessons(session.getId())),
                    new InternalLink(Messages.get("session.problems"), routes.SessionProblemController.viewProblems(session.getId())),
                    new InternalLink(Messages.get("session.dependencies"), routes.SessionSessionController.viewDependencies(session.getId()))
              ), c)
        );
        content.appendLayout(c -> headingWithActionLayout.render(Messages.get("session.session") + " #" + session.getId() + ": " + session.getName(), new InternalLink(Messages.get("commons.enter"), routes.SessionController.viewSession(session.getId())), c));
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
