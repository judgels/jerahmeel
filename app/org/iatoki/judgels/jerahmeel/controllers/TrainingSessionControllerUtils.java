package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.jerahmeel.Course;
import org.iatoki.judgels.jerahmeel.CourseSession;
import org.iatoki.judgels.jerahmeel.Curriculum;
import org.iatoki.judgels.jerahmeel.CurriculumCourse;
import org.iatoki.judgels.jerahmeel.JerahmeelUtils;
import org.iatoki.judgels.jerahmeel.Session;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.views.html.layouts.descriptionHtmlLayout;
import org.iatoki.judgels.play.views.html.layouts.headingWithActionAndBackLayout;
import org.iatoki.judgels.play.views.html.layouts.headingWithBackLayout;
import org.iatoki.judgels.play.views.html.layouts.subtabLayout;
import org.iatoki.judgels.play.views.html.layouts.tabLayout;
import play.i18n.Messages;

final class TrainingSessionControllerUtils {

    private TrainingSessionControllerUtils() {
        // prevent instantiation
    }

    static void appendTabLayout(LazyHtml content, Curriculum curriculum, CurriculumCourse curriculumCourse, Course course, CourseSession courseSession, Session session) {
        ImmutableList.Builder<InternalLink> tabLinksBuilder = ImmutableList.builder();
        tabLinksBuilder.add(new InternalLink(Messages.get("session.lessons"), routes.TrainingLessonController.viewLessons(curriculum.getId(), curriculumCourse.getId(), courseSession.getId())));
        tabLinksBuilder.add(new InternalLink(Messages.get("session.problems"), routes.TrainingProblemController.viewProblems(curriculum.getId(), curriculumCourse.getId(), courseSession.getId())));
        if (JerahmeelUtils.isGuest()) {
            tabLinksBuilder.add(new InternalLink(Messages.get("session.submissions"), routes.TrainingProgrammingSubmissionController.viewSubmissions(curriculum.getId(), curriculumCourse.getId(), courseSession.getId())));
        } else {
            tabLinksBuilder.add(new InternalLink(Messages.get("session.submissions"), routes.TrainingProgrammingSubmissionController.viewOwnSubmissions(curriculum.getId(), curriculumCourse.getId(), courseSession.getId())));
        }
        content.appendLayout(c -> tabLayout.render(tabLinksBuilder.build(), c));
        if (!session.getDescription().isEmpty()) {
            content.appendLayout(c -> descriptionHtmlLayout.render(session.getDescription(), c));
        }
        if (JerahmeelUtils.hasRole("admin")) {
            content.appendLayout(c -> headingWithActionAndBackLayout.render(
                            Messages.get("session.session") + " #" + session.getId() + ": " + session.getName(),
                            new InternalLink(Messages.get("commons.update"), routes.SessionController.editSessionGeneral(session.getId())),
                            new InternalLink(Messages.get("training.backTo") + " " + course.getName(), routes.TrainingSessionController.viewSessions(curriculum.getId(), curriculumCourse.getId())),
                            c)
            );
        } else {
            content.appendLayout(c -> headingWithBackLayout.render(
                            Messages.get("session.session") + " " + courseSession.getAlias() + ": " + session.getName(),
                            new InternalLink(Messages.get("training.backTo") + " " + course.getName(), routes.TrainingSessionController.viewSessions(curriculum.getId(), curriculumCourse.getId())),
                            c)
            );
        }
    }

    static void appendSubmissionSubtabLayout(LazyHtml content, Curriculum curriculum, CurriculumCourse curriculumCourse, Course course, CourseSession courseSession) {
        content.appendLayout(c -> subtabLayout.render(ImmutableList.of(
                        new InternalLink(Messages.get("training.submissions.programming"), routes.TrainingProgrammingSubmissionController.viewSubmissions(curriculum.getId(), curriculumCourse.getId(), courseSession.getId())),
                        new InternalLink(Messages.get("training.submissions.bundle"), routes.TrainingBundleSubmissionController.viewSubmissions(curriculum.getId(), curriculumCourse.getId(), courseSession.getId()))
                ), c)
        );
    }
}
