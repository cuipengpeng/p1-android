package com.p1.mobile.p1android.content.logic;

import android.util.Log;

import com.p1.mobile.p1android.content.Comment;
import com.p1.mobile.p1android.content.Comment.CommentIOSession;

public class DeleteComment {
    public static final String TAG = DeleteComment.class.getSimpleName();

    public static void deleteComment(Comment comment) {
        CommentIOSession io = comment.getIOSession();
        try {
            io.setValid(false);
        } finally {
            io.close();
        }
        comment.notifyListeners();
        Log.w(TAG, "Delete is not yet fully implemented");
    }

    public static void reportComment(Comment comment) {
        Log.w(TAG, "Reporting is not yet fully implemented");
    }

}
