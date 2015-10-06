package org.iatoki.judgels.jerahmeel.controllers.api.internal;

import org.iatoki.judgels.jerahmeel.SessionProblem;
import org.iatoki.judgels.jerahmeel.SessionProblemNotFoundException;
import org.iatoki.judgels.jerahmeel.UserItemStatus;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jerahmeel.services.SessionProblemService;
import org.iatoki.judgels.jerahmeel.services.UserItemService;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.controllers.apis.AbstractJudgelsAPIController;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public final class InternalProblemAPIController extends AbstractJudgelsAPIController {

    private final SessionProblemService sessionProblemService;
    private final UserItemService userItemService;

    @Inject
    public InternalProblemAPIController(SessionProblemService sessionProblemService, UserItemService userItemService) {
        this.sessionProblemService = sessionProblemService;
        this.userItemService = userItemService;
    }

    @Authenticated(LoggedIn.class)
    @Transactional
    public Result updateProblemViewStatus(long sessionProblemId) throws SessionProblemNotFoundException {
        JPA.em().getTransaction().commit();

        SessionProblem sessionProblem = sessionProblemService.findSessionProblemById(sessionProblemId);
        if (!userItemService.userItemExistsByUserJidAndItemJidAndStatus(IdentityUtils.getUserJid(), sessionProblem.getProblemJid(), UserItemStatus.COMPLETED)) {
            userItemService.upsertUserItem(IdentityUtils.getUserJid(), sessionProblem.getProblemJid(), UserItemStatus.VIEWED, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        }

        JPA.em().getTransaction().commit();

        return ok();
    }
}
