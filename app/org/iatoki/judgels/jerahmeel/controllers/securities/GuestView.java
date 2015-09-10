package org.iatoki.judgels.jerahmeel.controllers.securities;

import org.iatoki.judgels.jophiel.JophielSessionUtils;
import org.iatoki.judgels.play.IdentityUtils;
import play.mvc.Http;
import play.mvc.Security;

public class GuestView extends Security.Authenticator {

    @Override
    public String getUsername(Http.Context context) {
        if ((IdentityUtils.getUserJid() == null) || !JophielSessionUtils.isSessionValid(Http.Context.current())) {
            context.session().put("userJid", "guest");
        }
        return IdentityUtils.getUserJid();
    }
}
