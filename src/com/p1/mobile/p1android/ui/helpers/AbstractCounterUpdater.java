package com.p1.mobile.p1android.ui.helpers;

import android.util.Log;

import com.p1.mobile.p1android.content.Account;
import com.p1.mobile.p1android.content.Content;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.logic.ReadAccount;

public abstract class AbstractCounterUpdater implements IContentRequester {
    static final String TAG = AbstractCounterUpdater.class.getSimpleName();

    private CounterListener mListener;

    public interface CounterListener {
        public void onCounterUpdate(int count);
    }

    public void setCounterListener(CounterListener listener) {
        mListener = listener;
        Account account = ReadAccount.requestAccount(this);
        contentChanged(account);
    }

    @Override
    public void contentChanged(Content content) {
        if (content instanceof Account) {
            int count = getCount((Account) content);
            if (mListener == null) {
                Log.e(TAG, "No listener set");
                return;
            }
            mListener.onCounterUpdate(count);
        }
    }

    public void onDestroy() {
        ContentHandler.getInstance().removeRequester(this);
    }

    protected abstract int getCount(Account account);

}
