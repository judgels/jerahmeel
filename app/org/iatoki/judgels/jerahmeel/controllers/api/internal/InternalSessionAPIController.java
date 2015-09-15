package org.iatoki.judgels.jerahmeel.controllers.api.internal;

import com.google.gson.Gson;
import org.iatoki.judgels.AutoComplete;
import org.iatoki.judgels.jerahmeel.Session;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jerahmeel.services.SessionService;
import org.iatoki.judgels.play.controllers.apis.AbstractJudgelsAPIController;
import play.db.jpa.Transactional;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.stream.Collectors;

@Named
public final class InternalSessionAPIController extends AbstractJudgelsAPIController {

    private final SessionService sessionService;

    @Inject
    public InternalSessionAPIController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Authenticated(LoggedIn.class)
    @Transactional
    public Result autocompleteSession(String term) {
        List<Session> sessions = sessionService.getSessionsByTerm(term);
        List<AutoComplete> autocompletedSessions = sessions.stream()
                .map(c -> new AutoComplete("" + c.getId(), c.getName(), c.getName()))
                .collect(Collectors.toList());
        return ok(new Gson().toJson(autocompletedSessions));
    }
}
