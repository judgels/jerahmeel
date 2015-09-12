package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.iatoki.judgels.api.JudgelsAPIClientException;
import org.iatoki.judgels.api.jophiel.JophielPublicAPI;
import org.iatoki.judgels.api.jophiel.JophielUser;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.headingLayout;
import org.iatoki.judgels.play.views.html.layouts.headingWithActionLayout;
import org.iatoki.judgels.jerahmeel.services.impls.JidCacheServiceImpl;
import org.iatoki.judgels.jerahmeel.JerahmeelUtils;
import org.iatoki.judgels.jerahmeel.User;
import org.iatoki.judgels.jerahmeel.forms.UserCreateForm;
import org.iatoki.judgels.jerahmeel.UserNotFoundException;
import org.iatoki.judgels.jerahmeel.services.UserService;
import org.iatoki.judgels.jerahmeel.forms.UserUpdateForm;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.securities.Authorized;
import org.iatoki.judgels.jerahmeel.controllers.securities.HasRole;
import org.iatoki.judgels.jerahmeel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jerahmeel.views.html.user.createUserView;
import org.iatoki.judgels.jerahmeel.views.html.user.listUsersView;
import org.iatoki.judgels.jerahmeel.views.html.user.updateUserView;
import org.iatoki.judgels.jerahmeel.views.html.user.viewUserView;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Authenticated(value = {LoggedIn.class, HasRole.class})
@Authorized(value = "admin")
@Singleton
@Named
public final class UserController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;

    private final JophielPublicAPI jophielPublicAPI;
    private final UserService userService;

    @Inject
    public UserController(JophielPublicAPI jophielPublicAPI, UserService userService) {
        this.jophielPublicAPI = jophielPublicAPI;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public Result index() {
        return listUsers(0, "id", "asc", "");
    }

    @Transactional(readOnly = true)
    public Result listUsers(long pageIndex, String sortBy, String orderBy, String filterString) {
        Page<User> pageOfUsers = userService.getPageOfUsers(pageIndex, PAGE_SIZE, sortBy, orderBy, filterString);

        LazyHtml content = new LazyHtml(listUsersView.render(pageOfUsers, sortBy, orderBy, filterString));
        content.appendLayout(c -> headingWithActionLayout.render(Messages.get("user.list"), new InternalLink(Messages.get("commons.create"), routes.UserController.createUser()), c));
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        JerahmeelControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("user.users"), routes.UserController.index())
        ));
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "Users - List");

        JerahmeelControllerUtils.getInstance().addActivityLog("List all users <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result createUser() {
        UserCreateForm userCreateData = new UserCreateForm();
        userCreateData.roles = StringUtils.join(JerahmeelUtils.getDefaultRoles(), ",");
        Form<UserCreateForm> userCreateForm = Form.form(UserCreateForm.class).fill(userCreateData);

        JerahmeelControllerUtils.getInstance().addActivityLog("Try to create user <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return showCreateUser(userCreateForm);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postCreateUser() {
        Form<UserCreateForm> userCreateForm = Form.form(UserCreateForm.class).bindFromRequest();

        if (formHasErrors(userCreateForm)) {
            return showCreateUser(userCreateForm);
        }

        UserCreateForm userCreateData = userCreateForm.get();
        JophielUser jophielUser;
        try {
            jophielUser = jophielPublicAPI.findUserByUsername(userCreateData.username);
        } catch (JudgelsAPIClientException e) {
            userCreateForm.reject(Messages.get("user.create.error.usernameNotFound"));
            return showCreateUser(userCreateForm);
        }

        if (jophielUser == null) {
            userCreateForm.reject(Messages.get("user.create.error.usernameNotFound"));
            return showCreateUser(userCreateForm);
        }

        if (userService.existsByUserJid(jophielUser.getJid())) {
            userCreateForm.reject(Messages.get("user.create.error.userAlreadyExists"));
            return showCreateUser(userCreateForm);
        }

        userService.upsertUserFromJophielUser(jophielUser, userCreateData.getRolesAsList(), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        JerahmeelControllerUtils.getInstance().addActivityLog("Create user " + jophielUser.getJid() + ".");

        return redirect(routes.UserController.index());
    }

    @Transactional(readOnly = true)
    public Result viewUser(long userId) throws UserNotFoundException {
        User user = userService.findUserById(userId);
        LazyHtml content = new LazyHtml(viewUserView.render(user));
        content.appendLayout(c -> headingWithActionLayout.render(Messages.get("user.user") + " #" + user.getId() + ": " + JidCacheServiceImpl.getInstance().getDisplayName(user.getUserJid()), new InternalLink(Messages.get("commons.update"), routes.UserController.updateUser(user.getId())), c));
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        JerahmeelControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("user.users"), routes.UserController.index()),
                new InternalLink(Messages.get("user.view"), routes.UserController.viewUser(user.getId()))
        ));
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "User - View");

        JerahmeelControllerUtils.getInstance().addActivityLog("View user " + user.getUserJid() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result updateUser(long userId) throws UserNotFoundException {
        User user = userService.findUserById(userId);
        UserUpdateForm userUpdateForm = new UserUpdateForm();
        userUpdateForm.roles = StringUtils.join(user.getRoles(), ",");
        Form<UserUpdateForm> form = Form.form(UserUpdateForm.class).fill(userUpdateForm);

        JerahmeelControllerUtils.getInstance().addActivityLog("Try to update user " + user.getUserJid() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return showUpdateUser(form, user);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postUpdateUser(long userId) throws UserNotFoundException {
        User user = userService.findUserById(userId);
        Form<UserUpdateForm> userUpdateForm = Form.form(UserUpdateForm.class).bindFromRequest();

        if (formHasErrors(userUpdateForm)) {
            return showUpdateUser(userUpdateForm, user);
        }

        UserUpdateForm userUpdateData = userUpdateForm.get();
        userService.updateUser(user.getUserJid(), userUpdateData.getRolesAsList(), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        JerahmeelControllerUtils.getInstance().addActivityLog("Update user " + user.getUserJid() + ".");

        return redirect(routes.UserController.index());
    }

    @Transactional
    public Result deleteUser(long userId) throws UserNotFoundException {
        User user = userService.findUserById(userId);
        userService.deleteUser(user.getUserJid());

        JerahmeelControllerUtils.getInstance().addActivityLog("Delete user " + user.getUserJid() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return redirect(routes.UserController.index());
    }

    private Result showCreateUser(Form<UserCreateForm> userCreateForm) {
        LazyHtml content = new LazyHtml(createUserView.render(userCreateForm, jophielPublicAPI.getUserAutocompleteAPIEndpoint()));
        content.appendLayout(c -> headingLayout.render(Messages.get("user.create"), c));
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        JerahmeelControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("user.users"), routes.UserController.index()),
                new InternalLink(Messages.get("user.create"), routes.UserController.createUser())
        ));
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "User - Create");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }

    private Result showUpdateUser(Form<UserUpdateForm> userUpdateForm, User user) {
        LazyHtml content = new LazyHtml(updateUserView.render(userUpdateForm, user.getId()));
        content.appendLayout(c -> headingLayout.render(Messages.get("user.user") + " #" + user.getId() + ": " + JidCacheServiceImpl.getInstance().getDisplayName(user.getUserJid()), c));
        JerahmeelControllerUtils.getInstance().appendSidebarLayout(content);
        JerahmeelControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("user.users"), routes.UserController.index()),
                new InternalLink(Messages.get("user.update"), routes.UserController.updateUser(user.getId()))
        ));
        JerahmeelControllerUtils.getInstance().appendTemplateLayout(content, "User - Update");

        return JerahmeelControllerUtils.getInstance().lazyOk(content);
    }
}
