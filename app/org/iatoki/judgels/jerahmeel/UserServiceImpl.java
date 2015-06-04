package org.iatoki.judgels.jerahmeel;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.JudgelsUtils;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.UserDao;
import org.iatoki.judgels.jerahmeel.models.domains.UserModel;
import org.iatoki.judgels.jophiel.commons.Jophiel;
import org.iatoki.judgels.jophiel.commons.UserTokens;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public final class UserServiceImpl implements UserService {

    private final Jophiel jophiel;
    private final UserDao userDao;

    public UserServiceImpl(Jophiel jophiel, UserDao userDao) {
        this.jophiel = jophiel;
        this.userDao = userDao;
    }

    @Override
    public void upsertUser(String userJid, String accessToken, String idToken, String refreshToken, long expireTime) {
        if (userDao.existsByUserJid(userJid)) {
            UserModel userModel = userDao.findByUserJid(userJid);

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
        return userDao.existsByUserJid(userJid);
    }

    @Override
    public User findUserById(long userId) throws UserNotFoundException {
        UserModel userModel = userDao.findById(userId);
        if (userModel != null) {
            return createUserFromUserModel(userModel);
        } else {
            throw new UserNotFoundException("User not found.");
        }
    }

    @Override
    public User findUserByUserJid(String userJid) {
        UserModel userModel = userDao.findByUserJid(userJid);
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
        UserModel userRoleModel = userDao.findById(userId);
        userRoleModel.roles = StringUtils.join(roles, ",");

        userDao.edit(userRoleModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void deleteUser(long userId) {
        UserModel userModel = userDao.findById(userId);
        userDao.remove(userModel);
    }

    @Override
    public Page<User> pageUsers(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = userDao.countByFilters(filterString, ImmutableMap.of(), ImmutableMap.of());
        List<UserModel> userRoleModels = userDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(), ImmutableMap.of(), pageIndex * pageSize, pageSize);
        List<User> userRoles = Lists.transform(userRoleModels, m -> createUserFromUserModel(m));
        return new Page<>(userRoles, totalPages, pageIndex, pageSize);
    }

    @Override
    public void upsertUserFromJophielUserJid(String userJid) {
        try {
            org.iatoki.judgels.jophiel.commons.User user = jophiel.getUserByUserJid(userJid);

            if (!userDao.existsByUserJid(userJid))
                createUser(user.getJid(), JerahmeelUtils.getDefaultRoles());

            JidCacheService.getInstance().putDisplayName(user.getJid(), JudgelsUtils.getUserDisplayName(user.getUsername(), user.getName()), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
            AvatarCacheService.getInstance().putImageUrl(user.getJid(), user.getProfilePictureUrl(), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        } catch (IOException e) {
            // do nothing
        }
    }

    @Override
    public UserTokens getUserTokensByUserJid(String userJid) {
        UserModel userModel = userDao.findByUserJid(userJid);

        return createUserTokensFromUserModel(userModel);
    }

    private UserTokens createUserTokensFromUserModel(UserModel userModel) {
        return new UserTokens(userModel.userJid, userModel.accessToken, userModel.refreshToken, userModel.idToken, userModel.expirationTime);
    }

    private User createUserFromUserModel(UserModel userModel) {
        return new User(userModel.id, userModel.userJid, Arrays.asList(userModel.roles.split(",")));
    }
}
