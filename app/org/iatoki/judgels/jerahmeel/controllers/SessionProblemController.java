package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.headingWithActionLayout;
import org.iatoki.judgels.jerahmeel.forms.SessionProblemUpdateForm;
import org.iatoki.judgels.jerahmeel.services.impls.JidCacheServiceImpl;
import org.iatoki.judgels.jerahmeel.Session;
import org.iatoki.judgels.jerahmeel.SessionNotFoundException;
import org.iatoki.judgels.jerahmeel.SessionProblem;
import org.iatoki.judgels.jerahmeel.forms.SessionProblemCreateForm;
import org.iatoki.judgels.jerahmeel.SessionProblemNotFoundException;
import org.iatoki.judgels.jerahmeel.services.SessionProblemService;
import org.iatoki.judgels.jerahmeel.SessionProblemStatus;
import org.iatoki.judgels.jerahmeel.SessionProblemType;
import org.iatoki.judgels.jerahmeel.services.SessionService;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authorized;
import org.iatoki.judgels.jerahmeel.controllers.securities.HasRole;
import org.iatoki.judgels.jerahmeel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jerahmeel.views.html.session.problem.createProblemView;
import org.iatoki.judgels.jerahmeel.views.html.session.problem.updateSessionProblemView;
import org.iatoki.judgels.jerahmeel.views.html.session.problem.listSessionProblemsView;
import org.iatoki.judgels.jerahmeel.views.html.session.problem.viewProblemView;
import org.iatoki.judgels.sandalphon.ResourceDisplayNameUtils;
import org.iatoki.judgels.sandalphon.Sandalphon;
import org.iatoki.judgels.sandalphon.LanguageRestriction;
import play.data.DynamicForm;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Authenticated(value = {LoggedIn.class, HasRole.class})
@Authorized(value = "admin")
@Singleton
@Named
public final class SessionProblemController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;

    private final Sandalphon sandalphon;
    private final SessionProblemService sessionProblemService;
    private final SessionService sessionService;

    @Inject
    public SessionProblemController(Sandalphon sandalphon, SessionProblemService sessionProblemService, SessionService sessionService) {
        this.sandalphon = sandalphon;
        this.sessionProblemService = sessionProblemService;
        this.sessionService = sessionService;
    }

    @Transactional(readOnly = true)
    public Result viewSessionProblems(long sessionId) throws SessionNotFoundException {
        return listSessionProblems(sessionId, 0, "alias", "asc", "");
    }

    @Transactional(readOnly = true)
    public Result listSessionProblems(long sessionId, long page, String orderBy, String orderDir, String filterString) throws SessionNotFoundException {
        Session session = sessionService.findSessionById(sessionId);

        Page<SessionProblem> pageOfSessionProblems = sessionProblemService.getPageOfSessionProblems(session.getJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);
        List<String> problemJids = pageOfSessionProblems.getData().stream().map(cp -> cp.getProblemJid()).collect(Collectors.toList());
        Map<String, String> problemSlugsMap = ResourceDisplayNameUtils.buildSlugsMap(JidCacheServiceImpl.getInstance().getDisplayNames(problemJids));

        return showListSessionProblems(session, pageOfSessionProblems, orderBy, orderDir, filterString, problemSlugsMap);
    }

    @Transactional(readOnly = true)
    public Result viewProblem(long sessionId, long sessionProblemId) throws SessionNotFoundException, SessionProblemNotFoundException {
        Session session = sessionService.findSessionById(sessionId);
        SessionProblem sessionProblem = sessionProblemService.findSessionProblemById(sessionProblemId);

        if (!session.getJid().equals(sessionProblem.getSessionJid())) {
            return forbidden();
        }

        String submitUrl = "";
        if (SessionProblemType.BUNDLE.equals(sessionProblem.getType())) {
            submitUrl = routes.SessionBundleSubmissionController.postSubmitProblem(session.getId(), sessionProblem.getProblemJid()).absoluteURL(request(), request().secure());
        } else if (SessionProblemType.PROGRAMMING.equals(sessionProblem.getType())) {
            submitUrl = routes.SessionProgrammingSubmissionController.postSubmitProblem(session.getId(), sessionProblem.getProblemJid()).absoluteURL(request(), request().secure());
        }

        String requestUrl = sandalphon.getProblemStatementRenderUri().toString();
        String requestBody = sandalphon.getProblemStatementRenderRequestBody(sessionProblem.getProblemJid(), sessionProblem.getProblemSecret(), System.currentTimeMillis(), SessionControllerUtils.getCurrentStatementLanguage(), submitUrl, routes.SessionProblemController.switchLanguage().absoluteURL(request(), request().secure()), null, LanguageRestriction.defaultRestriction());

        LazyHtml content = new LazyHtml(viewProblemView.render(requestUrl, requestBody));
        SessionControllerUtils.appendUpdateLayout(content, session);
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, session,
                new InternalLink(sessionProblem.getAlias(), routes.SessionProblemController.viewProblem(session.getId(), sessionProblem.getId()))
        );

        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Sessions - Problem - View");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    @Transactional(readOnly = true)
    public Result renderImage(long sessionId, long sessionProblemId, String imageFilename) throws SessionNotFoundException, SessionProblemNotFoundException {
        Session session = sessionService.findSessionById(sessionId);
        SessionProblem sessionProblem = sessionProblemService.findSessionProblemById(sessionProblemId);

        if (!session.getJid().equals(sessionProblem.getSessionJid())) {
            return notFound();
        }

        URI imageUri = sandalphon.getProblemMediaRenderUri(sessionProblem.getProblemJid(), imageFilename);

        return redirect(imageUri.toString());
    }

    public Result switchLanguage() {
        String languageCode = DynamicForm.form().bindFromRequest().get("langCode");
        SessionControllerUtils.setCurrentStatementLanguage(languageCode);

        return redirect(request().getHeader("Referer"));
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result createProblem(long sessionId) throws SessionNotFoundException {
        Session session = sessionService.findSessionById(sessionId);
        Form<SessionProblemCreateForm> sessionProblemCreateForm = Form.form(SessionProblemCreateForm.class);

        return showCreateProblem(session, sessionProblemCreateForm);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postCreateProblem(long sessionId) throws SessionNotFoundException {
        Session session = sessionService.findSessionById(sessionId);
        Form<SessionProblemCreateForm> sessionProblemCreateForm = Form.form(SessionProblemCreateForm.class).bindFromRequest();

        if (formHasErrors(sessionProblemCreateForm)) {
            return showCreateProblem(session, sessionProblemCreateForm);
        }

        SessionProblemCreateForm sessionProblemCreateData = sessionProblemCreateForm.get();
        String problemName = null;
        try {
            problemName = sandalphon.verifyProblemJid(sessionProblemCreateData.problemJid);
        } catch (IOException e) {
            sessionProblemCreateForm.reject(Messages.get("error.system.sandalphon.connection"));

            return showCreateProblem(session, sessionProblemCreateForm);
        }
        if (problemName == null) {
            sessionProblemCreateForm.reject(Messages.get("error.problem.invalidJid"));

            return showCreateProblem(session, sessionProblemCreateForm);
        }

        if (sessionProblemService.aliasExistsInSession(session.getJid(), sessionProblemCreateData.alias)) {
            sessionProblemCreateForm.reject(Messages.get("error.session.problemExist"));

            return showCreateProblem(session, sessionProblemCreateForm);
        }

        sessionProblemService.addSessionProblem(session.getJid(), sessionProblemCreateData.problemJid, sessionProblemCreateData.problemSecret, sessionProblemCreateData.alias, SessionProblemType.valueOf(sessionProblemCreateData.type), SessionProblemStatus.valueOf(sessionProblemCreateData.status));
        JidCacheServiceImpl.getInstance().putDisplayName(sessionProblemCreateData.problemJid, problemName, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        return redirect(routes.SessionProblemController.viewSessionProblems(session.getId()));
    }

    @Transactional
    public Result deleteProblem(long sessionId, long sessionProblemId) throws SessionNotFoundException, SessionProblemNotFoundException {
        Session session = sessionService.findSessionById(sessionId);
        SessionProblem sessionProblem = sessionProblemService.findSessionProblemById(sessionProblemId);

        if (!session.getJid().equals(sessionProblem.getSessionJid())) {
            return forbidden();
        }

        sessionProblemService.removeSessionProblem(sessionProblemId);

        return redirect(routes.SessionProblemController.viewSessionProblems(session.getId()));
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result updateProblem(long sessionId, long sessionProblemId) throws SessionNotFoundException, SessionProblemNotFoundException {
        Session session = sessionService.findSessionById(sessionId);
        SessionProblem sessionProblem = sessionProblemService.findSessionProblemById(sessionProblemId);

        if (!session.getJid().equals(sessionProblem.getSessionJid())) {
            return notFound();
        }

        SessionProblemUpdateForm sessionProblemUpdateData = new SessionProblemUpdateForm();
        sessionProblemUpdateData.alias = sessionProblem.getAlias();
        sessionProblemUpdateData.status = sessionProblem.getStatus().name();

        Form<SessionProblemUpdateForm> sessionProblemUpdateForm = Form.form(SessionProblemUpdateForm.class).fill(sessionProblemUpdateData);

        return showUpdateProblem(session, sessionProblem, sessionProblemUpdateForm);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postUpdateProblem(long sessionId, long sessionProblemId) throws SessionNotFoundException, SessionProblemNotFoundException {
        Session session = sessionService.findSessionById(sessionId);
        SessionProblem sessionProblem = sessionProblemService.findSessionProblemById(sessionProblemId);

        if (!session.getJid().equals(sessionProblem.getSessionJid())) {
            return notFound();
        }

        Form<SessionProblemUpdateForm> sessionProblemUpdateForm = Form.form(SessionProblemUpdateForm.class).bindFromRequest();
        if (formHasErrors(sessionProblemUpdateForm)) {
            return showUpdateProblem(session, sessionProblem, sessionProblemUpdateForm);
        }

        SessionProblemUpdateForm sessionProblemUpdateData = sessionProblemUpdateForm.get();
        if (!sessionProblemUpdateData.alias.equals(sessionProblem.getAlias()) && !sessionProblemService.aliasExistsInSession(session.getJid(), sessionProblemUpdateData.alias)) {
            sessionProblemUpdateForm.reject(Messages.get("error.session.problem.duplicateAlias"));

            return showUpdateProblem(session, sessionProblem, sessionProblemUpdateForm);
        }

        sessionProblemService.updateSessionProblem(sessionProblem.getId(), sessionProblemUpdateData.alias, SessionProblemStatus.valueOf(sessionProblemUpdateData.status));

        return redirect(routes.SessionProblemController.viewSessionProblems(session.getId()));
    }

    private Result showListSessionProblems(Session session, Page<SessionProblem> pageOfSessionProblems, String orderBy, String orderDir, String filterString, Map<String, String> problemSlugsMap) {
        LazyHtml content = new LazyHtml(listSessionProblemsView.render(session.getId(), pageOfSessionProblems, orderBy, orderDir, filterString, problemSlugsMap));
        content.appendLayout(c -> headingWithActionLayout.render(Messages.get("session.problems"), new InternalLink(Messages.get("commons.add"), routes.SessionProblemController.createProblem(session.getId())), c));
        SessionControllerUtils.appendUpdateLayout(content, session);
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, session);
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Sessions - Problems");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    private Result showCreateProblem(Session session, Form<SessionProblemCreateForm> sessionProblemCreateForm) {
        LazyHtml content = new LazyHtml(createProblemView.render(session.getId(), sessionProblemCreateForm));
        SessionControllerUtils.appendUpdateLayout(content, session);
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, session,
                new InternalLink(Messages.get("commons.add"), routes.SessionProblemController.createProblem(session.getId()))
        );
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Sessions - Problems - Create");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    private Result showUpdateProblem(Session session, SessionProblem sessionProblem, Form<SessionProblemUpdateForm> sessionProblemUpdateForm) {
        LazyHtml content = new LazyHtml(updateSessionProblemView.render(sessionProblemUpdateForm, session.getId(), sessionProblem));
        SessionControllerUtils.appendUpdateLayout(content, session);
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, session,
                new InternalLink(Messages.get("commons.update"), routes.SessionProblemController.updateProblem(session.getId(), sessionProblem.getId()))
        );
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Sessions - Problems - Update");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    private void appendBreadcrumbsLayout(LazyHtml content, Session session, InternalLink... lastLinks) {
        ImmutableList.Builder<InternalLink> breadcrumbsBuilder = SessionControllerUtils.getBreadcrumbsBuilder();
        breadcrumbsBuilder.add(new InternalLink(Messages.get("session.problems"), routes.SessionController.jumpToProblems(session.getId())));
        breadcrumbsBuilder.add(new InternalLink(Messages.get("commons.view"), routes.SessionProblemController.viewSessionProblems(session.getId())));
        breadcrumbsBuilder.add(lastLinks);

        JerahmeelControllerUtils.getInstance().appendBreadcrumbsLayout(content, breadcrumbsBuilder.build());
    }
}
