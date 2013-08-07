package com.p1.mobile.p1android.ui.phone;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

import com.p1.mobile.p1android.content.Content;
import com.p1.mobile.p1android.content.Conversation;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.User;
import com.p1.mobile.p1android.content.User.UserIOSession;
import com.p1.mobile.p1android.content.logic.ReadAccount;
import com.p1.mobile.p1android.content.logic.ReadConversation;
import com.p1.mobile.p1android.content.logic.ReadFeed;
import com.p1.mobile.p1android.content.logic.ReadUser;
import com.p1.mobile.p1android.content.logic.WriteComment;
import com.p1.mobile.p1android.content.logic.WritePicture;
import com.p1.mobile.p1android.content.logic.WriteUser;

public class TestActivity extends FlurryFragmentActivity implements
        IContentRequester {
    protected static final String TAG = TestActivity.class.getSimpleName();

    private static final int SHARE_IMAGE_PICK = 1337;
    private static final int PROFILE_IMAGE_PICK = 1338;

    private int shouldChangeCityCount = 0;

    private LinearLayout layout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        setContentView(layout);

        addGetFeedButton();
        addCommentShareButton();
        addGetAccountButton();
        addChangeCityButton();
        addNewConversationButton();
        addProfilePictureButton();

    }

    private void addGetFeedButton() {
        Button button = new Button(this);
        button.setText("Feed request");
        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                ReadFeed.requestFeed(null);

            }
        });
        layout.addView(button);
    }

    private void addGetAccountButton() {
        Button button = new Button(this);
        button.setText("Account request");
        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                ReadAccount.requestAccount(null);

            }
        });
        layout.addView(button);
    }

    private void addProfilePictureButton() {
        Button button = new Button(this);
        button.setText("Change profile pic.");
        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent photoPickIntent = new Intent(Intent.ACTION_PICK);
                photoPickIntent.setType("image/*");
                startActivityForResult(photoPickIntent, PROFILE_IMAGE_PICK);

            }
        });
        layout.addView(button);
    }

    private void addCommentShareButton() {
        Button button = new Button(this);
        button.setText("Comment share");
        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                WriteComment.sendShareComment("Comment from Android! 良好! åäö!",
                        "9378");

            }
        });
        layout.addView(button);
    }

    private void addChangeCityButton() {
        Button button = new Button(this);
        button.setText("Change City");
        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                shouldChangeCityCount++;
                User user = ReadUser.requestLoggedInUser(TestActivity.this);
                contentChanged(user);
            }
        });
        layout.addView(button);
    }

    private void addNewConversationButton() {
        Button button = new Button(this);
        button.setText("New conversation");
        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Conversation conversation = ReadConversation
                        .requestConversation("3", TestActivity.this);
                ReadConversation.fillConversation(conversation);
            }
        });
        layout.addView(button);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PROFILE_IMAGE_PICK && resultCode == RESULT_OK) {
            Uri imageUri = null;
            if (data != null) {
                imageUri = data.getData();
            }
            WritePicture.setProfilePicture(getApplicationContext(),
                    imageUri.toString());
        }
    }

    @Override
    public void contentChanged(Content content) {
        if (content instanceof User) {

            User user = (User) content;
            UserIOSession io = user.getIOSession();
            try {
                if (io.isValid() && shouldChangeCityCount > 0) {
                    if (io.getCity().equals("Beijing, China"))
                        WriteUser.changeCity(user, "Beijing");
                    else {
                        WriteUser.changeCity(user, "Beijing, China");
                    }
                    shouldChangeCityCount--;
                }

            } finally {
                io.close();
            }

        }

    }

}
