package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.jerahmeel.Curriculum;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.views.html.layouts.headingLayout;
import org.iatoki.judgels.play.views.html.layouts.tabLayout;
import play.i18n.Messages;

final class CurriculumControllerUtils {

    private CurriculumControllerUtils() {
        // prevent instantiation
    }

    static void appendTabLayout(LazyHtml content, Curriculum curriculum) {
        content.appendLayout(c -> tabLayout.render(ImmutableList.of(
                    new InternalLink(Messages.get("curriculum.update"), routes.CurriculumController.editCurriculumGeneral(curriculum.getId())),
                    new InternalLink(Messages.get("curriculum.courses"), routes.CurriculumController.jumpToCourses(curriculum.getId()))
              ), c)
        );
        content.appendLayout(c -> headingLayout.render(Messages.get("curriculum.curriculum") + " #" + curriculum.getId() + ": " + curriculum.getName(), c));
    }

    static ImmutableList.Builder<InternalLink> getBreadcrumbsBuilder() {
        ImmutableList.Builder<InternalLink> breadcrumbsBuilder = ImmutableList.builder();
        breadcrumbsBuilder.add(new InternalLink(Messages.get("curriculum.curriculums"), routes.CurriculumController.viewCurriculums()));

        return breadcrumbsBuilder;
    }
}
