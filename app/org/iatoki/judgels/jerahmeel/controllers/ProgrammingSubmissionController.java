package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.FileSystemProvider;
import org.iatoki.judgels.api.sandalphon.SandalphonResourceDisplayNameUtils;
import org.iatoki.judgels.gabriel.GradingLanguageRegistry;
import org.iatoki.judgels.gabriel.SubmissionSource;
import org.iatoki.judgels.jerahmeel.JerahmeelUtils;
import org.iatoki.judgels.jerahmeel.Session;
import org.iatoki.judgels.jerahmeel.SessionProblem;
import org.iatoki.judgels.jerahmeel.config.ProgrammingSubmissionLocalFileSystemProvider;
import org.iatoki.judgels.jerahmeel.config.ProgrammingSubmissionRemoteFileSystemProvider;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.securities.GuestView;
import org.iatoki.judgels.jerahmeel.controllers.securities.HasRole;
import org.iatoki.judgels.jerahmeel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jerahmeel.services.ProblemSetService;
import org.iatoki.judgels.jerahmeel.services.SessionProblemService;
import org.iatoki.judgels.jerahmeel.services.SessionService;
import org.iatoki.judgels.jerahmeel.services.impls.JidCacheServiceImpl;
import org.iatoki.judgels.jerahmeel.views.html.submission.programming.listOwnSubmissionsView;
import org.iatoki.judgels.jerahmeel.views.html.submission.programming.listSubmissionsView;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.heading3Layout;
import org.iatoki.judgels.sandalphon.ProgrammingSubmission;
import org.iatoki.judgels.sandalphon.ProgrammingSubmissionNotFoundException;
import org.iatoki.judgels.sandalphon.ProgrammingSubmissionUtils;
import org.iatoki.judgels.sandalphon.adapters.GradingEngineAdapterRegistry;
import org.iatoki.judgels.sandalphon.services.ProgrammingSubmissionService;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.mvc.Result;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
@Named
public final class ProgrammingSubmissionController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;

    private final ProblemSetService problemSetService;
    private final FileSystemProvider programmingSubmissionLocalFileSystemProvider;
    private final FileSystemProvider programmingSubmissionRemoteFileSystemProvider;
    private final ProgrammingSubmissionService programmingSubmissionService;
    private final SessionProblemService sessionProblemService;
    private final SessionService sessionService;

    @Inject
    public ProgrammingSubmissionController(ProblemSetService problemSetService, @ProgrammingSubmissionLocalFileSystemProvider FileSystemProvider programmingSubmissionLocalFileSystemProvider, @ProgrammingSubmissionRemoteFileSystemProvider @Nullable FileSystemProvider programmingSubmissionRemoteFileSystemProvider, ProgrammingSubmissionService programmingSubmissionService, SessionProblemService sessionProblemService, SessionService sessionService) {
        this.problemSetService = problemSetService;
        this.programmingSubmissionLocalFileSystemProvider = programmingSubmissionLocalFileSystemProvider;
        this.programmingSubmissionRemoteFileSystemProvider = programmingSubmissionRemoteFileSystemProvider;
        this.programmingSubmissionService = programmingSubmissionService;
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
        Page<ProgrammingSubmission> pageOfProgrammingSubmissions = programmingSubmissionService.getPageOfProgrammingSubmissions(pageIndex, PAGE_SIZE, orderBy, orderDir, IdentityUtils.getUserJid(), null, null);
        Map<String, String> jidToNameMap = SubmissionControllerUtils.getJidToNameMap(sessionService, problemSetService, pageOfProgrammingSubmissions.getData().stream().map(s -> s.getContainerJid()).collect(Collectors.toList()));
        Map<String, String> gradingLanguageToNameMap = GradingLanguageRegistry.getInstance().getGradingLanguages();

        LazyHtml content = new LazyHtml(listOwnSubmissionsView.render(pageOfProgrammingSubmissions, jidToNameMap, gradingLanguageToNameMap, pageIndex, orderBy, orderDir));
        SubmissionControllerUtils.appendOwnSubtabLayout(content);
        SubmissionControllerUtils.appendTabLayout(content);
        content.appendLayout(c -> heading3Layout.render(Messages.get("submission.submissions"), c));
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content,
                new InternalLink(Messages.get("submission.own"), routes.SubmissionController.jumpToOwnSubmissions()),
                new InternalLink(Messages.get("submission.programming"), routes.ProgrammingSubmissionController.viewOwnSubmissions())
        );
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Submissions - Programming");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional(readOnly = true)
    public Result viewOwnSubmission(long programmingSubmissionId) throws ProgrammingSubmissionNotFoundException {
        ProgrammingSubmission programmingSubmission = programmingSubmissionService.findProgrammingSubmissionById(programmingSubmissionId);

        if (!(JerahmeelControllerUtils.getInstance().isAdmin() || programmingSubmission.getAuthorJid().equals(IdentityUtils.getUserJid()))) {
            return notFound();
        }

        LazyHtml content = getViewSubmissionContent(programmingSubmission);

        SubmissionControllerUtils.appendTabLayout(content);
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content,
                new InternalLink(Messages.get("submission.own"), routes.SubmissionController.jumpToOwnSubmissions()),
                new InternalLink(Messages.get("submission.programming"), routes.ProgrammingSubmissionController.viewOwnSubmissions()),
                new InternalLink(programmingSubmission.getId() + "", routes.ProgrammingSubmissionController.viewSubmission(programmingSubmission.getId()))
        );
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Submissions - Programming - View");

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
        Page<ProgrammingSubmission> pageOfProgrammingSubmissions = programmingSubmissionService.getPageOfProgrammingSubmissions(pageIndex, PAGE_SIZE, orderBy, orderDir, null, null, null);

        List<String> problemJids = pageOfProgrammingSubmissions.getData().stream().map(s -> s.getProblemJid()).collect(Collectors.toList());
        Map<String, String> problemTitlesMap = SandalphonResourceDisplayNameUtils.buildTitlesMap(JidCacheServiceImpl.getInstance().getDisplayNames(problemJids), "en-US");
        Map<String, String> jidToNameMap = SubmissionControllerUtils.getJidToNameMap(sessionService, problemSetService, pageOfProgrammingSubmissions.getData().stream().map(s -> s.getContainerJid()).collect(Collectors.toList()));
        Map<String, String> gradingLanguageToNameMap = GradingLanguageRegistry.getInstance().getGradingLanguages();

        LazyHtml content = new LazyHtml(listSubmissionsView.render(pageOfProgrammingSubmissions, jidToNameMap, problemTitlesMap, gradingLanguageToNameMap, pageIndex, orderBy, orderDir, JerahmeelControllerUtils.getInstance().isAdmin()));
        SubmissionControllerUtils.appendAllSubtabLayout(content);
        if (!JerahmeelUtils.isGuest()) {
            SubmissionControllerUtils.appendTabLayout(content);
        }
        content.appendLayout(c -> heading3Layout.render(Messages.get("submission.submissions"), c));
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content,
                new InternalLink(Messages.get("submission.all"), routes.SubmissionController.jumpToAllSubmissions()),
                new InternalLink(Messages.get("submission.programming"), routes.ProgrammingSubmissionController.viewSubmissions())
        );
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Submissions - Programming");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional(readOnly = true)
    public Result viewSubmission(long programmingSubmissionId) throws ProgrammingSubmissionNotFoundException {
        ProgrammingSubmission programmingSubmission = programmingSubmissionService.findProgrammingSubmissionById(programmingSubmissionId);

        if (!(JerahmeelControllerUtils.getInstance().isAdmin() || programmingSubmission.getAuthorJid().equals(IdentityUtils.getUserJid()))) {
            return notFound();
        }

        LazyHtml content = getViewSubmissionContent(programmingSubmission);

        SubmissionControllerUtils.appendTabLayout(content);
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content,
                new InternalLink(Messages.get("submission.all"), routes.SubmissionController.jumpToAllSubmissions()),
                new InternalLink(Messages.get("submission.programming"), routes.ProgrammingSubmissionController.viewSubmissions()),
                new InternalLink(programmingSubmission.getId() + "", routes.ProgrammingSubmissionController.viewSubmission(programmingSubmission.getId()))
        );
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Submissions - Programming - View");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    private LazyHtml getViewSubmissionContent(ProgrammingSubmission programmingSubmission) {
        Session session = sessionService.findSessionByJid(programmingSubmission.getContainerJid());
        SubmissionSource submissionSource = ProgrammingSubmissionUtils.createSubmissionSourceFromPastSubmission(programmingSubmissionLocalFileSystemProvider, programmingSubmissionRemoteFileSystemProvider, programmingSubmission.getJid());
        String authorName = JidCacheServiceImpl.getInstance().getDisplayName(programmingSubmission.getAuthorJid());
        SessionProblem sessionProblem = sessionProblemService.findSessionProblemBySessionJidAndProblemJid(session.getJid(), programmingSubmission.getProblemJid());
        String sessionProblemAlias = sessionProblem.getAlias();
        String sessionProblemName = SandalphonResourceDisplayNameUtils.parseTitleByLanguage(JidCacheServiceImpl.getInstance().getDisplayName(sessionProblem.getProblemJid()), "en-US");
        String gradingLanguageName = GradingLanguageRegistry.getInstance().getLanguage(programmingSubmission.getGradingLanguage()).getName();

        return new LazyHtml(GradingEngineAdapterRegistry.getInstance().getByGradingEngineName(programmingSubmission.getGradingEngine()).renderViewSubmission(programmingSubmission, submissionSource, authorName, sessionProblemAlias, sessionProblemName, gradingLanguageName, session.getName()));
    }

    private void appendBreadcrumbsLayout(LazyHtml content, InternalLink... lastLinks) {
        ImmutableList.Builder<InternalLink> breadcrumbsBuilder = SubmissionControllerUtils.getBreadcrumbsBuilder();
        breadcrumbsBuilder.add(lastLinks);

        JerahmeelControllerUtils.getInstance().appendBreadcrumbsLayout(content, breadcrumbsBuilder.build());
    }
}
