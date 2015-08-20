package org.iatoki.judgels.jerahmeel.services;

import org.iatoki.judgels.jerahmeel.UserItem;
import org.iatoki.judgels.jerahmeel.UserItemStatus;

import java.util.List;

public interface UserItemService {

    boolean userItemExistsByUserJidAndItemJid(String userJid, String itemJid);

    boolean userItemExistsByUserJidAndItemJidAndStatus(String userJid, String itemJid, UserItemStatus status);

    void upsertUserItem(String userJid, String itemJid, UserItemStatus status);

    List<UserItem> getUserItemsByUserJid(String userJid);

    List<UserItem> getUserItemsByItemJid(String itemJid);
}
