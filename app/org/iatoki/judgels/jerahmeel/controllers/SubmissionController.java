package org.iatoki.judgels.jerahmeel.controllers;

import org.iatoki.judgels.jerahmeel.JerahmeelUtils;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.securities.GuestView;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import play.mvc.Result;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named
public final class SubmissionController extends AbstractJudgelsController {

    @Authenticated(value = GuestView.class)
    public Result jumpToSubmissions() {
        if (JerahmeelControllerUtils.getInstance().isAdmin() || JerahmeelUtils.isGuest()) {
            return redirect(routes.SubmissionController.jumpToAllSubmissions());
        }

        return redirect(routes.SubmissionController.jumpToOwnSubmissions());
    }

    @Authenticated(value = GuestView.class)
    public Result jumpToOwnSubmissions() {
        return redirect(routes.ProgrammingSubmissionController.viewOwnSubmissions());
    }

    @Authenticated(value = GuestView.class)
    public Result jumpToAllSubmissions() {
        return redirect(routes.ProgrammingSubmissionController.viewSubmissions());
    }
}
