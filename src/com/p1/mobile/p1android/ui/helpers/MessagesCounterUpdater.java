package com.p1.mobile.p1android.ui.helpers;

import com.p1.mobile.p1android.content.Account;
import com.p1.mobile.p1android.content.Account.AccountIOSession;

public class MessagesCounterUpdater extends AbstractCounterUpdater {

    @Override
    protected int getCount(Account account) {
        AccountIOSession io = account.getIOSession();
        int count = 0;
        try {
            count = io.getUnreadMessages();
        } finally {
            io.close();
        }
        return count;
    }
}
