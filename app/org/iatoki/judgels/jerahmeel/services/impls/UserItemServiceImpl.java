package org.iatoki.judgels.jerahmeel.services.impls;

import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.jerahmeel.UserItem;
import org.iatoki.judgels.jerahmeel.UserItemStatus;
import org.iatoki.judgels.jerahmeel.models.daos.UserItemDao;
import org.iatoki.judgels.jerahmeel.models.entities.UserItemModel;
import org.iatoki.judgels.jerahmeel.services.UserItemService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
@Named("userItemService")
public final class UserItemServiceImpl implements UserItemService {

    private final UserItemDao userItemDao;

    @Inject
    public UserItemServiceImpl(UserItemDao userItemDao) {
        this.userItemDao = userItemDao;
    }

    @Override
    public boolean isViewed(String userJid, String itemJid) {
        return userItemDao.existByUserJidAndItemJid(userJid, itemJid);
    }

    @Override
    public boolean isUserItemExist(String userJid, String itemJid) {
        return userItemDao.existByUserJidAndItemJid(userJid, itemJid);
    }

    @Override
    public boolean isUserItemExist(String userJid, String itemJid, UserItemStatus status) {
        return userItemDao.existByUserJidItemJidAndStatus(userJid, itemJid, status.name());
    }

    @Override
    public void upsertUserItem(String userJid, String itemJid, UserItemStatus status) {
        if (userItemDao.existByUserJidAndItemJid(userJid, itemJid)) {
            UserItemModel userItemModel = userItemDao.findByUserJidAndItemJid(userJid, itemJid);
            userItemModel.status = status.name();

            userItemDao.edit(userItemModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        } else {
            UserItemModel userItemModel = new UserItemModel();
            userItemModel.userJid = userJid;
            userItemModel.itemJid = itemJid;
            userItemModel.status = status.name();

            userItemDao.persist(userItemModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        }
    }

    @Override
    public List<UserItem> findAllUserItemByUserJid(String userJid) {
        return userItemDao.findByUserJid(userJid).stream().map(u -> createFromModel(u)).collect(Collectors.toList());
    }

    @Override
    public List<UserItem> findAllUserItemByItemJid(String itemJid) {
        return userItemDao.findByItemJid(itemJid).stream().map(u -> createFromModel(u)).collect(Collectors.toList());
    }

    private UserItem createFromModel(UserItemModel u) {
        return new UserItem(u.userJid, u.itemJid, UserItemStatus.valueOf(u.status));
    }
}
