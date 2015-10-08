package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.api.JudgelsAPIClientException;
import org.iatoki.judgels.api.sandalphon.SandalphonBundleProblemStatementRenderRequestParam;
import org.iatoki.judgels.api.sandalphon.SandalphonClientAPI;
import org.iatoki.judgels.api.sandalphon.SandalphonProblem;
import org.iatoki.judgels.api.sandalphon.SandalphonProgrammingProblemStatementRenderRequestParam;
import org.iatoki.judgels.api.sandalphon.SandalphonResourceDisplayNameUtils;
import org.iatoki.judgels.jerahmeel.Session;
import org.iatoki.judgels.jerahmeel.SessionNotFoundException;
import org.iatoki.judgels.jerahmeel.SessionProblem;
import org.iatoki.judgels.jerahmeel.SessionProblemNotFoundException;
import org.iatoki.judgels.jerahmeel.SessionProblemStatus;
import org.iatoki.judgels.jerahmeel.SessionProblemType;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authorized;
import org.iatoki.judgels.jerahmeel.controllers.securities.HasRole;
import org.iatoki.judgels.jerahmeel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jerahmeel.forms.SessionProblemAddForm;
import org.iatoki.judgels.jerahmeel.forms.SessionProblemEditForm;
import org.iatoki.judgels.jerahmeel.services.SessionProblemService;
import org.iatoki.judgels.jerahmeel.services.SessionService;
import org.iatoki.judgels.jerahmeel.services.impls.JidCacheServiceImpl;
import org.iatoki.judgels.jerahmeel.views.html.session.problem.addSessionProblemView;
import org.iatoki.judgels.jerahmeel.views.html.session.problem.editSessionProblemView;
import org.iatoki.judgels.jerahmeel.views.html.session.problem.listSessionProblemsView;
import org.iatoki.judgels.jerahmeel.views.html.session.problem.viewProblemView;
import org.iatoki.judgels.jophiel.BasicActivityKeys;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.headingWithActionLayout;
import play.data.DynamicForm;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Authenticated(value = {LoggedIn.class, HasRole.class})
@Authorized(value = "admin")
@Singleton
@Named
public final class SessionProblemController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;
    private static final String PROBLEM = "problem";
    private static final String SESSION = "session";

    private final SandalphonClientAPI sandalphonClientAPI;
    private final SessionProblemService sessionProblemService;
    private final SessionService sessionService;

    @Inject
    public SessionProblemController(SandalphonClientAPI sandalphonClientAPI, SessionProblemService sessionProblemService, SessionService sessionService) {
        this.sandalphonClientAPI = sandalphonClientAPI;
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
        Map<String, String> problemSlugsMap = SandalphonResourceDisplayNameUtils.buildSlugsMap(JidCacheServiceImpl.getInstance().getDisplayNames(problemJids));

        return showListSessionProblems(session, pageOfSessionProblems, orderBy, orderDir, filterString, problemSlugsMap);
    }

    @Transactional(readOnly = true)
    public Result viewSessionProblem(long sessionId, long sessionProblemId) throws SessionNotFoundException, SessionProblemNotFoundException {
        Session session = sessionService.findSessionById(sessionId);
        SessionProblem sessionProblem = sessionProblemService.findSessionProblemById(sessionProblemId);

        if (!session.getJid().equals(sessionProblem.getSessionJid())) {
            return forbidden();
        }

        String requestUrl;
        String requestBody;

        if (SessionProblemType.BUNDLE.equals(sessionProblem.getType())) {
            SandalphonBundleProblemStatementRenderRequestParam param = new SandalphonBundleProblemStatementRenderRequestParam();

            param.setProblemSecret(sessionProblem.getProblemSecret());
            param.setCurrentMillis(System.currentTimeMillis());
            param.setStatementLanguage(SessionControllerUtils.getCurrentStatementLanguage());
            param.setSwitchStatementLanguageUrl(routes.TrainingProblemController.switchLanguage().absoluteURL(request(), request().secure()));
            param.setPostSubmitUrl(routes.SessionBundleSubmissionController.postSubmitProblem(session.getId(), sessionProblem.getProblemJid()).absoluteURL(request(), request().secure()));

            requestUrl = sandalphonClientAPI.getBundleProblemStatementRenderAPIEndpoint(sessionProblem.getProblemJid());
            requestBody = sandalphonClientAPI.constructBundleProblemStatementRenderAPIRequestBody(sessionProblem.getProblemJid(), param);
        } else if (SessionProblemType.PROGRAMMING.equals(sessionProblem.getType())) {
            SandalphonProgrammingProblemStatementRenderRequestParam param = new SandalphonProgrammingProblemStatementRenderRequestParam();

            param.setProblemSecret(sessionProblem.getProblemSecret());
            param.setCurrentMillis(System.currentTimeMillis());
            param.setStatementLanguage(SessionControllerUtils.getCurrentStatementLanguage());
            param.setSwitchStatementLanguageUrl(routes.TrainingProblemController.switchLanguage().absoluteURL(request(), request().secure()));
            param.setPostSubmitUrl(routes.SessionProgrammingSubmissionController.postSubmitProblem(session.getId(), sessionProblem.getProblemJid()).absoluteURL(request(), request().secure()));
            param.setReasonNotAllowedToSubmit("");
            param.setAllowedGradingLanguages("");

            requestUrl = sandalphonClientAPI.getProgrammingProblemStatementRenderAPIEndpoint(sessionProblem.getProblemJid());
            requestBody = sandalphonClientAPI.constructProgrammingProblemStatementRenderAPIRequestBody(sessionProblem.getProblemJid(), param);
        } else {
            throw new IllegalStateException();
        }

        LazyHtml content = new LazyHtml(viewProblemView.render(requestUrl, requestBody));
        SessionControllerUtils.appendUpdateLayout(content, session);
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, session,
                new InternalLink(sessionProblem.getAlias(), routes.SessionProblemController.viewSessionProblem(session.getId(), sessionProblem.getId()))
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

        String imageUrl = sandalphonClientAPI.getProblemStatementMediaRenderAPIEndpoint(sessionProblem.getProblemJid(), imageFilename);

        return redirect(imageUrl);
    }

    public Result switchLanguage() {
        String languageCode = DynamicForm.form().bindFromRequest().get("langCode");
        SessionControllerUtils.setCurrentStatementLanguage(languageCode);

        return redirect(request().getHeader("Referer"));
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result addSessionProblem(long sessionId) throws SessionNotFoundException {
        Session session = sessionService.findSessionById(sessionId);
        Form<SessionProblemAddForm> sessionProblemAddForm = Form.form(SessionProblemAddForm.class);

        return showAddSessionProblem(session, sessionProblemAddForm);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postAddSessionProblem(long sessionId) throws SessionNotFoundException {
        Session session = sessionService.findSessionById(sessionId);
        Form<SessionProblemAddForm> sessionProblemAddForm = Form.form(SessionProblemAddForm.class).bindFromRequest();

        if (formHasErrors(sessionProblemAddForm)) {
            return showAddSessionProblem(session, sessionProblemAddForm);
        }

        SessionProblemAddForm sessionProblemCreateData = sessionProblemAddForm.get();

        if (sessionProblemService.aliasExistsInSession(session.getJid(), sessionProblemCreateData.alias)) {
            sessionProblemAddForm.reject(Messages.get("error.session.problem.duplicateAlias"));

            return showAddSessionProblem(session, sessionProblemAddForm);
        }

        SandalphonProblem sandalphonProblem;
        try {
            sandalphonProblem = sandalphonClientAPI.findClientProblem(sessionProblemCreateData.problemJid, sessionProblemCreateData.problemSecret);
        } catch (JudgelsAPIClientException e) {
            if (e.getStatusCode() >= Http.Status.INTERNAL_SERVER_ERROR) {
                sessionProblemAddForm.reject(Messages.get("error.system.sandalphon.connection"));
            } else {
                sessionProblemAddForm.reject(Messages.get("error.problem.invalid"));
            }
            return showAddSessionProblem(session, sessionProblemAddForm);
        }

        sessionProblemService.addSessionProblem(session.getJid(), sessionProblemCreateData.problemJid, sessionProblemCreateData.problemSecret, sessionProblemCreateData.alias, SessionProblemType.valueOf(sessionProblemCreateData.type), SessionProblemStatus.valueOf(sessionProblemCreateData.status), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        JidCacheServiceImpl.getInstance().putDisplayName(sessionProblemCreateData.problemJid, sandalphonProblem.getDisplayName(), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        JerahmeelControllerUtils.getInstance().addActivityLog(BasicActivityKeys.ADD_IN.construct(SESSION, session.getJid(), session.getName(), PROBLEM, sandalphonProblem.getJid(), sandalphonProblem.getSlug()));

        return redirect(routes.SessionProblemController.viewSessionProblems(session.getId()));
    }

    @Transactional
    public Result removeSessionProblem(long sessionId, long sessionProblemId) throws SessionNotFoundException, SessionProblemNotFoundException {
        Session session = sessionService.findSessionById(sessionId);
        SessionProblem sessionProblem = sessionProblemService.findSessionProblemById(sessionProblemId);

        if (!session.getJid().equals(sessionProblem.getSessionJid())) {
            return forbidden();
        }

        sessionProblemService.removeSessionProblem(sessionProblemId);

        JerahmeelControllerUtils.getInstance().addActivityLog(BasicActivityKeys.REMOVE_FROM.construct(SESSION, session.getJid(), session.getName(), PROBLEM, sessionProblem.getProblemJid(), SandalphonResourceDisplayNameUtils.parseSlugByLanguage(JidCacheServiceImpl.getInstance().getDisplayName(sessionProblem.getProblemJid()))));

        return redirect(routes.SessionProblemController.viewSessionProblems(session.getId()));
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result editSessionProblem(long sessionId, long sessionProblemId) throws SessionNotFoundException, SessionProblemNotFoundException {
        Session session = sessionService.findSessionById(sessionId);
        SessionProblem sessionProblem = sessionProblemService.findSessionProblemById(sessionProblemId);

        if (!session.getJid().equals(sessionProblem.getSessionJid())) {
            return notFound();
        }

        SessionProblemEditForm sessionProblemEditData = new SessionProblemEditForm();
        sessionProblemEditData.alias = sessionProblem.getAlias();
        sessionProblemEditData.status = sessionProblem.getStatus().name();

        Form<SessionProblemEditForm> sessionProblemEditForm = Form.form(SessionProblemEditForm.class).fill(sessionProblemEditData);

        return showEditSessionProblem(session, sessionProblem, sessionProblemEditForm);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postEditSessionProblem(long sessionId, long sessionProblemId) throws SessionNotFoundException, SessionProblemNotFoundException {
        Session session = sessionService.findSessionById(sessionId);
        SessionProblem sessionProblem = sessionProblemService.findSessionProblemById(sessionProblemId);

        if (!session.getJid().equals(sessionProblem.getSessionJid())) {
            return notFound();
        }

        Form<SessionProblemEditForm> sessionProblemEditForm = Form.form(SessionProblemEditForm.class).bindFromRequest();
        if (formHasErrors(sessionProblemEditForm)) {
            return showEditSessionProblem(session, sessionProblem, sessionProblemEditForm);
        }

        SessionProblemEditForm sessionProblemEditData = sessionProblemEditForm.get();
        if (!sessionProblemEditData.alias.equals(sessionProblem.getAlias()) && sessionProblemService.aliasExistsInSession(session.getJid(), sessionProblemEditData.alias)) {
            sessionProblemEditForm.reject(Messages.get("error.session.problem.duplicateAlias"));

            return showEditSessionProblem(session, sessionProblem, sessionProblemEditForm);
        }

        sessionProblemService.updateSessionProblem(sessionProblem.getId(), sessionProblemEditData.alias, SessionProblemStatus.valueOf(sessionProblemEditData.status), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        JerahmeelControllerUtils.getInstance().addActivityLog(BasicActivityKeys.EDIT_IN.construct(SESSION, session.getJid(), session.getName(), PROBLEM, sessionProblem.getProblemJid(), SandalphonResourceDisplayNameUtils.parseSlugByLanguage(JidCacheServiceImpl.getInstance().getDisplayName(sessionProblem.getProblemJid()))));

        return redirect(routes.SessionProblemController.viewSessionProblems(session.getId()));
    }

    private Result showListSessionProblems(Session session, Page<SessionProblem> pageOfSessionProblems, String orderBy, String orderDir, String filterString, Map<String, String> problemSlugsMap) {
        LazyHtml content = new LazyHtml(listSessionProblemsView.render(session.getId(), pageOfSessionProblems, orderBy, orderDir, filterString, problemSlugsMap));
        content.appendLayout(c -> headingWithActionLayout.render(Messages.get("session.problems"), new InternalLink(Messages.get("commons.add"), routes.SessionProblemController.addSessionProblem(session.getId())), c));
        SessionControllerUtils.appendUpdateLayout(content, session);
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, session);
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Sessions - Problems");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    private Result showAddSessionProblem(Session session, Form<SessionProblemAddForm> sessionProblemAddForm) {
        LazyHtml content = new LazyHtml(addSessionProblemView.render(session.getId(), sessionProblemAddForm));
        SessionControllerUtils.appendUpdateLayout(content, session);
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, session,
                new InternalLink(Messages.get("commons.add"), routes.SessionProblemController.addSessionProblem(session.getId()))
        );
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Sessions - Problems - Create");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    private Result showEditSessionProblem(Session session, SessionProblem sessionProblem, Form<SessionProblemEditForm> sessionProblemEditForm) {
        LazyHtml content = new LazyHtml(editSessionProblemView.render(sessionProblemEditForm, session.getId(), sessionProblem));
        SessionControllerUtils.appendUpdateLayout(content, session);
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, session,
                new InternalLink(Messages.get("commons.update"), routes.SessionProblemController.editSessionProblem(session.getId(), sessionProblem.getId()))
        );
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Sessions - Problems - Edit");

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
