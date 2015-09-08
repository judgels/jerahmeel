package org.iatoki.judgels.jerahmeel.controllers.apis;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.AutoComplete;
import org.iatoki.judgels.jerahmeel.Session;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jerahmeel.services.SessionService;
import org.iatoki.judgels.play.controllers.apis.AbstractJudgelsAPIController;
import play.data.DynamicForm;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named
public final class SessionAPIController extends AbstractJudgelsAPIController {

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

        DynamicForm dForm = DynamicForm.form().bindFromRequest();
        String callback = dForm.get("callback");
        String term = dForm.get("term");

        List<Session> sessions = sessionService.getSessionsByTerm(term);
        ImmutableList.Builder<AutoComplete> autoCompleteBuilder = ImmutableList.builder();

        for (Session session : sessions) {
            autoCompleteBuilder.add(new AutoComplete(session.getJid(), session.getJid(), session.getName()));
        }

        return ok(createJsonPResponse(callback, Json.toJson(autoCompleteBuilder.build()).toString()));
    }

}
