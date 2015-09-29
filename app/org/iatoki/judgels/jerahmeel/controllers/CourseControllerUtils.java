package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.jerahmeel.Course;
import org.iatoki.judgels.jerahmeel.Curriculum;
import org.iatoki.judgels.jerahmeel.CurriculumCourse;
import org.iatoki.judgels.jerahmeel.JerahmeelUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.views.html.layouts.descriptionLayout;
import org.iatoki.judgels.play.views.html.layouts.headingLayout;
import org.iatoki.judgels.play.views.html.layouts.headingWithActionAndBackLayout;
import org.iatoki.judgels.play.views.html.layouts.headingWithBackLayout;
import org.iatoki.judgels.play.views.html.layouts.tabLayout;
import play.i18n.Messages;

final class CourseControllerUtils {

    private CourseControllerUtils() {
        // prevent instantiation
    }

    static void appendViewLayout(LazyHtml content, Curriculum curriculum, CurriculumCourse curriculumCourse, Course course) {
        content.appendLayout(c -> descriptionLayout.render(course.getDescription(), c));
        if (JerahmeelUtils.hasRole("admin")) {
            content.appendLayout(c -> headingWithActionAndBackLayout.render(
                    Messages.get("course.course") + " #" + course.getId() + ": " + course.getName(),
                    new InternalLink(Messages.get("commons.update"), routes.CourseController.editCourseGeneral(course.getId())),
                    new InternalLink(Messages.get("training.backTo") + " " + curriculum.getName(), routes.TrainingCourseController.viewCourses(curriculum.getId())),
                    c)
            );
        } else if (curriculumCourse.isCompleteable()) {
            content.appendLayout(c -> headingWithBackLayout.render(Messages.get("course.course") + " " + curriculumCourse.getAlias() + ": " + course.getName(),
                    new InternalLink(Messages.get("training.backTo") + " " + curriculum.getName(), routes.TrainingCourseController.viewCourses(curriculum.getId())),
                    c)
            );
        } else {
            content.appendLayout(c -> headingWithBackLayout.render(course.getName(),
                    new InternalLink(Messages.get("training.backTo") + " " + curriculum.getName(), routes.TrainingCourseController.viewCourses(curriculum.getId())),
                    c)
            );
        }
    }

    static void appendUpdateLayout(LazyHtml content, Course course) {
        content.appendLayout(c -> tabLayout.render(ImmutableList.of(
                    new InternalLink(Messages.get("course.update"), routes.CourseController.editCourseGeneral(course.getId())),
                    new InternalLink(Messages.get("course.sessions"), routes.CourseController.jumpToSessions(course.getId()))
              ), c)
        );
        content.appendLayout(c -> headingLayout.render(Messages.get("course.course") + " #" + course.getId() + ": " + course.getName(), c));
    }

    static ImmutableList.Builder<InternalLink> getBreadcrumbsBuilder() {
        ImmutableList.Builder<InternalLink> breadcrumbsBuilder = ImmutableList.builder();
        breadcrumbsBuilder.add(new InternalLink(Messages.get("course.courses"), routes.CourseController.viewCourses()));

        return breadcrumbsBuilder;
    }
}
