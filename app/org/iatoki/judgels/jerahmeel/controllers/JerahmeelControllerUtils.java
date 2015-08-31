package org.iatoki.judgels.jerahmeel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.JudgelsPlayUtils;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.jophiel.UserActivityMessage;
import org.iatoki.judgels.jophiel.forms.ViewpointForm;
import org.iatoki.judgels.play.controllers.AbstractJudgelsControllerUtils;
import org.iatoki.judgels.play.controllers.ControllerUtils;
import org.iatoki.judgels.play.views.html.layouts.guestLoginView;
import org.iatoki.judgels.play.views.html.layouts.sidebarLayout;
import org.iatoki.judgels.play.views.html.layouts.profileView;
import org.iatoki.judgels.play.views.html.layouts.menusLayout;
import org.iatoki.judgels.jophiel.views.html.client.linkedClientsLayout;
import org.iatoki.judgels.jophiel.views.html.viewas.viewAsLayout;
import org.iatoki.judgels.jophiel.Jophiel;
import org.iatoki.judgels.jerahmeel.JerahmeelUtils;
import org.iatoki.judgels.jerahmeel.services.impls.UserActivityMessageServiceImpl;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Http;

public final class JerahmeelControllerUtils extends AbstractJudgelsControllerUtils {

    private static JerahmeelControllerUtils INSTANCE;

    private final Jophiel jophiel;

    public JerahmeelControllerUtils(Jophiel jophiel) {
        this.jophiel = jophiel;
    }

    @Override
    public void appendSidebarLayout(LazyHtml content) {
        ImmutableList.Builder<InternalLink> internalLinkBuilder = ImmutableList.builder();
        internalLinkBuilder.add(new InternalLink(Messages.get("training.training"), routes.TrainingController.jumpToCurriculums()));
        if (isAdmin()) {
            internalLinkBuilder.add(new InternalLink(Messages.get("curriculum.curriculums"), routes.CurriculumController.viewCurriculums()));
            internalLinkBuilder.add(new InternalLink(Messages.get("course.courses"), routes.CourseController.viewCourses()));
            internalLinkBuilder.add(new InternalLink(Messages.get("session.sessions"), routes.SessionController.viewSessions()));
            internalLinkBuilder.add(new InternalLink(Messages.get("user.users"), routes.UserController.index()));
        }
        LazyHtml sidebarContent;
        if (JerahmeelUtils.isGuest()) {
            sidebarContent = new LazyHtml(guestLoginView.render(routes.ApplicationController.auth(ControllerUtils.getCurrentUrl(Http.Context.current().request())).absoluteURL(Http.Context.current().request(), Http.Context.current().request().secure()), jophiel.getRegisterUri().toString()));
        } else {
            sidebarContent = new LazyHtml(profileView.render(
                    IdentityUtils.getUsername(),
                    IdentityUtils.getUserRealName(),
                    org.iatoki.judgels.jophiel.controllers.routes.JophielClientController.profile().absoluteURL(Http.Context.current().request(), Http.Context.current().request().secure()),
                    org.iatoki.judgels.jophiel.controllers.routes.JophielClientController.logout(routes.ApplicationController.index().absoluteURL(Http.Context.current().request(), Http.Context.current().request().secure())).absoluteURL(Http.Context.current().request(), Http.Context.current().request().secure())
                ));
        }
        if (JerahmeelUtils.trullyHasRole("admin")) {
            Form<ViewpointForm> form = Form.form(ViewpointForm.class);
            if (JudgelsPlayUtils.hasViewPoint()) {
                ViewpointForm viewpointForm = new ViewpointForm();
                viewpointForm.username = IdentityUtils.getUsername();
                form.fill(viewpointForm);
            }
            sidebarContent.appendLayout(c -> viewAsLayout.render(form, jophiel.getAutoCompleteEndPoint(), "lib/jophielcommons/javascripts/userAutoComplete.js", org.iatoki.judgels.jerahmeel.controllers.routes.ApplicationController.postViewAs(), org.iatoki.judgels.jerahmeel.controllers.routes.ApplicationController.resetViewAs(), c));
        }
        sidebarContent.appendLayout(c -> menusLayout.render(internalLinkBuilder.build(), c));
        sidebarContent.appendLayout(c -> linkedClientsLayout.render(jophiel.getLinkedClientsEndPoint(), "lib/jophielcommons/javascripts/linkedClients.js", c));

        content.appendLayout(c -> sidebarLayout.render(sidebarContent.render(), c));
    }

    public boolean isAdmin() {
        return JerahmeelUtils.hasRole("admin");
    }

    public void addActivityLog(String log) {
        if (!JerahmeelUtils.isGuest()) {
            try {
                UserActivityMessageServiceImpl.getInstance().addUserActivityMessage(new UserActivityMessage(System.currentTimeMillis(), IdentityUtils.getUserJid(), log, IdentityUtils.getIpAddress()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static synchronized void buildInstance(Jophiel jophiel) {
        if (INSTANCE != null) {
            throw new UnsupportedOperationException("ControllerUtils instance has already been built");
        }
        INSTANCE = new JerahmeelControllerUtils(jophiel);
    }

    static JerahmeelControllerUtils getInstance() {
        if (INSTANCE == null) {
            throw new UnsupportedOperationException("ControllerUtils instance has not been built");
        }
        return INSTANCE;
    }
}
