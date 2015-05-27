package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.views.html.layouts.descriptionLayout;
import org.iatoki.judgels.commons.views.html.layouts.headingLayout;
import org.iatoki.judgels.commons.views.html.layouts.headingWithActionLayout;
import org.iatoki.judgels.commons.views.html.layouts.tabLayout;
import org.iatoki.judgels.jerahmeel.Curriculum;
import org.iatoki.judgels.jerahmeel.JerahmeelUtils;
import play.i18n.Messages;

public final class CurriculumControllerUtils {

    private CurriculumControllerUtils() {
        // prevent instantiation
    }

    static void appendViewLayout(LazyHtml content, Curriculum curriculum) {
        content.appendLayout(c -> descriptionLayout.render(curriculum.getDescription(), c));
        if (JerahmeelUtils.hasRole("admin")) {
            content.appendLayout(c -> headingWithActionLayout.render(Messages.get("curriculum.curriculum") + " #" + curriculum.getId() + ": " + curriculum.getName(), new InternalLink(Messages.get("commons.update"), routes.CurriculumController.updateCurriculumGeneral(curriculum.getId())), c));
        } else {
            content.appendLayout(c -> headingLayout.render(Messages.get("curriculum.curriculum") + " #" + curriculum.getId() + ": " + curriculum.getName(), c));
        }
    }

    static void appendUpdateLayout(LazyHtml content, Curriculum curriculum) {
        content.appendLayout(c -> tabLayout.render(ImmutableList.of(
                    new InternalLink(Messages.get("curriculum.update"), routes.CurriculumController.updateCurriculumGeneral(curriculum.getId())),
                    new InternalLink(Messages.get("curriculum.courses"), routes.CurriculumController.jumpToCourses(curriculum.getId()))
              ), c)
        );
        content.appendLayout(c -> headingLayout.render(Messages.get("curriculum.curriculum") + " #" + curriculum.getId() + ": " + curriculum.getName(), c));
    }
}
