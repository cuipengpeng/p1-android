package com.p1.mobile.p1android.ui.phone;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.ui.fragment.NewConversationFragment;

public class NewConversationActivity extends FlurryFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.new_conversation_activity);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.activity_content, NewConversationFragment.newInstance());
        ft.commit();
    }
}
