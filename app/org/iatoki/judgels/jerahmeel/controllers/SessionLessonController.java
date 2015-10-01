package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.api.JudgelsAPIClientException;
import org.iatoki.judgels.api.sandalphon.SandalphonClientAPI;
import org.iatoki.judgels.api.sandalphon.SandalphonLesson;
import org.iatoki.judgels.api.sandalphon.SandalphonLessonStatementRenderRequestParam;
import org.iatoki.judgels.api.sandalphon.SandalphonResourceDisplayNameUtils;
import org.iatoki.judgels.jerahmeel.Session;
import org.iatoki.judgels.jerahmeel.SessionLesson;
import org.iatoki.judgels.jerahmeel.SessionLessonNotFoundException;
import org.iatoki.judgels.jerahmeel.SessionLessonStatus;
import org.iatoki.judgels.jerahmeel.SessionNotFoundException;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authorized;
import org.iatoki.judgels.jerahmeel.controllers.securities.HasRole;
import org.iatoki.judgels.jerahmeel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jerahmeel.forms.SessionLessonAddForm;
import org.iatoki.judgels.jerahmeel.forms.SessionLessonEditForm;
import org.iatoki.judgels.jerahmeel.services.SessionLessonService;
import org.iatoki.judgels.jerahmeel.services.SessionService;
import org.iatoki.judgels.jerahmeel.services.impls.JidCacheServiceImpl;
import org.iatoki.judgels.jerahmeel.views.html.session.lesson.addLessonView;
import org.iatoki.judgels.jerahmeel.views.html.session.lesson.editSessionLessonView;
import org.iatoki.judgels.jerahmeel.views.html.session.lesson.listSessionLessonsView;
import org.iatoki.judgels.jerahmeel.views.html.session.lesson.viewLessonView;
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
public final class SessionLessonController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;
    private static final String LESSON = "lesson";
    private static final String SESSION = "session";

    private final SandalphonClientAPI sandalphonClientAPI;
    private final SessionLessonService sessionLessonService;
    private final SessionService sessionService;

    @Inject
    public SessionLessonController(SandalphonClientAPI sandalphonClientAPI, SessionLessonService sessionLessonService, SessionService sessionService) {
        this.sandalphonClientAPI = sandalphonClientAPI;
        this.sessionLessonService = sessionLessonService;
        this.sessionService = sessionService;
    }

    @Transactional(readOnly = true)
    public Result viewSessionLessons(long sessionId) throws SessionNotFoundException {
        return listSessionLessons(sessionId, 0, "alias", "asc", "");
    }

    @Transactional(readOnly = true)
    public Result listSessionLessons(long sessionId, long page, String orderBy, String orderDir, String filterString) throws SessionNotFoundException {
        Session session = sessionService.findSessionById(sessionId);

        Page<SessionLesson> pageOfSessionLessons = sessionLessonService.getPageOfSessionLessons(session.getJid(), page, PAGE_SIZE, orderBy, orderDir, filterString);
        List<String> lessonJids = pageOfSessionLessons.getData().stream().map(cp -> cp.getLessonJid()).collect(Collectors.toList());
        Map<String, String> lessonSlugsMap = SandalphonResourceDisplayNameUtils.buildSlugsMap(JidCacheServiceImpl.getInstance().getDisplayNames(lessonJids));

        return showListSessionLessons(session, pageOfSessionLessons, orderBy, orderDir, filterString, lessonSlugsMap);
    }

    @Transactional(readOnly = true)
    public Result viewLesson(long sessionId, long sessionLessonId) throws SessionNotFoundException, SessionLessonNotFoundException {
        Session session = sessionService.findSessionById(sessionId);
        SessionLesson sessionLesson = sessionLessonService.findSessionLessonById(sessionLessonId);

        if (!session.getJid().equals(sessionLesson.getSessionJid())) {
            return forbidden();
        }

        SandalphonLessonStatementRenderRequestParam param = new SandalphonLessonStatementRenderRequestParam();

        param.setLessonSecret(sessionLesson.getLessonSecret());
        param.setCurrentMillis(System.currentTimeMillis());
        param.setStatementLanguage(SessionControllerUtils.getCurrentStatementLanguage());
        param.setSwitchStatementLanguageUrl(routes.SessionLessonController.switchLanguage().absoluteURL(request(), request().secure()));

        String requestUrl = sandalphonClientAPI.getLessonStatementRenderAPIEndpoint(sessionLesson.getLessonJid());
        String requestBody = sandalphonClientAPI.constructLessonStatementRenderAPIRequestBody(sessionLesson.getLessonJid(), param);

        LazyHtml content = new LazyHtml(viewLessonView.render(requestUrl, requestBody));
        SessionControllerUtils.appendUpdateLayout(content, session);
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, session,
                new InternalLink(sessionLesson.getAlias(), routes.SessionLessonController.viewLesson(session.getId(), sessionLesson.getId()))
        );

        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Sessions - Lesson - View");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    @Transactional(readOnly = true)
    public Result renderImage(long sessionId, long sessionLessonId, String imageFilename) throws SessionNotFoundException, SessionLessonNotFoundException {
        Session session = sessionService.findSessionById(sessionId);
        SessionLesson sessionLesson = sessionLessonService.findSessionLessonById(sessionLessonId);

        if (!session.getJid().equals(sessionLesson.getSessionJid())) {
            return notFound();
        }

        String imageUrl = sandalphonClientAPI.getLessonStatementMediaRenderAPIEndpoint(sessionLesson.getLessonJid(), imageFilename);

        return redirect(imageUrl);
    }

    public Result switchLanguage() {
        String languageCode = DynamicForm.form().bindFromRequest().get("langCode");
        SessionControllerUtils.setCurrentStatementLanguage(languageCode);

        return redirect(request().getHeader("Referer"));
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result addLesson(long sessionId) throws SessionNotFoundException {
        Session session = sessionService.findSessionById(sessionId);
        Form<SessionLessonAddForm> sessionLessonAddForm = Form.form(SessionLessonAddForm.class);

        return showAddLesson(session, sessionLessonAddForm);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postAddLesson(long sessionId) throws SessionNotFoundException {
        Session session = sessionService.findSessionById(sessionId);
        Form<SessionLessonAddForm> sessionLessonCreateForm = Form.form(SessionLessonAddForm.class).bindFromRequest();

        if (formHasErrors(sessionLessonCreateForm)) {
            return showAddLesson(session, sessionLessonCreateForm);
        }

        SessionLessonAddForm sessionLessonCreateData = sessionLessonCreateForm.get();

        if (sessionLessonService.aliasExistsInSession(session.getJid(), sessionLessonCreateData.alias)) {
            sessionLessonCreateForm.reject(Messages.get("error.session.lesson.aliasExist"));

            return showAddLesson(session, sessionLessonCreateForm);
        }

        SandalphonLesson sandalphonLesson;
        try {
            sandalphonLesson = sandalphonClientAPI.findClientLesson(sessionLessonCreateData.lessonJid, sessionLessonCreateData.lessonSecret);
        } catch (JudgelsAPIClientException e) {
            if (e.getStatusCode() >= Http.Status.INTERNAL_SERVER_ERROR) {
                sessionLessonCreateForm.reject(Messages.get("error.system.sandalphon.connection"));
            } else {
                sessionLessonCreateForm.reject(Messages.get("error.lesson.invalid"));
            }
            return showAddLesson(session, sessionLessonCreateForm);
        }

        sessionLessonService.addSessionLesson(session.getJid(), sessionLessonCreateData.lessonJid, sessionLessonCreateData.lessonSecret, sessionLessonCreateData.alias, SessionLessonStatus.valueOf(sessionLessonCreateData.status), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        JidCacheServiceImpl.getInstance().putDisplayName(sessionLessonCreateData.lessonJid, sandalphonLesson.getDisplayName(), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        JerahmeelControllerUtils.getInstance().addActivityLog(BasicActivityKeys.ADD_IN.construct(SESSION, session.getJid(), session.getName(), LESSON, sandalphonLesson.getJid(), sandalphonLesson.getSlug()));

        return redirect(routes.SessionLessonController.viewSessionLessons(session.getId()));
    }

    @Transactional
    public Result removeLesson(long sessionId, long sessionLessonId) throws SessionNotFoundException, SessionLessonNotFoundException {
        Session session = sessionService.findSessionById(sessionId);
        SessionLesson sessionLesson = sessionLessonService.findSessionLessonById(sessionLessonId);

        if (!session.getJid().equals(sessionLesson.getSessionJid())) {
            return forbidden();
        }

        sessionLessonService.removeSessionLesson(sessionLessonId);

        JerahmeelControllerUtils.getInstance().addActivityLog(BasicActivityKeys.REMOVE_FROM.construct(SESSION, session.getJid(), session.getName(), LESSON, sessionLesson.getLessonJid(), SandalphonResourceDisplayNameUtils.parseSlugByLanguage(JidCacheServiceImpl.getInstance().getDisplayName(sessionLesson.getLessonJid()))));

        return redirect(routes.SessionLessonController.viewSessionLessons(session.getId()));
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result editLesson(long sessionId, long sessionLessonId) throws SessionNotFoundException, SessionLessonNotFoundException {
        Session session = sessionService.findSessionById(sessionId);
        SessionLesson sessionLesson = sessionLessonService.findSessionLessonById(sessionLessonId);

        if (!session.getJid().equals(sessionLesson.getSessionJid())) {
            return notFound();
        }

        SessionLessonEditForm sessionLessonEditData = new SessionLessonEditForm();
        sessionLessonEditData.alias = sessionLesson.getAlias();
        sessionLessonEditData.status = sessionLesson.getStatus().name();

        Form<SessionLessonEditForm> sessionLessonEditForm = Form.form(SessionLessonEditForm.class).fill(sessionLessonEditData);

        return showEditLesson(session, sessionLesson, sessionLessonEditForm);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postEditLesson(long sessionId, long sessionLessonId) throws SessionNotFoundException, SessionLessonNotFoundException {
        Session session = sessionService.findSessionById(sessionId);
        SessionLesson sessionLesson = sessionLessonService.findSessionLessonById(sessionLessonId);

        if (!session.getJid().equals(sessionLesson.getSessionJid())) {
            return notFound();
        }

        Form<SessionLessonEditForm> sessionLessonEditForm = Form.form(SessionLessonEditForm.class).bindFromRequest();
        if (formHasErrors(sessionLessonEditForm)) {
            return showEditLesson(session, sessionLesson, sessionLessonEditForm);
        }

        SessionLessonEditForm sessionLessonEditData = sessionLessonEditForm.get();
        if (!sessionLessonEditData.alias.equals(sessionLesson.getAlias()) && sessionLessonService.aliasExistsInSession(session.getJid(), sessionLessonEditData.alias)) {
            sessionLessonEditForm.reject(Messages.get("error.session.lesson.duplicateAlias"));

            return showEditLesson(session, sessionLesson, sessionLessonEditForm);
        }

        sessionLessonService.updateSessionLesson(sessionLesson.getId(), sessionLessonEditData.alias, SessionLessonStatus.valueOf(sessionLessonEditData.status), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        JerahmeelControllerUtils.getInstance().addActivityLog(BasicActivityKeys.EDIT_IN.construct(SESSION, session.getJid(), session.getName(), LESSON, sessionLesson.getLessonJid(), SandalphonResourceDisplayNameUtils.parseSlugByLanguage(JidCacheServiceImpl.getInstance().getDisplayName(sessionLesson.getLessonJid()))));

        return redirect(routes.SessionLessonController.viewSessionLessons(session.getId()));
    }


    private Result showListSessionLessons(Session session, Page<SessionLesson> currentPage, String orderBy, String orderDir, String filterString, Map<String, String> lessonSlugsMap) {
        LazyHtml content = new LazyHtml(listSessionLessonsView.render(session.getId(), currentPage, orderBy, orderDir, filterString, lessonSlugsMap));
        content.appendLayout(c -> headingWithActionLayout.render(Messages.get("session.lessons"), new InternalLink(Messages.get("commons.add"), routes.SessionLessonController.addLesson(session.getId())), c));
        SessionControllerUtils.appendUpdateLayout(content, session);
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, session);
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Sessions - Lessons");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    private Result showAddLesson(Session session, Form<SessionLessonAddForm> form) {
        LazyHtml content = new LazyHtml(addLessonView.render(session.getId(), form));
        SessionControllerUtils.appendUpdateLayout(content, session);
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, session,
              new InternalLink(Messages.get("commons.add"), routes.SessionLessonController.addLesson(session.getId()))
        );
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Sessions - Lessons - Create");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    private Result showEditLesson(Session session, SessionLesson sessionLesson, Form<SessionLessonEditForm> form) {
        LazyHtml content = new LazyHtml(editSessionLessonView.render(form, session.getId(), sessionLesson));
        SessionControllerUtils.appendUpdateLayout(content, session);
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content, session,
                new InternalLink(Messages.get("commons.update"), routes.SessionLessonController.editLesson(session.getId(), sessionLesson.getId()))
        );
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Sessions - Lessons - Update");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    private void appendBreadcrumbsLayout(LazyHtml content, Session session, InternalLink... lastLinks) {
        ImmutableList.Builder<InternalLink> breadcrumbsBuilder = SessionControllerUtils.getBreadcrumbsBuilder();
        breadcrumbsBuilder.add(new InternalLink(Messages.get("session.lessons"), routes.SessionController.jumpToLessons(session.getId())));
        breadcrumbsBuilder.add(new InternalLink(Messages.get("commons.view"), routes.SessionLessonController.viewSessionLessons(session.getId())));
        breadcrumbsBuilder.add(lastLinks);

        JerahmeelControllerUtils.getInstance().appendBreadcrumbsLayout(content, breadcrumbsBuilder.build());
    }
}
