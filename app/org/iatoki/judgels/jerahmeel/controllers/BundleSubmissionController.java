package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.iatoki.judgels.FileSystemProvider;
import org.iatoki.judgels.jerahmeel.JerahmeelUtils;
import org.iatoki.judgels.jerahmeel.Session;
import org.iatoki.judgels.jerahmeel.SessionProblem;
import org.iatoki.judgels.jerahmeel.config.BundleSubmissionLocalFileSystemProvider;
import org.iatoki.judgels.jerahmeel.config.BundleSubmissionRemoteFileSystemProvider;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.securities.GuestView;
import org.iatoki.judgels.jerahmeel.controllers.securities.HasRole;
import org.iatoki.judgels.jerahmeel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jerahmeel.services.SessionProblemService;
import org.iatoki.judgels.jerahmeel.services.SessionService;
import org.iatoki.judgels.jerahmeel.services.impls.JidCacheServiceImpl;
import org.iatoki.judgels.jerahmeel.views.html.submission.bundle.listOwnSubmissionsView;
import org.iatoki.judgels.jerahmeel.views.html.submission.bundle.listSubmissionsView;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.heading3Layout;
import org.iatoki.judgels.sandalphon.BundleAnswer;
import org.iatoki.judgels.sandalphon.BundleDetailResult;
import org.iatoki.judgels.sandalphon.BundleSubmission;
import org.iatoki.judgels.sandalphon.BundleSubmissionNotFoundException;
import org.iatoki.judgels.sandalphon.services.BundleSubmissionService;
import org.iatoki.judgels.sandalphon.views.html.problem.bundle.submission.bundleSubmissionView;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.mvc.Result;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
@Named
public final class BundleSubmissionController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;

    private final FileSystemProvider bundleSubmissionLocalFileSystemProvider;
    private final FileSystemProvider bundleSubmissionRemoteFileSystemProvider;
    private final BundleSubmissionService bundleSubmissionService;
    private final SessionProblemService sessionProblemService;
    private final SessionService sessionService;

    @Inject
    public BundleSubmissionController(@BundleSubmissionLocalFileSystemProvider FileSystemProvider bundleSubmissionLocalFileSystemProvider, @BundleSubmissionRemoteFileSystemProvider @Nullable FileSystemProvider bundleSubmissionRemoteFileSystemProvider, BundleSubmissionService bundleSubmissionService, SessionProblemService sessionProblemService, SessionService sessionService) {
        this.bundleSubmissionLocalFileSystemProvider = bundleSubmissionLocalFileSystemProvider;
        this.bundleSubmissionRemoteFileSystemProvider = bundleSubmissionRemoteFileSystemProvider;
        this.bundleSubmissionService = bundleSubmissionService;
        this.sessionProblemService = sessionProblemService;
        this.sessionService = sessionService;
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional(readOnly = true)
    public Result viewOwnSubmissions() {
        return listOwnSubmissions(0, "id", "desc");
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional(readOnly = true)
    public Result listOwnSubmissions(long pageIndex, String orderBy, String orderDir) {
        Page<BundleSubmission> pageOfBundleSubmissions = bundleSubmissionService.getPageOfBundleSubmissions(pageIndex, PAGE_SIZE, orderBy, orderDir, IdentityUtils.getUserJid(), null, null);
        Map<String, String> sessionJidToNameMap = sessionService.getSessionJidToNameMapBySessionJids(pageOfBundleSubmissions.getData().stream().map(s -> s.getContainerJid()).collect(Collectors.toList()));

        LazyHtml content = new LazyHtml(listOwnSubmissionsView.render(pageOfBundleSubmissions, sessionJidToNameMap, pageIndex, orderBy, orderDir));
        SubmissionControllerUtils.appendOwnSubtabLayout(content);
        SubmissionControllerUtils.appendTabLayout(content);
        content.appendLayout(c -> heading3Layout.render(Messages.get("submission.submissions"), c));
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content,
                new InternalLink(Messages.get("submission.own"), routes.SubmissionController.jumpToOwnSubmissions()),
                new InternalLink(Messages.get("submission.bundle"), routes.BundleSubmissionController.viewOwnSubmissions())
        );
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Submissions - Bundle");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional(readOnly = true)
    public Result viewOwnSubmission(long bundleSubmissionId) throws BundleSubmissionNotFoundException {
        BundleSubmission bundleSubmission = bundleSubmissionService.findBundleSubmissionById(bundleSubmissionId);

        if (!JerahmeelControllerUtils.getInstance().isAdmin() && bundleSubmission.getAuthorJid().equals(IdentityUtils.getUserJid())) {
            return notFound();
        }

        LazyHtml content = getViewSubmissionContent(bundleSubmission);

        SubmissionControllerUtils.appendOwnSubtabLayout(content);
        SubmissionControllerUtils.appendTabLayout(content);
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content,
                new InternalLink(Messages.get("submission.own"), routes.SubmissionController.jumpToOwnSubmissions()),
                new InternalLink(Messages.get("submission.bundle"), routes.BundleSubmissionController.viewOwnSubmissions()),
                new InternalLink(bundleSubmission.getId() + "", routes.BundleSubmissionController.viewSubmission(bundleSubmission.getId()))
        );
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Submissions - Bundle - View");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    @Authenticated(value = GuestView.class)
    @Transactional(readOnly = true)
    public Result viewSubmissions() {
        return listSubmissions(0, "id", "desc");
    }

    @Authenticated(value = GuestView.class)
    @Transactional(readOnly = true)
    public Result listSubmissions(long pageIndex, String orderBy, String orderDir) {
        Page<BundleSubmission> pageOfBundleSubmissions = bundleSubmissionService.getPageOfBundleSubmissions(pageIndex, PAGE_SIZE, orderBy, orderDir, null, null, null);
        Map<String, String> sessionJidToNameMap = sessionService.getSessionJidToNameMapBySessionJids(pageOfBundleSubmissions.getData().stream().map(s -> s.getContainerJid()).collect(Collectors.toList()));

        LazyHtml content = new LazyHtml(listSubmissionsView.render(pageOfBundleSubmissions, sessionJidToNameMap, pageIndex, orderBy, orderDir, JerahmeelControllerUtils.getInstance().isAdmin()));
        SubmissionControllerUtils.appendAllSubtabLayout(content);
        if (!JerahmeelUtils.isGuest()) {
            SubmissionControllerUtils.appendTabLayout(content);
        }
        content.appendLayout(c -> heading3Layout.render(Messages.get("submission.submissions"), c));
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content,
                new InternalLink(Messages.get("submission.all"), routes.SubmissionController.jumpToAllSubmissions()),
                new InternalLink(Messages.get("submission.bundle"), routes.BundleSubmissionController.viewSubmissions())
        );
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Submissions - Bundle");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional(readOnly = true)
    public Result viewSubmission(long bundleSubmissionId) throws BundleSubmissionNotFoundException {
        BundleSubmission bundleSubmission = bundleSubmissionService.findBundleSubmissionById(bundleSubmissionId);

        if (!JerahmeelControllerUtils.getInstance().isAdmin() && bundleSubmission.getAuthorJid().equals(IdentityUtils.getUserJid())) {
            return notFound();
        }

        LazyHtml content = getViewSubmissionContent(bundleSubmission);

        SubmissionControllerUtils.appendOwnSubtabLayout(content);
        SubmissionControllerUtils.appendTabLayout(content);
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content,
                new InternalLink(Messages.get("submission.all"), routes.SubmissionController.jumpToAllSubmissions()),
                new InternalLink(Messages.get("submission.bundle"), routes.BundleSubmissionController.viewSubmissions()),
                new InternalLink(bundleSubmission.getId() + "", routes.BundleSubmissionController.viewSubmission(bundleSubmission.getId()))
        );
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Submissions - Bundle - View");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    private LazyHtml getViewSubmissionContent(BundleSubmission bundleSubmission) {
        Session session = sessionService.findSessionByJid(bundleSubmission.getContainerJid());

        BundleAnswer bundleAnswer;
        try {
            bundleAnswer = bundleSubmissionService.createBundleAnswerFromPastSubmission(bundleSubmissionLocalFileSystemProvider, bundleSubmissionRemoteFileSystemProvider, bundleSubmission.getJid());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        SessionProblem sessionProblem = sessionProblemService.findSessionProblemBySessionJidAndProblemJid(session.getJid(), bundleSubmission.getProblemJid());
        String sessionProblemAlias = sessionProblem.getAlias();
        String sessionProblemName = JidCacheServiceImpl.getInstance().getDisplayName(sessionProblem.getProblemJid());

        return new LazyHtml(bundleSubmissionView.render(bundleSubmission, new Gson().fromJson(bundleSubmission.getLatestDetails(), new TypeToken<Map<String, BundleDetailResult>>() { }.getType()), bundleAnswer, JidCacheServiceImpl.getInstance().getDisplayName(bundleSubmission.getAuthorJid()), sessionProblemAlias, sessionProblemName, session.getName()));
    }

    private void appendBreadcrumbsLayout(LazyHtml content, InternalLink... lastLinks) {
        ImmutableList.Builder<InternalLink> breadcrumbsBuilder = SubmissionControllerUtils.getBreadcrumbsBuilder();
        breadcrumbsBuilder.add(lastLinks);

        JerahmeelControllerUtils.getInstance().appendBreadcrumbsLayout(content, breadcrumbsBuilder.build());
    }
}
