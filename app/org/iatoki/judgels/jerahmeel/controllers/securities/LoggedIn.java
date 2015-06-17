package org.iatoki.judgels.jerahmeel.controllers.securities;

import org.iatoki.judgels.jophiel.controllers.securities.BaseLoggedIn;
import play.mvc.Call;
import play.mvc.Http;

public class LoggedIn extends BaseLoggedIn {

    @Override
    public Call getRedirectCall(Http.Context context) {
        context.session().remove("role");
        return org.iatoki.judgels.jerahmeel.controllers.routes.ApplicationController.auth(context.request().uri());
    }

}
