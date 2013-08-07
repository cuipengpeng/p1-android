package com.p1.mobile.p1android.ui.phone;

import android.os.Bundle;

import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.ui.fragment.AboutFragment;

public class AboutActivity extends FlurryFragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_activity_layout);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.triButton, new AboutFragment()).commit();
    }

}
