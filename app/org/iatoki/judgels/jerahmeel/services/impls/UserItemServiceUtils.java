package org.iatoki.judgels.jerahmeel.services.impls;

import org.iatoki.judgels.jerahmeel.UserItem;
import org.iatoki.judgels.jerahmeel.UserItemStatus;
import org.iatoki.judgels.jerahmeel.models.entities.UserItemModel;

final class UserItemServiceUtils {

    private UserItemServiceUtils() {
        // prevent instantiation
    }

    static UserItem createFromModel(UserItemModel u) {
        return new UserItem(u.userJid, u.itemJid, UserItemStatus.valueOf(u.status));
    }
}
