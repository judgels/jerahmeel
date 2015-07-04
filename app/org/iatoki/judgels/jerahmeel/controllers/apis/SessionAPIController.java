package org.iatoki.judgels.jerahmeel.controllers.apis;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.commons.AutoComplete;
import org.iatoki.judgels.jerahmeel.Session;
import org.iatoki.judgels.jerahmeel.services.SessionService;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.securities.LoggedIn;
import play.data.DynamicForm;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named
public final class SessionAPIController extends Controller {

    private final SessionService sessionService;

    @Inject
    public SessionAPIController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Authenticated(LoggedIn.class)
    @Transactional
    public Result sessionAutoCompleteList() {
        response().setHeader("Access-Control-Allow-Origin", "*");
        response().setContentType("application/javascript");

        DynamicForm form = DynamicForm.form().bindFromRequest();
        String term = form.get("term");
        List<Session> sessions = sessionService.findAllSessionByTerm(term);
        ImmutableList.Builder<AutoComplete> responseBuilder = ImmutableList.builder();

        for (Session session : sessions) {
            responseBuilder.add(new AutoComplete(session.getJid(), session.getJid(), session.getName()));
        }

        String callback = form.get("callback");

        return ok(callback + "(" + Json.toJson(responseBuilder.build()).toString() + ")");
    }

}
