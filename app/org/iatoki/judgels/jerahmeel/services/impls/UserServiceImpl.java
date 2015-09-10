package org.iatoki.judgels.jerahmeel.services.impls;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.iatoki.judgels.api.jophiel.JophielUser;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.JudgelsPlayUtils;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.jerahmeel.JerahmeelUtils;
import org.iatoki.judgels.jerahmeel.UserNotFoundException;
import org.iatoki.judgels.jerahmeel.models.daos.UserDao;
import org.iatoki.judgels.jerahmeel.models.entities.UserModel;
import org.iatoki.judgels.jerahmeel.services.UserService;
import org.iatoki.judgels.jophiel.UserTokens;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;

@Singleton
@Named("userService")
public final class UserServiceImpl implements UserService {

    private final UserDao userDao;

    @Inject
    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public void upsertUser(String userJid, String accessToken, String idToken, String refreshToken, long expireTime) {
        if (userDao.existsByJid(userJid)) {
            UserModel userModel = userDao.findByJid(userJid);

            userModel.accessToken = accessToken;
            userModel.refreshToken = refreshToken;
            userModel.idToken = idToken;
            userModel.expirationTime = expireTime;

            userDao.edit(userModel, "guest", IdentityUtils.getIpAddress());
        } else {
            UserModel userModel = new UserModel();
            userModel.userJid = userJid;
            userModel.roles = StringUtils.join(JerahmeelUtils.getDefaultRoles(), ",");

            userModel.accessToken = accessToken;
            userModel.refreshToken = refreshToken;
            userModel.idToken = idToken;
            userModel.expirationTime = expireTime;

            userDao.persist(userModel, "guest", IdentityUtils.getIpAddress());
        }
    }

    @Override
    public boolean existsByUserJid(String userJid) {
        return userDao.existsByJid(userJid);
    }

    @Override
    public org.iatoki.judgels.jerahmeel.User findUserById(long userId) throws UserNotFoundException {
        UserModel userModel = userDao.findById(userId);
        if (userModel == null) {
            throw new UserNotFoundException("User not found.");
        }

        return createUserFromUserModel(userModel);
    }

    @Override
    public org.iatoki.judgels.jerahmeel.User findUserByJid(String userJid) {
        UserModel userModel = userDao.findByJid(userJid);
        return createUserFromUserModel(userModel);
    }

    @Override
    public void createUser(String userJid, List<String> roles) {
        UserModel userModel = new UserModel();
        userModel.userJid = userJid;
        userModel.roles = StringUtils.join(roles, ",");

        userDao.persist(userModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void updateUser(long userId, List<String> roles) {
        UserModel userModel = userDao.findById(userId);
        userModel.roles = StringUtils.join(roles, ",");

        userDao.edit(userModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void deleteUser(long userId) {
        UserModel userModel = userDao.findById(userId);
        userDao.remove(userModel);
    }

    @Override
    public Page<org.iatoki.judgels.jerahmeel.User> getPageOfUsers(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = userDao.countByFilters(filterString, ImmutableMap.of(), ImmutableMap.of());
        List<UserModel> userModels = userDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(), ImmutableMap.of(), pageIndex * pageSize, pageSize);
        List<org.iatoki.judgels.jerahmeel.User> users = Lists.transform(userModels, m -> createUserFromUserModel(m));
        return new Page<>(users, totalPages, pageIndex, pageSize);
    }

    @Override
    public void upsertUserFromJophielUser(JophielUser jophielUser) {
        upsertUserFromJophielUser(jophielUser, JerahmeelUtils.getDefaultRoles());
    }

    @Override
    public void upsertUserFromJophielUser(JophielUser jophielUser, List<String> roles) {
        if (!userDao.existsByJid(jophielUser.getJid())) {
            createUser(jophielUser.getJid(), roles);
        }

        JidCacheServiceImpl.getInstance().putDisplayName(jophielUser.getJid(), JudgelsPlayUtils.getUserDisplayName(jophielUser.getUsername()), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        AvatarCacheServiceImpl.getInstance().putImageUrl(jophielUser.getJid(), jophielUser.getProfilePictureUrl(), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public UserTokens getUserTokensByUserJid(String userJid) {
        UserModel userModel = userDao.findByJid(userJid);

        return createUserTokensFromUserModel(userModel);
    }

    private UserTokens createUserTokensFromUserModel(UserModel userModel) {
        return new UserTokens(userModel.userJid, userModel.accessToken, userModel.refreshToken, userModel.idToken, userModel.expirationTime);
    }

    private org.iatoki.judgels.jerahmeel.User createUserFromUserModel(UserModel userModel) {
        return new org.iatoki.judgels.jerahmeel.User(userModel.id, userModel.userJid, Arrays.asList(userModel.roles.split(",")));
    }
}
