package org.iatoki.judgels.jerahmeel.services;

import org.iatoki.judgels.api.jophiel.JophielUser;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.jerahmeel.User;
import org.iatoki.judgels.jerahmeel.UserNotFoundException;
import org.iatoki.judgels.jophiel.services.BaseUserService;

import java.util.List;

public interface UserService extends BaseUserService {

    User findUserById(long userId) throws UserNotFoundException;

    User findUserByJid(String userJid);

    void createUser(String userJid, List<String> roles);

    void updateUser(long userId, List<String> roles);

    void deleteUser(long userId);

    Page<User> getPageOfUsers(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    void upsertUserFromJophielUser(JophielUser jophielUser);

    void upsertUserFromJophielUser(JophielUser jophielUser, List<String> roles);
}
