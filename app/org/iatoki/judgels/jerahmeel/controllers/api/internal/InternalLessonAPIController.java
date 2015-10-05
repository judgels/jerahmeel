package org.iatoki.judgels.jerahmeel.controllers.api.internal;

import org.iatoki.judgels.jerahmeel.SessionLesson;
import org.iatoki.judgels.jerahmeel.SessionLessonNotFoundException;
import org.iatoki.judgels.jerahmeel.UserItemStatus;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jerahmeel.services.SessionLessonService;
import org.iatoki.judgels.jerahmeel.services.UserItemService;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.controllers.apis.AbstractJudgelsAPIController;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public final class InternalLessonAPIController extends AbstractJudgelsAPIController {

    private final SessionLessonService sessionLessonService;
    private final UserItemService userItemService;

    @Inject
    public InternalLessonAPIController(SessionLessonService sessionLessonService, UserItemService userItemService) {
        this.sessionLessonService = sessionLessonService;
        this.userItemService = userItemService;
    }

    @Authenticated(LoggedIn.class)
    @Transactional
    public Result updateLessonViewStatus(long sessionLessonId) throws SessionLessonNotFoundException {
        JPA.em().getTransaction().begin();

        SessionLesson sessionLesson = sessionLessonService.findSessionLessonById(sessionLessonId);
        if (!userItemService.userItemExistsByUserJidAndItemJidAndStatus(IdentityUtils.getUserJid(), sessionLesson.getLessonJid(), UserItemStatus.COMPLETED)) {
            userItemService.upsertUserItem(IdentityUtils.getUserJid(), sessionLesson.getLessonJid(), UserItemStatus.COMPLETED, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        }

        JPA.em().getTransaction().commit();

        return ok();
    }
}
