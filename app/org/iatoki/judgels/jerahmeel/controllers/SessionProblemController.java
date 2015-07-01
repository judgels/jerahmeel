package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.InternalLink;
import org.iatoki.judgels.commons.LazyHtml;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.commons.controllers.BaseController;
import org.iatoki.judgels.commons.views.html.layouts.headingWithActionLayout;
import org.iatoki.judgels.jerahmeel.controllers.forms.SessionProblemUpdateForm;
import org.iatoki.judgels.jerahmeel.services.JidCacheService;
import org.iatoki.judgels.jerahmeel.Session;
import org.iatoki.judgels.jerahmeel.SessionNotFoundException;
import org.iatoki.judgels.jerahmeel.SessionProblem;
import org.iatoki.judgels.jerahmeel.controllers.forms.SessionProblemCreateForm;
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
import org.iatoki.judgels.sandalphon.Sandalphon;
import org.iatoki.judgels.sandalphon.programming.LanguageRestriction;
import play.data.DynamicForm;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Result;

import java.io.IOException;
import java.net.URI;

@Authenticated(value = {LoggedIn.class, HasRole.class})
@Authorized(value = {"admin"})
public final class SessionProblemController extends BaseController {

    private static final long PAGE_SIZE = 20;

    private final SessionService sessionService;
    private final SessionProblemService sessionProblemService;
    private final Sandalphon sandalphon;

    public SessionProblemController(SessionService sessionService, SessionProblemService sessionProblemService, Sandalphon sandalphon) {
        this.sessionService = sessionService;
        this.sessionProblemService = sessionProblemService;
        this.sandalphon = sandalphon;
    }

    @Transactional(readOnly = true)
    public Result viewSessionProblems(long sessionId) throws SessionNotFoundException {
        return listSessionProblems(sessionId, 0, "alias", "asc", "");
    }

    @Transactional(readOnly = true)
    public Result listSessionProblems(long sessionId, long page, String orderBy, String orderDir, String filterString) throws SessionNotFoundException {
        Session session = sessionService.findSessionBySessionId(sessionId);

        Page<SessionProblem> sessionPage = sessionProblemService.findSessionProblems(session.getJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);

        return showListSessionProblems(session, sessionPage, orderBy, orderDir, filterString);
    }

    @Transactional(readOnly = true)
    public Result viewProblem(long sessionId, long sessionProblemId) throws SessionNotFoundException, SessionProblemNotFoundException {
        Session session = sessionService.findSessionBySessionId(sessionId);
        SessionProblem sessionProblem = sessionProblemService.findSessionProblemBySessionProblemId(sessionProblemId);

        if (session.getJid().equals(sessionProblem.getSessionJid())) {
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
            ControllerUtils.getInstance().appendSidebarLayout(content);
            ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                  new InternalLink(Messages.get("session.sessions"), routes.SessionController.viewSessions()),
                  new InternalLink(Messages.get("session.problems"), routes.SessionController.jumpToProblems(session.getId())),
                  new InternalLink(Messages.get("commons.view"), routes.SessionProblemController.viewSessionProblems(session.getId())),
                  new InternalLink(sessionProblem.getAlias(), routes.SessionProblemController.viewProblem(session.getId(), sessionProblem.getId()))
            ));

            ControllerUtils.getInstance().appendTemplateLayout(content, "Sessions - Problem - View");

            return ControllerUtils.getInstance().lazyOk(content);
        } else {
            return forbidden();
        }
    }

    @Transactional(readOnly = true)
    public Result renderImage(long sessionId, long sessionProblemId, String imageFilename) throws SessionNotFoundException, SessionProblemNotFoundException {
        Session session = sessionService.findSessionBySessionId(sessionId);
        SessionProblem sessionProblem = sessionProblemService.findSessionProblemBySessionProblemId(sessionProblemId);

        if (session.getJid().equals(sessionProblem.getSessionJid())) {
            URI imageUri = sandalphon.getProblemMediaRenderUri(sessionProblem.getProblemJid(), imageFilename);

            return redirect(imageUri.toString());
        } else {
            return notFound();
        }
    }

    public Result switchLanguage() {
        String languageCode = DynamicForm.form().bindFromRequest().get("langCode");
        SessionControllerUtils.setCurrentStatementLanguage(languageCode);

        return redirect(request().getHeader("Referer"));
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result createProblem(long sessionId) throws SessionNotFoundException {
        Session session = sessionService.findSessionBySessionId(sessionId);
        Form<SessionProblemCreateForm> form = Form.form(SessionProblemCreateForm.class);

        return showCreateProblem(session, form);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postCreateProblem(long sessionId) throws SessionNotFoundException {
        Session session = sessionService.findSessionBySessionId(sessionId);
        Form<SessionProblemCreateForm> form = Form.form(SessionProblemCreateForm.class).bindFromRequest();

        if (form.hasErrors() || form.hasGlobalErrors()) {
            return showCreateProblem(session, form);
        } else {
            SessionProblemCreateForm data = form.get();
            String problemName = null;
            try {
                problemName = sandalphon.verifyProblemJid(data.problemJid);
            } catch (IOException e) {
                form.reject(Messages.get("error.system.sandalphon.connection"));

                return showCreateProblem(session, form);
            }
            if (problemName != null) {
                if (!sessionProblemService.isInSessionByAlias(session.getJid(), data.alias)) {
                    sessionProblemService.addSessionProblem(session.getJid(), data.problemJid, data.problemSecret, data.alias, SessionProblemType.valueOf(data.type), SessionProblemStatus.valueOf(data.status));
                    JidCacheService.getInstance().putDisplayName(data.problemJid, problemName, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

                    return redirect(routes.SessionProblemController.viewSessionProblems(session.getId()));
                } else {
                    form.reject(Messages.get("error.session.problemExist"));

                    return showCreateProblem(session, form);
                }
            } else {
                form.reject(Messages.get("error.problem.invalidJid"));

                return showCreateProblem(session, form);
            }
        }
    }

    @Transactional
    public Result deleteProblem(long sessionId, long sessionProblemId) throws SessionNotFoundException, SessionProblemNotFoundException {
        Session session = sessionService.findSessionBySessionId(sessionId);
        SessionProblem sessionProblem = sessionProblemService.findSessionProblemBySessionProblemId(sessionProblemId);

        if (session.getJid().equals(sessionProblem.getSessionJid())) {
            sessionProblemService.removeSessionProblem(sessionProblemId);

            return redirect(routes.SessionProblemController.viewSessionProblems(session.getId()));
        } else {
            return forbidden();
        }
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result updateProblem(long sessionId, long sessionProblemId) throws SessionNotFoundException, SessionProblemNotFoundException {
        Session session = sessionService.findSessionBySessionId(sessionId);
        SessionProblem sessionProblem = sessionProblemService.findSessionProblemBySessionProblemId(sessionProblemId);

        if (session.getJid().equals(sessionProblem.getSessionJid())) {
            SessionProblemUpdateForm sessionProblemUpdateForm = new SessionProblemUpdateForm();
            sessionProblemUpdateForm.alias = sessionProblem.getAlias();
            sessionProblemUpdateForm.status = sessionProblem.getStatus().name();

            Form<SessionProblemUpdateForm> form = Form.form(SessionProblemUpdateForm.class).fill(sessionProblemUpdateForm);

            return showUpdateProblem(session, sessionProblem, form);
        } else {
            return notFound();
        }
    }

    @Transactional
    @RequireCSRFCheck
    public Result postUpdateProblem(long sessionId, long sessionProblemId) throws SessionNotFoundException, SessionProblemNotFoundException {
        Session session = sessionService.findSessionBySessionId(sessionId);
        SessionProblem sessionProblem = sessionProblemService.findSessionProblemBySessionProblemId(sessionProblemId);

        if (session.getJid().equals(sessionProblem.getSessionJid())) {
            Form<SessionProblemUpdateForm> form = Form.form(SessionProblemUpdateForm.class).bindFromRequest();
            if (!((form.hasErrors()) || (form.hasGlobalErrors()))) {
                SessionProblemUpdateForm sessionProblemUpdateForm = form.get();
                if ((sessionProblemUpdateForm.alias.equals(sessionProblem.getAlias())) || (!sessionProblemService.isInSessionByAlias(session.getJid(), sessionProblemUpdateForm.alias))) {
                    sessionProblemService.updateSessionProblem(sessionProblem.getId(), sessionProblemUpdateForm.alias, SessionProblemStatus.valueOf(sessionProblemUpdateForm.status));

                    return redirect(routes.SessionProblemController.viewSessionProblems(session.getId()));
                } else {
                    form.reject(Messages.get("error.session.problem.duplicateAlias"));

                    return showUpdateProblem(session, sessionProblem, form);
                }
            } else {
                return showUpdateProblem(session, sessionProblem, form);
            }
        } else {
            return notFound();
        }
    }

    private Result showListSessionProblems(Session session, Page<SessionProblem> currentPage, String orderBy, String orderDir, String filterString) {
        LazyHtml content = new LazyHtml(listSessionProblemsView.render(session.getId(), currentPage, orderBy, orderDir, filterString));
        content.appendLayout(c -> headingWithActionLayout.render(Messages.get("session.problems"), new InternalLink(Messages.get("commons.add"), routes.SessionProblemController.createProblem(session.getId())), c));
        SessionControllerUtils.appendUpdateLayout(content, session);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("session.sessions"), routes.SessionController.viewSessions()),
              new InternalLink(Messages.get("session.problems"), routes.SessionController.jumpToProblems(session.getId())),
              new InternalLink(Messages.get("commons.view"), routes.SessionProblemController.viewSessionProblems(session.getId()))
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Sessions - Problems");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    private Result showCreateProblem(Session session, Form<SessionProblemCreateForm> form) {
        LazyHtml content = new LazyHtml(createProblemView.render(session.getId(), form));
        SessionControllerUtils.appendUpdateLayout(content, session);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
              new InternalLink(Messages.get("session.sessions"), routes.SessionController.viewSessions()),
              new InternalLink(Messages.get("session.problems"), routes.SessionController.jumpToProblems(session.getId())),
              new InternalLink(Messages.get("commons.view"), routes.SessionProblemController.viewSessionProblems(session.getId())),
              new InternalLink(Messages.get("commons.add"), routes.SessionProblemController.createProblem(session.getId()))
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Sessions - Problems - Create");

        return ControllerUtils.getInstance().lazyOk(content);
    }

    private Result showUpdateProblem(Session session, SessionProblem sessionProblem, Form<SessionProblemUpdateForm> form) {
        LazyHtml content = new LazyHtml(updateSessionProblemView.render(form, session.getId(), sessionProblem));
        SessionControllerUtils.appendUpdateLayout(content, session);
        ControllerUtils.getInstance().appendSidebarLayout(content);
        ControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("session.sessions"), routes.SessionController.viewSessions()),
                new InternalLink(Messages.get("session.problems"), routes.SessionController.jumpToProblems(session.getId())),
                new InternalLink(Messages.get("commons.view"), routes.SessionProblemController.viewSessionProblems(session.getId())),
                new InternalLink(Messages.get("commons.update"), routes.SessionProblemController.updateProblem(session.getId(), sessionProblem.getId()))
        ));
        ControllerUtils.getInstance().appendTemplateLayout(content, "Sessions - Problems - Update");

        return ControllerUtils.getInstance().lazyOk(content);
    }
}
