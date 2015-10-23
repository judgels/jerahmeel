package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.jerahmeel.ProblemSet;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.views.html.layouts.subtabLayout;
import play.i18n.Messages;

final class ProblemSetSubmissionControllerUtils {

    private ProblemSetSubmissionControllerUtils() {
        // prevent instantiation
    }

    static void appendSubtabLayout(LazyHtml content, ProblemSet problemSet) {
        content.appendLayout(c -> subtabLayout.render(ImmutableList.of(
                        new InternalLink(Messages.get("archive.problemSet.submissions.programming"), routes.ProblemSetProgrammingSubmissionController.viewOwnSubmissions(problemSet.getId())),
                        new InternalLink(Messages.get("archive.problemSet.submissions.bundle"), routes.ProblemSetBundleSubmissionController.viewOwnSubmissions(problemSet.getId()))
                ), c)
        );
    }
}
