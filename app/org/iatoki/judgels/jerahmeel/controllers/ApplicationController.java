package org.iatoki.judgels.jerahmeel.controllers;

import org.iatoki.judgels.jerahmeel.JerahmeelUtils;
import org.iatoki.judgels.jerahmeel.User;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.securities.HasRole;
import org.iatoki.judgels.jerahmeel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jerahmeel.services.UserService;
import org.iatoki.judgels.jerahmeel.services.impls.AvatarCacheServiceImpl;
import org.iatoki.judgels.jerahmeel.services.impls.JidCacheServiceImpl;
import org.iatoki.judgels.jophiel.Jophiel;
import org.iatoki.judgels.jophiel.forms.ViewpointForm;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.JudgelsPlayUtils;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import play.data.Form;
import play.db.jpa.Transactional;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;

@Singleton
@Named
public final class ApplicationController extends AbstractJudgelsController {

    private final Jophiel jophiel;
    private final UserService userService;

    @Inject
    public ApplicationController(Jophiel jophiel, UserService userService) {
        this.jophiel = jophiel;
        this.userService = userService;
    }

    public Result index() {
        if (session().containsKey("username") && session().containsKey("role")) {
            return redirect(routes.TrainingController.jumpToCurriculums());
        } else if (session().containsKey("username")) {
            String returnUri = routes.TrainingController.jumpToCurriculums().absoluteURL(request(), request().secure());
            return redirect(routes.ApplicationController.authRole(returnUri));
        } else {
            String returnUri = routes.TrainingController.jumpToCurriculums().absoluteURL(request(), request().secure());
            return redirect(routes.ApplicationController.auth(returnUri));
        }
    }

    public Result auth(String returnUri) {
        if (session().containsKey("username") && session().containsKey("role")) {
            return redirect(returnUri);
        } else if (session().containsKey("username")) {
            return redirect(routes.ApplicationController.authRole(returnUri));
        } else {
            String newReturnUri = org.iatoki.judgels.jerahmeel.controllers.routes.ApplicationController.afterLogin(returnUri).absoluteURL(request(), request().secure());
            return redirect(org.iatoki.judgels.jophiel.controllers.routes.JophielClientController.login(newReturnUri));
        }
    }

    @Transactional
    public Result authRole(String returnUri) {
        if (session().containsKey("username") && session().containsKey("role")) {
            return redirect(returnUri);
        }

        String userRoleJid = IdentityUtils.getUserJid();
        if (!userService.existsByUserJid(userRoleJid)) {
            userService.createUser(userRoleJid, JerahmeelUtils.getDefaultRoles());
            JerahmeelUtils.saveRolesInSession(JerahmeelUtils.getDefaultRoles());
            return redirect(returnUri);
        }

        User userRole = userService.findUserByJid(userRoleJid);
        JerahmeelUtils.saveRolesInSession(userRole.getRoles());
        return redirect(returnUri);
    }

    @Transactional
    public Result afterLogin(String returnUri) {
        if (!session().containsKey("role")) {
            String newReturnUri = org.iatoki.judgels.jerahmeel.controllers.routes.ApplicationController.afterLogin(returnUri).absoluteURL(request(), request().secure());
            return redirect(routes.ApplicationController.authRole(newReturnUri));
        }

        JudgelsPlayUtils.updateUserJidCache(JidCacheServiceImpl.getInstance());
        Jophiel.updateUserAvatarCache(AvatarCacheServiceImpl.getInstance());

        if (JudgelsPlayUtils.hasViewPoint()) {
            try {
                JerahmeelUtils.backupSession();
                JerahmeelUtils.setUserSession(jophiel.getPublicUserByJid(JudgelsPlayUtils.getViewPoint()), userService.findUserByJid(JudgelsPlayUtils.getViewPoint()));
            } catch (IOException e) {
                JudgelsPlayUtils.removeViewPoint();
                JerahmeelUtils.restoreSession();
            }
        }
        return redirect(returnUri);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    public Result postViewAs() {
        Form<ViewpointForm> viewpointForm = Form.form(ViewpointForm.class).bindFromRequest();

        if (!formHasErrors(viewpointForm) && JerahmeelUtils.trullyHasRole("admin")) {
            ViewpointForm viewpointData = viewpointForm.get();
            try {
                String userJid = jophiel.verifyUsername(viewpointData.username);
                if (userJid != null) {
                    try {
                        userService.upsertUserFromJophielUserJid(userJid);
                        if (!JudgelsPlayUtils.hasViewPoint()) {
                            JerahmeelUtils.backupSession();
                        }
                        JudgelsPlayUtils.setViewPointInSession(userJid);
                        JerahmeelUtils.setUserSession(jophiel.getPublicUserByJid(userJid), userService.findUserByJid(userJid));

                        JerahmeelControllerUtils.getInstance().addActivityLog("View as user " + viewpointData.username + ".");

                    } catch (IOException e) {
                        JudgelsPlayUtils.removeViewPoint();
                        JerahmeelUtils.restoreSession();
                    }
                }
            } catch (IOException e) {
                // do nothing
                e.printStackTrace();
            }
        }
        return redirectToReferer();
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    public Result resetViewAs() {
        JudgelsPlayUtils.removeViewPoint();
        JerahmeelUtils.restoreSession();

        return redirectToReferer();
    }
}
