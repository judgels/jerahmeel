package org.iatoki.judgels.jerahmeel.controllers;

import org.iatoki.judgels.commons.controllers.BaseController;
import org.iatoki.judgels.jerahmeel.controllers.security.Authenticated;
import org.iatoki.judgels.jerahmeel.controllers.security.Authorized;
import org.iatoki.judgels.jerahmeel.controllers.security.HasRole;
import org.iatoki.judgels.jerahmeel.controllers.security.LoggedIn;
import play.db.jpa.Transactional;

@Transactional
@Authenticated(value = {LoggedIn.class, HasRole.class})
@Authorized(value = {"admin"})
public final class SessionSubmissionController extends BaseController {


}
