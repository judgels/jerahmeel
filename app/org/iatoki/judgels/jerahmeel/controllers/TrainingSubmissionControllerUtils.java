package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.jerahmeel.Course;
import org.iatoki.judgels.jerahmeel.CourseSession;
import org.iatoki.judgels.jerahmeel.Curriculum;
import org.iatoki.judgels.jerahmeel.CurriculumCourse;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.views.html.layouts.subtabLayout;
import play.i18n.Messages;

final class TrainingSubmissionControllerUtils {

    private TrainingSubmissionControllerUtils() {
        // prevent instantiation
    }

    static void appendSubtabLayout(LazyHtml content, Curriculum curriculum, CurriculumCourse curriculumCourse, Course course, CourseSession courseSession) {
        content.appendLayout(c -> subtabLayout.render(ImmutableList.of(
                        new InternalLink(Messages.get("training.submissions.programming"), routes.TrainingProgrammingSubmissionController.viewSubmissions(curriculum.getId(), curriculumCourse.getId(), courseSession.getId())),
                        new InternalLink(Messages.get("training.submissions.bundle"), routes.TrainingBundleSubmissionController.viewSubmissions(curriculum.getId(), curriculumCourse.getId(), courseSession.getId()))
                ), c)
        );
    }
}
