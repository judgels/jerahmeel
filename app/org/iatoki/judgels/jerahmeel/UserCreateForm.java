package org.iatoki.judgels.jerahmeel;

import play.data.validation.Constraints;

public final class UserCreateForm {

    public UserCreateForm() {

    }

    @Constraints.Required
    public String username;

    @Constraints.Required
    public String roles;
}
