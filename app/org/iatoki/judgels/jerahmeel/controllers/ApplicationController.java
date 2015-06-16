package org.iatoki.judgels.jerahmeel.controllers;

import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.JudgelsUtils;
import org.iatoki.judgels.jophiel.controllers.forms.ViewpointForm;
import org.iatoki.judgels.commons.controllers.BaseController;
import org.iatoki.judgels.jophiel.Jophiel;
import org.iatoki.judgels.jerahmeel.AvatarCacheService;
import org.iatoki.judgels.jerahmeel.JidCacheService;
import org.iatoki.judgels.jerahmeel.JerahmeelUtils;
import org.iatoki.judgels.jerahmeel.User;
import org.iatoki.judgels.jerahmeel.UserService;
import org.iatoki.judgels.jerahmeel.controllers.security.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.security.HasRole;
import org.iatoki.judgels.jerahmeel.controllers.security.LoggedIn;
import play.data.Form;
import play.db.jpa.Transactional;
import play.mvc.Result;

import java.io.IOException;

@Transactional
public final class ApplicationController extends BaseController {

    private final Jophiel jophiel;
    private final UserService userService;

    public ApplicationController(Jophiel jophiel, UserService userService) {
        this.jophiel = jophiel;
        this.userService = userService;
    }

    public Result index() {
        if ((session().containsKey("username")) && (session().containsKey("role"))) {
            return redirect(routes.TrainingController.jumpToCurriculums());
        } else if (session().containsKey("username")) {
            String returnUri = routes.CurriculumController.viewCurriculums().absoluteURL(request(), request().secure());
            return redirect(routes.ApplicationController.authRole(returnUri));
        } else {
            String returnUri = routes.CurriculumController.viewCurriculums().absoluteURL(request(), request().secure());
            return redirect(routes.ApplicationController.auth(returnUri));
        }
    }

    public Result auth(String returnUri) {
        if ((session().containsKey("username")) && (session().containsKey("role"))) {
            return redirect(returnUri);
        } else if (session().containsKey("username")) {
            return redirect(routes.ApplicationController.authRole(returnUri));
        } else {
            returnUri = org.iatoki.judgels.jerahmeel.controllers.routes.ApplicationController.afterLogin(returnUri).absoluteURL(request(), request().secure());
            return redirect(org.iatoki.judgels.jophiel.controllers.routes.JophielClientController.login(returnUri));
        }
    }

    public Result authRole(String returnUri) {
        if ((session().containsKey("username")) && (session().containsKey("role"))) {
            return redirect(returnUri);
        } else {
            String userRoleJid = IdentityUtils.getUserJid();
            if (userService.existsByUserJid(userRoleJid)) {
                User userRole = userService.findUserByUserJid(userRoleJid);
                JerahmeelUtils.saveRolesInSession(userRole.getRoles());
                return redirect(returnUri);
            } else {
                userService.createUser(userRoleJid, JerahmeelUtils.getDefaultRoles());
                JerahmeelUtils.saveRolesInSession(JerahmeelUtils.getDefaultRoles());
                return redirect(returnUri);
            }
        }
    }

    public Result afterLogin(String returnUri) {
        if (session().containsKey("role")) {
            JudgelsUtils.updateUserJidCache(JidCacheService.getInstance());
            Jophiel.updateUserAvatarCache(AvatarCacheService.getInstance());

            if (JudgelsUtils.hasViewPoint()) {
                try {
                    JerahmeelUtils.backupSession();
                    JerahmeelUtils.setUserSession(jophiel.getUserByUserJid(JudgelsUtils.getViewPoint()), userService.findUserByUserJid(JudgelsUtils.getViewPoint()));
                } catch (IOException e) {
                    JudgelsUtils.removeViewPoint();
                    JerahmeelUtils.restoreSession();
                }
            }
            return redirect(returnUri);
        } else {
            returnUri = org.iatoki.judgels.jerahmeel.controllers.routes.ApplicationController.afterLogin(returnUri).absoluteURL(request(), request().secure());
            return redirect(routes.ApplicationController.authRole(returnUri));
        }
    }

    public Result afterProfile(String returnUri) {
        JudgelsUtils.updateUserJidCache(JidCacheService.getInstance());
        Jophiel.updateUserAvatarCache(AvatarCacheService.getInstance());

        return redirect(returnUri);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    public Result postViewAs() {
        Form<ViewpointForm> form = Form.form(ViewpointForm.class).bindFromRequest();

        if ((!(form.hasErrors() || form.hasGlobalErrors())) && (JerahmeelUtils.trullyHasRole("admin"))) {
            ViewpointForm viewpointForm = form.get();
            String userJid = jophiel.verifyUsername(viewpointForm.username);
            if (userJid != null) {
                try {
                    userService.upsertUserFromJophielUserJid(userJid);
                    if (!JudgelsUtils.hasViewPoint()) {
                        JerahmeelUtils.backupSession();
                    }
                    JudgelsUtils.setViewPointInSession(userJid);
                    JerahmeelUtils.setUserSession(jophiel.getUserByUserJid(userJid), userService.findUserByUserJid(userJid));

                    ControllerUtils.getInstance().addActivityLog("View as user " + viewpointForm.username + ".");

                } catch (IOException e) {
                    JudgelsUtils.removeViewPoint();
                    JerahmeelUtils.restoreSession();
                }
            }
        }
        return redirect(request().getHeader("Referer"));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    public Result resetViewAs() {
        JudgelsUtils.removeViewPoint();
        JerahmeelUtils.restoreSession();

        return redirect(request().getHeader("Referer"));
    }
}
