package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.jerahmeel.Archive;
import org.iatoki.judgels.jerahmeel.ArchiveNotFoundException;
import org.iatoki.judgels.jerahmeel.JerahmeelUtils;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.securities.GuestView;
import org.iatoki.judgels.jerahmeel.controllers.securities.HasRole;
import org.iatoki.judgels.jerahmeel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jerahmeel.forms.ProblemSetCreateForm;
import org.iatoki.judgels.jerahmeel.services.ArchiveService;
import org.iatoki.judgels.jerahmeel.services.ProblemSetService;
import org.iatoki.judgels.jerahmeel.views.html.archive.problemset.createProblemSetView;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.headingLayout;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.Stack;

public final class ProblemSetController extends AbstractJudgelsController {

    private final ArchiveService archiveService;
    private final ProblemSetService problemSetService;

    @Inject
    public ProblemSetController(ArchiveService archiveService, ProblemSetService problemSetService) {
        this.archiveService = archiveService;
        this.problemSetService = problemSetService;
    }

    @Authenticated(value = GuestView.class)
    public Result jumpToProblems(long problemSetId) {
        if (JerahmeelUtils.hasRole("admin")) {
            return redirect(routes.ProblemSetProblemController.viewProblemSetProblems(problemSetId));
        }

        return redirect(routes.ProblemSetProblemController.viewVisibleProblemSetProblems(problemSetId));
    }

    public Result jumpToSubmissions(long problemSetId) {
        return redirect(routes.ProblemSetProgrammingSubmissionController.viewSubmissions(problemSetId));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result createProblemSet(long archiveId) throws ArchiveNotFoundException {
        Archive archive = archiveService.findArchiveById(archiveId);

        Form<ProblemSetCreateForm> problemSetCreateForm = Form.form(ProblemSetCreateForm.class);

        return showCreateProblemSet(archive, problemSetCreateForm);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    @RequireCSRFCheck
    public Result postCreateProblemSet(long archiveId) throws ArchiveNotFoundException {
        Archive archive = archiveService.findArchiveById(archiveId);

        Form<ProblemSetCreateForm> problemSetCreateForm = Form.form(ProblemSetCreateForm.class).bindFromRequest();

        if (formHasErrors(problemSetCreateForm)) {
            return showCreateProblemSet(archive, problemSetCreateForm);
        }

        ProblemSetCreateForm problemSetCreateData = problemSetCreateForm.get();
        problemSetService.createProblemSet(archive.getJid(), problemSetCreateData.name, problemSetCreateData.description);

        return redirect(routes.ArchiveController.viewArchives(archive.getId()));
    }

    private Result showCreateProblemSet(Archive archive, Form<ProblemSetCreateForm> problemSetCreateForm) {
        LazyHtml content = new LazyHtml(createProblemSetView.render(archive, problemSetCreateForm));
        content.appendLayout(c -> headingLayout.render(Messages.get("archive.problemSet.create"), c));
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        ImmutableList.Builder<InternalLink> internalLinkBuilder = ImmutableList.builder();
        if (archive != null) {
            Stack<InternalLink> internalLinkStack = new Stack<>();
            Archive currentParent = archive;
            while (currentParent != null) {
                internalLinkStack.push(new InternalLink(currentParent.getName(), routes.ArchiveController.viewArchives(currentParent.getId())));
                currentParent = currentParent.getParentArchive();
            }

            while (!internalLinkStack.isEmpty()) {
                internalLinkBuilder.add(internalLinkStack.pop());
            }
        }
        internalLinkBuilder.add(new InternalLink(Messages.get("archive.problemSet.create"), routes.ProblemSetController.createProblemSet(archive.getId())));
        ArchiveControllerUtils.appendBreadcrumbsLayout(content, internalLinkBuilder.build());
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Archive - Problem Set - Create");
        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }
}
