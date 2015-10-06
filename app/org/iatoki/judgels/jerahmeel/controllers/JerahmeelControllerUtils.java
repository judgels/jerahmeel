package org.iatoki.judgels.jerahmeel.controllers;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.api.jophiel.JophielClientAPI;
import org.iatoki.judgels.api.jophiel.JophielPublicAPI;
import org.iatoki.judgels.api.sandalphon.SandalphonResourceDisplayNameUtils;
import org.iatoki.judgels.jerahmeel.JerahmeelUtils;
import org.iatoki.judgels.jerahmeel.PointStatistic;
import org.iatoki.judgels.jerahmeel.ProblemScoreStatistic;
import org.iatoki.judgels.jerahmeel.ProblemStatistic;
import org.iatoki.judgels.jerahmeel.SubmissionEntry;
import org.iatoki.judgels.jerahmeel.services.PointStatisticService;
import org.iatoki.judgels.jerahmeel.services.ProblemScoreStatisticService;
import org.iatoki.judgels.jerahmeel.services.ProblemStatisticService;
import org.iatoki.judgels.jerahmeel.services.impls.ActivityLogServiceImpl;
import org.iatoki.judgels.jerahmeel.services.impls.JidCacheServiceImpl;
import org.iatoki.judgels.jerahmeel.views.html.widget.pointStatisticView;
import org.iatoki.judgels.jerahmeel.views.html.widget.problemScoreStatisticLayout;
import org.iatoki.judgels.jerahmeel.views.html.widget.problemStatisticView;
import org.iatoki.judgels.jerahmeel.views.html.widget.recentSubmissionView;
import org.iatoki.judgels.jophiel.ActivityKey;
import org.iatoki.judgels.jophiel.UserActivityMessage;
import org.iatoki.judgels.jophiel.controllers.JophielClientControllerUtils;
import org.iatoki.judgels.jophiel.forms.SearchProfileForm;
import org.iatoki.judgels.jophiel.forms.ViewpointForm;
import org.iatoki.judgels.jophiel.services.impls.UserActivityMessageServiceImpl;
import org.iatoki.judgels.jophiel.views.html.client.linkedClientsLayout;
import org.iatoki.judgels.jophiel.views.html.isLoggedInLayout;
import org.iatoki.judgels.jophiel.views.html.isLoggedOutLayout;
import org.iatoki.judgels.jophiel.views.html.profile.searchProfileLayout;
import org.iatoki.judgels.jophiel.views.html.viewas.viewAsLayout;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.JudgelsPlayUtils;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsControllerUtils;
import org.iatoki.judgels.play.controllers.ControllerUtils;
import org.iatoki.judgels.play.views.html.layouts.contentLayout;
import org.iatoki.judgels.play.views.html.layouts.guestLoginView;
import org.iatoki.judgels.play.views.html.layouts.menusLayout;
import org.iatoki.judgels.play.views.html.layouts.profileView;
import org.iatoki.judgels.play.views.html.layouts.sidebarLayout;
import org.iatoki.judgels.play.views.html.layouts.threeWidgetLayout;
import org.iatoki.judgels.sandalphon.BundleSubmission;
import org.iatoki.judgels.sandalphon.ProgrammingSubmission;
import org.iatoki.judgels.sandalphon.services.BundleSubmissionService;
import org.iatoki.judgels.sandalphon.services.ProgrammingSubmissionService;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Http;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class JerahmeelControllerUtils extends AbstractJudgelsControllerUtils {

    private static JerahmeelControllerUtils INSTANCE;

    private final JophielClientAPI jophielClientAPI;
    private final JophielPublicAPI jophielPublicAPI;
    private final BundleSubmissionService bundleSubmissionService;
    private final PointStatisticService pointStatisticService;
    private final ProblemScoreStatisticService problemScoreStatisticService;
    private final ProblemStatisticService problemStatisticService;
    private final ProgrammingSubmissionService programmingSubmissionService;

    public JerahmeelControllerUtils(JophielClientAPI jophielClientAPI, JophielPublicAPI jophielPublicAPI, BundleSubmissionService bundleSubmissionService, PointStatisticService pointStatisticService, ProblemScoreStatisticService problemScoreStatisticService, ProblemStatisticService problemStatisticService, ProgrammingSubmissionService programmingSubmissionService) {
        this.jophielClientAPI = jophielClientAPI;
        this.jophielPublicAPI = jophielPublicAPI;
        this.bundleSubmissionService = bundleSubmissionService;
        this.pointStatisticService = pointStatisticService;
        this.problemScoreStatisticService = problemScoreStatisticService;
        this.problemStatisticService = problemStatisticService;
        this.programmingSubmissionService = programmingSubmissionService;
    }

    @Override
    public void appendSidebarLayout(LazyHtml content) {
        content.appendLayout(c -> contentLayout.render(c));

        if (Http.Context.current().session().containsKey("problemJid")) {
            String problemJid = Http.Context.current().session().get("problemJid");
            Http.Context.current().session().remove("problemJid");

            addProblemWidget(content, problemJid);
        }

        ImmutableList.Builder<InternalLink> internalLinkBuilder = ImmutableList.builder();
        internalLinkBuilder.add(new InternalLink(Messages.get("training.training"), routes.TrainingController.index()));
        internalLinkBuilder.add(new InternalLink(Messages.get("submission.submissions"), routes.SubmissionController.jumpToSubmissions()));
        internalLinkBuilder.add(new InternalLink(Messages.get("statistic.statistics"), routes.StatisticController.index()));
        if (isAdmin()) {
            internalLinkBuilder.add(new InternalLink(Messages.get("curriculum.curriculums"), routes.CurriculumController.viewCurriculums()));
            internalLinkBuilder.add(new InternalLink(Messages.get("course.courses"), routes.CourseController.viewCourses()));
            internalLinkBuilder.add(new InternalLink(Messages.get("session.sessions"), routes.SessionController.viewSessions()));
            internalLinkBuilder.add(new InternalLink(Messages.get("user.users"), routes.UserController.index()));
        }
        LazyHtml sidebarContent;
        if (JerahmeelUtils.isGuest()) {
            sidebarContent = new LazyHtml(guestLoginView.render(routes.ApplicationController.auth(ControllerUtils.getCurrentUrl(Http.Context.current().request())).absoluteURL(Http.Context.current().request(), Http.Context.current().request().secure()), JophielClientControllerUtils.getInstance().getRegisterUrl().toString()));
        } else {
            sidebarContent = new LazyHtml(profileView.render(
                    IdentityUtils.getUsername(),
                    IdentityUtils.getUserRealName(),
                    org.iatoki.judgels.jophiel.controllers.routes.JophielClientController.profile().absoluteURL(Http.Context.current().request(), Http.Context.current().request().secure()),
                    org.iatoki.judgels.jophiel.controllers.routes.JophielClientController.logout(ControllerUtils.getCurrentUrl(Http.Context.current().request())).absoluteURL(Http.Context.current().request(), Http.Context.current().request().secure())
                ));
        }
        if (JerahmeelUtils.trullyHasRole("admin")) {
            Form<ViewpointForm> form = Form.form(ViewpointForm.class);
            if (JudgelsPlayUtils.hasViewPoint()) {
                ViewpointForm viewpointForm = new ViewpointForm();
                viewpointForm.username = IdentityUtils.getUsername();
                form.fill(viewpointForm);
            }
            sidebarContent.appendLayout(c -> viewAsLayout.render(form, jophielPublicAPI.getUserAutocompleteAPIEndpoint(), "lib/jophielcommons/javascripts/userAutoComplete.js", org.iatoki.judgels.jerahmeel.controllers.routes.ApplicationController.postViewAs(), org.iatoki.judgels.jerahmeel.controllers.routes.ApplicationController.resetViewAs(), c));
        }
        sidebarContent.appendLayout(c -> menusLayout.render(internalLinkBuilder.build(), c));
        sidebarContent.appendLayout(c -> linkedClientsLayout.render(jophielClientAPI.getLinkedClientsAPIEndpoint(), "lib/jophielcommons/javascripts/linkedClients.js", c));
        Form<SearchProfileForm> searchProfileForm = Form.form(SearchProfileForm.class);
        sidebarContent.appendLayout(c -> searchProfileLayout.render(searchProfileForm, jophielPublicAPI.getUserAutocompleteAPIEndpoint(), "lib/jophielcommons/javascripts/userAutoComplete.js", JophielClientControllerUtils.getInstance().getUserViewProfileUrl(), c));

        content.appendLayout(c -> sidebarLayout.render(sidebarContent.render(), c));

        if (isInTrainingMainPage()) {
            addWidgets(content);
        }

        if (JerahmeelUtils.isGuest()) {
            content.appendLayout(c -> isLoggedInLayout.render(jophielClientAPI.getUserIsLoggedInAPIEndpoint(), routes.ApplicationController.auth(ControllerUtils.getCurrentUrl(Http.Context.current().request())).absoluteURL(Http.Context.current().request(), Http.Context.current().request().secure()), "lib/jophielcommons/javascripts/isLoggedIn.js", c));
        } else {
            content.appendLayout(c -> isLoggedOutLayout.render(jophielClientAPI.getUserIsLoggedInAPIEndpoint(), org.iatoki.judgels.jophiel.controllers.routes.JophielClientController.logout(ControllerUtils.getCurrentUrl(Http.Context.current().request())).absoluteURL(Http.Context.current().request(), Http.Context.current().request().secure()), "lib/jophielcommons/javascripts/isLoggedOut.js", JerahmeelUtils.getRealUserJid(), c));
        }
    }

    public boolean isAdmin() {
        return JerahmeelUtils.hasRole("admin");
    }

    public void addActivityLog(ActivityKey activityKey) {
        if (!JerahmeelUtils.isGuest()) {
            long time = System.currentTimeMillis();
            ActivityLogServiceImpl.getInstance().addActivityLog(activityKey, JerahmeelUtils.getRealUsername(), time, JerahmeelUtils.getRealUserJid(), IdentityUtils.getIpAddress());
            String log = JerahmeelUtils.getRealUsername() + " " + activityKey.toString();
            try {
                if (JudgelsPlayUtils.hasViewPoint()) {
                    log += " view as " + IdentityUtils.getUsername();
                }
                UserActivityMessageServiceImpl.getInstance().addUserActivityMessage(new UserActivityMessage(System.currentTimeMillis(), JerahmeelUtils.getRealUserJid(), log, IdentityUtils.getIpAddress()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static synchronized void buildInstance(JophielClientAPI jophielClientAPI, JophielPublicAPI jophielPublicAPI, BundleSubmissionService bundleSubmissionService, PointStatisticService pointStatisticService, ProblemScoreStatisticService problemScoreStatisticService, ProblemStatisticService problemStatisticService, ProgrammingSubmissionService programmingSubmissionService) {
        if (INSTANCE != null) {
            throw new UnsupportedOperationException("ControllerUtils instance has already been built");
        }
        INSTANCE = new JerahmeelControllerUtils(jophielClientAPI, jophielPublicAPI, bundleSubmissionService, pointStatisticService, problemScoreStatisticService, problemStatisticService, programmingSubmissionService);
    }

    private boolean isInTrainingMainPage() {
        return ControllerUtils.getCurrentUrl(Http.Context.current().request()).equals(routes.TrainingController.index().absoluteURL(Http.Context.current().request(), Http.Context.current().request().secure()));
    }

    private void addProblemWidget(LazyHtml content, String problemJid) {
        if (problemScoreStatisticService.problemScoreStatisticExists(problemJid)) {
            ProblemScoreStatistic problemScoreStatistic = problemScoreStatisticService.getLatestProblemScoreStatisticWithPagination(problemJid, 0, 5, "id", "asc", "");
            content.appendLayout(c -> problemScoreStatisticLayout.render(problemScoreStatistic, c));
        }
    }

    private void addWidgets(LazyHtml content) {
        PointStatistic pointStatistic;
        if (pointStatisticService.pointStatisticExists()) {
            pointStatistic = pointStatisticService.getLatestPointStatisticWithPagination(0, 5, "id", "asc", "");
        } else {
            pointStatistic = null;
        }

        ProblemStatistic problemStatistic;
        if (problemStatisticService.problemStatisticExists()) {
            problemStatistic = problemStatisticService.getLatestProblemStatisticWithPagination(0, 5, "id", "asc", "");
        } else {
            problemStatistic = null;
        }

        List<SubmissionEntry> submissionEntries = Lists.newArrayList();
        Page<BundleSubmission> pageOfBundleSubmissions = bundleSubmissionService.getPageOfBundleSubmissions(0, 5, "timeCreate", "desc", null, null, null);
        Page<ProgrammingSubmission> pageOfProgrammingSubmissions = programmingSubmissionService.getPageOfProgrammingSubmissions(0, 5, "timeCreate", "desc", null, null, null);
        for (BundleSubmission bundleSubmission : pageOfBundleSubmissions.getData()) {
            submissionEntries.add(new SubmissionEntry(bundleSubmission.getAuthorJid(), bundleSubmission.getProblemJid(), bundleSubmission.getLatestScore(), bundleSubmission.getTime().getTime()));
        }
        for (ProgrammingSubmission programmingSubmission : pageOfProgrammingSubmissions.getData()) {
            submissionEntries.add(new SubmissionEntry(programmingSubmission.getAuthorJid(), programmingSubmission.getProblemJid(), programmingSubmission.getLatestScore(), programmingSubmission.getTime().getTime()));
        }

        List<String> problemJids = submissionEntries.stream().map(e -> e.getProblemJid()).collect(Collectors.toList());
        Map<String, String> problemTitlesMap = SandalphonResourceDisplayNameUtils.buildTitlesMap(JidCacheServiceImpl.getInstance().getDisplayNames(problemJids), "en-US");

        Collections.sort(submissionEntries);

        content.appendLayout(c -> threeWidgetLayout.render(pointStatisticView.render(pointStatistic), problemStatisticView.render(problemStatistic), recentSubmissionView.render(submissionEntries, problemTitlesMap), c));
    }

    static JerahmeelControllerUtils getInstance() {
        if (INSTANCE == null) {
            throw new UnsupportedOperationException("ControllerUtils instance has not been built");
        }
        return INSTANCE;
    }
}
