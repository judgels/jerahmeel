package org.iatoki.judgels.jerahmeel.controllers.api.internal;

import org.iatoki.judgels.AutoComplete;
import org.iatoki.judgels.jerahmeel.CourseSession;
import org.iatoki.judgels.jerahmeel.CourseSessionNotFoundException;
import org.iatoki.judgels.jerahmeel.Session;
import org.iatoki.judgels.jerahmeel.UserItemStatus;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jerahmeel.services.CourseSessionService;
import org.iatoki.judgels.jerahmeel.services.SessionDependencyService;
import org.iatoki.judgels.jerahmeel.services.SessionService;
import org.iatoki.judgels.jerahmeel.services.UserItemService;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.controllers.apis.AbstractJudgelsAPIController;
import play.db.jpa.Transactional;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.stream.Collectors;

@Named
public final class InternalSessionAPIController extends AbstractJudgelsAPIController {

    private final CourseSessionService courseSessionService;
    private final SessionDependencyService sessionDependencyService;
    private final SessionService sessionService;
    private final UserItemService userItemService;

    @Inject
    public InternalSessionAPIController(CourseSessionService courseSessionService, SessionDependencyService sessionDependencyService, SessionService sessionService, UserItemService userItemService) {
        this.courseSessionService = courseSessionService;
        this.sessionDependencyService = sessionDependencyService;
        this.sessionService = sessionService;
        this.userItemService = userItemService;
    }

    @Authenticated(LoggedIn.class)
    @Transactional
    public Result autocompleteSession(String term) {
        List<Session> sessions = sessionService.getSessionsByTerm(term);
        List<AutoComplete> autocompletedSessions = sessions.stream()
                .map(c -> new AutoComplete("" + c.getId(), c.getJid(), c.getName()))
                .collect(Collectors.toList());
        return okAsJson(autocompletedSessions);
    }

    @Authenticated(LoggedIn.class)
    @Transactional
    public Result updateSessionViewStatus(long courseSessionId) throws CourseSessionNotFoundException {
        CourseSession courseSession = courseSessionService.findCourseSessionById(courseSessionId);
        Session session = sessionService.findSessionByJid(courseSession.getSessionJid());
        if (!userItemService.userItemExistsByUserJidAndItemJid(IdentityUtils.getUserJid(), session.getJid()) && sessionDependencyService.isDependenciesFulfilled(IdentityUtils.getUserJid(), session.getJid())) {
            userItemService.upsertUserItem(IdentityUtils.getUserJid(), session.getJid(), UserItemStatus.VIEWED, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        }

        return ok();
    }
}
