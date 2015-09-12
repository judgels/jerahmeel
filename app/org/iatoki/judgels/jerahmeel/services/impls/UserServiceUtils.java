package org.iatoki.judgels.jerahmeel.services.impls;

import org.iatoki.judgels.jerahmeel.User;
import org.iatoki.judgels.jerahmeel.models.entities.UserModel;
import org.iatoki.judgels.jophiel.UserTokens;

import java.util.Arrays;

final class UserServiceUtils {

    private UserServiceUtils() {
        // prevent instantiation
    }

    static UserTokens createUserTokensFromUserModel(UserModel userModel) {
        return new UserTokens(userModel.userJid, userModel.accessToken, userModel.refreshToken, userModel.idToken, userModel.expirationTime);
    }

    static User createUserFromUserModel(UserModel userModel) {
        return new User(userModel.id, userModel.userJid, Arrays.asList(userModel.roles.split(",")));
    }
}
