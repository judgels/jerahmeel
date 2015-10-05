package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.jerahmeel.Archive;
import org.iatoki.judgels.jerahmeel.ArchiveNotFoundException;
import org.iatoki.judgels.jerahmeel.ArchiveWithScore;
import org.iatoki.judgels.jerahmeel.JerahmeelUtils;
import org.iatoki.judgels.jerahmeel.ProblemSetWithScore;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authorized;
import org.iatoki.judgels.jerahmeel.controllers.securities.GuestView;
import org.iatoki.judgels.jerahmeel.controllers.securities.HasRole;
import org.iatoki.judgels.jerahmeel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jerahmeel.forms.ArchiveUpsertForm;
import org.iatoki.judgels.jerahmeel.services.ArchiveService;
import org.iatoki.judgels.jerahmeel.services.ProblemSetService;
import org.iatoki.judgels.jerahmeel.views.html.archive.createArchiveView;
import org.iatoki.judgels.jerahmeel.views.html.archive.listArchivesAndProblemSetsView;
import org.iatoki.judgels.jerahmeel.views.html.archive.listArchivesView;
import org.iatoki.judgels.jerahmeel.views.html.archive.updateArchiveGeneralView;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.headingLayout;
import org.iatoki.judgels.play.views.html.layouts.headingWithActionAndBackLayout;
import org.iatoki.judgels.play.views.html.layouts.headingWithActionLayout;
import org.iatoki.judgels.play.views.html.layouts.headingWithActionsAndBackLayout;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
@Named
public final class ArchiveController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;

    private final ArchiveService archiveService;
    private final ProblemSetService problemSetService;

    @Inject
    public ArchiveController(ArchiveService archiveService, ProblemSetService problemSetService) {
        this.archiveService = archiveService;
        this.problemSetService = problemSetService;
    }

    @Authenticated(value = GuestView.class)
    @Transactional(readOnly = true)
    public Result index() throws ArchiveNotFoundException {
        return viewArchives(0);
    }

    @Authenticated(value = GuestView.class)
    @Transactional(readOnly = true)
    public Result viewArchives(long archiveId) throws ArchiveNotFoundException {
        return showListArchivesProblemSets(archiveId, 0, "timeUpdate", "desc", "");
    }

    @Authenticated(value = GuestView.class)
    @Transactional(readOnly = true)
    public Result listArchivesProblemSets(long archiveId, long pageIndex, String orderBy, String orderDir, String filterString) throws ArchiveNotFoundException {
        return showListArchivesProblemSets(archiveId, pageIndex, orderBy, orderDir, filterString);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = "admin")
    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result createArchive() {
        Form<ArchiveUpsertForm> archiveUpsertForm = Form.form(ArchiveUpsertForm.class);

        return showCreateArchive(archiveUpsertForm);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = "admin")
    @Transactional
    @RequireCSRFCheck
    public Result postCreateArchive() {
        Form<ArchiveUpsertForm> archiveUpsertForm = Form.form(ArchiveUpsertForm.class).bindFromRequest();

        if (formHasErrors(archiveUpsertForm)) {
            return showCreateArchive(archiveUpsertForm);
        }

        ArchiveUpsertForm archiveUpsertData = archiveUpsertForm.get();
        archiveService.createArchive(archiveUpsertData.parentJid, archiveUpsertData.name, archiveUpsertData.description);

        return redirect(routes.ArchiveController.index());
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = "admin")
    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result updateArchiveGeneralConfig(long archiveId) throws ArchiveNotFoundException {
        Archive archive = archiveService.findArchiveById(archiveId);
        ArchiveUpsertForm archiveUpsertData = new ArchiveUpsertForm();
        if (archive.getParentArchive() != null) {
            archiveUpsertData.parentJid = archive.getParentArchive().getJid();
        }
        archiveUpsertData.name = archive.getName();
        archiveUpsertData.description = archive.getDescription();

        Form<ArchiveUpsertForm> archiveUpsertForm = Form.form(ArchiveUpsertForm.class).fill(archiveUpsertData);

        return showUpdateArchiveGeneral(archiveUpsertForm, archive);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = "admin")
    @Transactional
    @RequireCSRFCheck
    public Result postUpdateArchiveGeneralConfig(long archiveId) throws ArchiveNotFoundException {
        Archive archive = archiveService.findArchiveById(archiveId);
        Form<ArchiveUpsertForm> archiveUpsertForm = Form.form(ArchiveUpsertForm.class).bindFromRequest();

        if (formHasErrors(archiveUpsertForm)) {
            return showUpdateArchiveGeneral(archiveUpsertForm, archive);
        }

        ArchiveUpsertForm archiveUpsertData = archiveUpsertForm.get();
        archiveService.updateArchive(archive.getJid(), archiveUpsertData.parentJid, archiveUpsertData.name, archiveUpsertData.description);

        return redirect(routes.ArchiveController.index());
    }

    private Result showListArchivesProblemSets(long archiveId, long pageIndex, String orderBy, String orderDir, String filterString) throws ArchiveNotFoundException {
        Archive currentArchive;
        Archive parentArchive;
        List<ArchiveWithScore> childArchivesWithScore;
        if (archiveId == 0) {
            currentArchive = null;
            parentArchive = null;
            childArchivesWithScore = archiveService.getChildArchivesWithScore("", IdentityUtils.getUserJid());
        } else {
            currentArchive = archiveService.findArchiveById(archiveId);
            parentArchive = currentArchive.getParentArchive();
            childArchivesWithScore = archiveService.getChildArchivesWithScore(currentArchive.getJid(), IdentityUtils.getUserJid());
        }

        LazyHtml content;
        if (currentArchive != null) {
            Page<ProblemSetWithScore> pageOfProblemSets = problemSetService.getPageOfProblemSets(currentArchive, IdentityUtils.getUserJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

            content = new LazyHtml(listArchivesAndProblemSetsView.render(currentArchive, childArchivesWithScore, pageOfProblemSets, orderBy, orderDir, filterString));
        } else {
            content = new LazyHtml(listArchivesView.render(currentArchive, childArchivesWithScore));
        }

        if (currentArchive != null) {
            final String parentArchiveName;
            final long parentArchiveId;
            if (parentArchive == null) {
                parentArchiveName = Messages.get("archive.home");
                parentArchiveId = 0;
            } else {
                parentArchiveName = parentArchive.getName();
                parentArchiveId = parentArchive.getId();
            }

            if (JerahmeelUtils.hasRole("admin")) {
                ImmutableList.Builder<InternalLink> actionsBuilder = ImmutableList.builder();
                actionsBuilder.add(new InternalLink(Messages.get("commons.update"), routes.ArchiveController.updateArchiveGeneralConfig(archiveId)));
                actionsBuilder.add(new InternalLink(Messages.get("archive.create"), routes.ArchiveController.createArchive()));
                actionsBuilder.add(new InternalLink(Messages.get("archive.problemSet.create"), routes.ProblemSetController.createProblemSet(currentArchive.getId())));

                content.appendLayout(c -> headingWithActionsAndBackLayout.render(Messages.get("archive.archive") + " " + currentArchive.getName(), actionsBuilder.build(), new InternalLink(Messages.get("archive.backTo") + " " + parentArchiveName, routes.ArchiveController.viewArchives(parentArchiveId)), c));
            } else {
                content.appendLayout(c -> headingWithActionAndBackLayout.render(Messages.get("archive.archive") + " " + currentArchive.getName(), new InternalLink(Messages.get("archive.problemSet.create"), routes.ProblemSetController.createProblemSet(currentArchive.getId())), new InternalLink(Messages.get("archive.backTo") + " " + parentArchiveName, routes.ArchiveController.viewArchives(parentArchiveId)), c));
            }
        } else {
            if (JerahmeelUtils.hasRole("admin")) {
                content.appendLayout(c -> headingWithActionLayout.render(Messages.get("archive.archives"), new InternalLink(Messages.get("commons.create"), routes.ArchiveController.createArchive()), c));
            } else {
                content.appendLayout(c -> headingLayout.render(Messages.get("archive.archives"), c));
            }
        }

        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);

        ImmutableList.Builder<InternalLink> breadcrumbsBuilder = ImmutableList.builder();
        ArchiveControllerUtils.fillBreadcrumbsBuilder(breadcrumbsBuilder, currentArchive);
        ArchiveControllerUtils.appendBreadcrumbsLayout(content, breadcrumbsBuilder.build());
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Archives");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    private Result showCreateArchive(Form<ArchiveUpsertForm> archiveUpsertForm) {
        LazyHtml content = new LazyHtml(createArchiveView.render(archiveUpsertForm, archiveService.getAllArchives()));
        content.appendLayout(c -> headingLayout.render(Messages.get("archive.create"), c));
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        ArchiveControllerUtils.appendBreadcrumbsLayout(content,
                new InternalLink(Messages.get("archive.create"), routes.ArchiveController.createArchive())
        );
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Archive - Create");
        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    private Result showUpdateArchiveGeneral(Form<ArchiveUpsertForm> archiveUpsertForm, Archive archive) {
        LazyHtml content = new LazyHtml(updateArchiveGeneralView.render(archiveUpsertForm, archive.getId(), archiveService.getAllArchives().stream().filter(f -> !f.containsJidInHierarchy(archive.getJid())).collect(Collectors.toList())));
        ArchiveControllerUtils.appendUpdateLayout(content, archive);
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        ArchiveControllerUtils.appendBreadcrumbsLayout(content,
                new InternalLink(Messages.get("archive.update"), routes.ArchiveController.updateArchiveGeneralConfig(archive.getId())),
                new InternalLink(Messages.get("archive.config.general"), routes.ArchiveController.updateArchiveGeneralConfig(archive.getId()))
        );
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Archive - Update");
        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }
}
