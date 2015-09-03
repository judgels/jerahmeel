package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.views.html.layouts.subtabLayout;
import org.iatoki.judgels.play.views.html.layouts.descriptionLayout;
import org.iatoki.judgels.play.views.html.layouts.headingLayout;
import org.iatoki.judgels.play.views.html.layouts.headingWithBackLayout;
import org.iatoki.judgels.play.views.html.layouts.headingWithActionAndBackLayout;
import org.iatoki.judgels.play.views.html.layouts.tabLayout;
import org.iatoki.judgels.jerahmeel.CourseSession;
import org.iatoki.judgels.jerahmeel.Curriculum;
import org.iatoki.judgels.jerahmeel.CurriculumCourse;
import org.iatoki.judgels.jerahmeel.JerahmeelUtils;
import org.iatoki.judgels.jerahmeel.Session;
import play.i18n.Messages;
import play.mvc.Controller;

public final class SessionControllerUtils {

    private SessionControllerUtils() {
        // prevent instantiation
    }

    static void appendViewLayout(LazyHtml content, Curriculum curriculum, CurriculumCourse curriculumCourse, CourseSession courseSession, Session session) {
        ImmutableList.Builder<InternalLink> tabLinksBuilder = ImmutableList.builder();
        tabLinksBuilder.add(new InternalLink(Messages.get("session.lessons"), routes.TrainingController.jumpToLessons(curriculum.getId(), curriculumCourse.getId(), courseSession.getId())));
        tabLinksBuilder.add(new InternalLink(Messages.get("session.problems"), routes.TrainingController.jumpToProblems(curriculum.getId(), curriculumCourse.getId(), courseSession.getId())));
        if (!JerahmeelUtils.isGuest()) {
            tabLinksBuilder.add(new InternalLink(Messages.get("session.submissions"), routes.TrainingController.jumpToSubmissions(curriculum.getId(), curriculumCourse.getId(), courseSession.getId())));
        }
        content.appendLayout(c -> tabLayout.render(tabLinksBuilder.build(), c));
        if (session.getDescription().isEmpty()) {
            content.appendLayout(c -> descriptionLayout.render(session.getDescription(), c));
        }
        if (JerahmeelUtils.hasRole("admin")) {
            content.appendLayout(c -> headingWithActionAndBackLayout.render(
                    Messages.get("session.session") + " #" + session.getId() + ": " + session.getName(),
                    new InternalLink(Messages.get("commons.update"), routes.SessionController.updateSessionGeneral(session.getId())),
                    new InternalLink(Messages.get("training.backTo") + " " + curriculumCourse.getCourseName(), routes.TrainingSessionController.viewSessions(curriculum.getId(), curriculumCourse.getId())),
                    c)
            );
        } else if (courseSession.isCompleteable()) {
            content.appendLayout(c -> headingWithBackLayout.render(
                    Messages.get("session.session") + " " + courseSession.getAlias() + ": " + session.getName(),
                    new InternalLink(Messages.get("training.backTo") + " " + curriculumCourse.getCourseName(), routes.TrainingSessionController.viewSessions(curriculum.getId(), curriculumCourse.getId())),
                    c)
            );
        } else {
            content.appendLayout(c -> headingWithBackLayout.render(
                    session.getName(),
                    new InternalLink(Messages.get("training.backTo") + " " + curriculumCourse.getCourseName(), routes.TrainingSessionController.viewSessions(curriculum.getId(), curriculumCourse.getId())),
                    c)
            );
        }
    }

    static void appendUpdateLayout(LazyHtml content, Session session) {
        content.appendLayout(c -> subtabLayout.render(ImmutableList.of(
                    new InternalLink(Messages.get("session.update"), routes.SessionController.updateSessionGeneral(session.getId())),
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
