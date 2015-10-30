package org.iatoki.judgels.jerahmeel.controllers;

import org.iatoki.judgels.jerahmeel.Curriculum;
import org.iatoki.judgels.jerahmeel.JerahmeelUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.views.html.layouts.descriptionHtmlLayout;
import org.iatoki.judgels.play.views.html.layouts.headingWithActionAndBackLayout;
import org.iatoki.judgels.play.views.html.layouts.headingWithBackLayout;
import play.i18n.Messages;

final class TrainingCurriculumControllerUtils {

    private TrainingCurriculumControllerUtils() {
        // prevent instantiation
    }

    static void appendTabLayout(LazyHtml content, Curriculum curriculum) {
        if (!curriculum.getDescription().isEmpty()) {
            content.appendLayout(c -> descriptionHtmlLayout.render(curriculum.getDescription(), c));
        }
        if (JerahmeelUtils.hasRole("admin")) {
            content.appendLayout(c -> headingWithActionAndBackLayout.render(
                    Messages.get("curriculum.curriculum") + " #" + curriculum.getId() + ": " + curriculum.getName(),
                    new InternalLink(Messages.get("commons.update"), routes.CurriculumController.editCurriculumGeneral(curriculum.getId())),
                    new InternalLink(Messages.get("training.backToHome"), routes.TrainingController.index()),
                    c)
            );
        } else {
            content.appendLayout(c -> headingWithBackLayout.render(curriculum.getName(),
                    new InternalLink(Messages.get("training.backToHome"), routes.TrainingController.index()),
                    c)
            );
        }
    }
}
