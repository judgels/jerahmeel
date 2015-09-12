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

    void createUser(String userJid, List<String> roles, String createUserJid, String createUserIpAddress);

    void updateUser(String userJid, List<String> roles, String updateUserJid, String updateUserIpAddress);

    void deleteUser(String userJid);

    Page<User> getPageOfUsers(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    void upsertUserFromJophielUser(JophielUser jophielUser, String userJid, String userIpAddress);

    void upsertUserFromJophielUser(JophielUser jophielUser, List<String> roles, String userJid, String userIpAddress);
}
