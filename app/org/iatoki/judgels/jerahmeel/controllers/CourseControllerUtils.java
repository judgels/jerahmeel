package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.views.html.layouts.descriptionLayout;
import org.iatoki.judgels.commons.views.html.layouts.headingLayout;
import org.iatoki.judgels.commons.views.html.layouts.headingWithActionLayout;
import org.iatoki.judgels.commons.views.html.layouts.tabLayout;
import org.iatoki.judgels.jerahmeel.Course;
import org.iatoki.judgels.jerahmeel.JerahmeelUtils;
import play.i18n.Messages;

public final class CourseControllerUtils {

    private CourseControllerUtils() {
        // prevent instantiation
    }

    static void appendViewLayout(LazyHtml content, Course course) {
        content.appendLayout(c -> descriptionLayout.render(course.getDescription(), c));
        if (JerahmeelUtils.hasRole("admin")) {
            content.appendLayout(c -> headingWithActionLayout.render(Messages.get("course.course") + " #" + course.getId() + ": " + course.getName(), new InternalLink(Messages.get("commons.update"), routes.CourseController.updateCourseGeneral(course.getId())), c));
        } else {
            content.appendLayout(c -> headingLayout.render(Messages.get("course.course") + " #" + course.getId() + ": " + course.getName(), c));
        }
    }

    static void appendUpdateLayout(LazyHtml content, Course course) {
        content.appendLayout(c -> tabLayout.render(ImmutableList.of(
                    new InternalLink(Messages.get("course.update"), routes.CourseController.updateCourseGeneral(course.getId())),
                    new InternalLink(Messages.get("course.sessions"), routes.CourseController.jumpToSessions(course.getId()))
              ), c)
        );
        content.appendLayout(c -> headingLayout.render(Messages.get("course.course") + " #" + course.getId() + ": " + course.getName(), c));
    }
}
