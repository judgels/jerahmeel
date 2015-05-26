package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.views.html.layouts.headingWithActionLayout;
import org.iatoki.judgels.commons.views.html.layouts.tabLayout;
import org.iatoki.judgels.jerahmeel.Curriculum;
import play.i18n.Messages;

public final class CurriculumControllerUtils {

    private CurriculumControllerUtils() {
        // prevent instantiation
    }

    static void appendUpdateTabLayout(LazyHtml content, Curriculum curriculum) {
        content.appendLayout(c -> tabLayout.render(ImmutableList.of(
                    new InternalLink(Messages.get("curriculum.update"), routes.CurriculumController.updateCurriculumGeneral(curriculum.getId())),
                    new InternalLink(Messages.get("curriculum.courses"), routes.CurriculumCourseController.viewCourses(curriculum.getId()))
              ), c)
        );
        content.appendLayout(c -> headingWithActionLayout.render(Messages.get("curriculum.curriculum") + " #" + curriculum.getId() + ": " + curriculum.getName(), new InternalLink(Messages.get("commons.enter"), routes.CurriculumController.viewCurriculum(curriculum.getId())), c));
    }
}
