package biz.dealnote.messenger.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.fragment.attachments.PostCreateFragment;
import biz.dealnote.messenger.model.EditingPostType;
import biz.dealnote.messenger.model.WallEditorAttrs;
import biz.dealnote.messenger.util.AssertUtils;
import biz.dealnote.messenger.util.Objects;

public class PostCreateActivity extends NoMainActivity {

    public static Intent newIntent(@NonNull Context context, int accountId, @NonNull WallEditorAttrs attrs, @Nullable ArrayList<Uri> streams, @Nullable String links, @Nullable String mime) {
        return new Intent(context, PostCreateActivity.class)
                .putExtra(Extra.ACCOUNT_ID, accountId)
                .putParcelableArrayListExtra("streams", streams)
                .putExtra("attrs", attrs)
                .putExtra("links", links)
                .putExtra(Extra.TYPE, mime);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Objects.isNull(savedInstanceState)) {
            AssertUtils.requireNonNull(getIntent().getExtras());

            int accountId = getIntent().getExtras().getInt(Extra.ACCOUNT_ID);
            ArrayList<Uri> streams = getIntent().getParcelableArrayListExtra("streams");
            WallEditorAttrs attrs = getIntent().getParcelableExtra("attrs");
            String links = getIntent().getStringExtra("links");
            String mime = getIntent().getStringExtra(Extra.TYPE);

            Bundle args = PostCreateFragment.buildArgs(accountId, attrs.getOwner().getOwnerId(), EditingPostType.TEMP, null, attrs, streams, links, mime);

            PostCreateFragment fragment = PostCreateFragment.newInstance(args);
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit)
                    .replace(getMainContainerViewId(), fragment)
                    .addToBackStack(null)
                    .commitAllowingStateLoss();
        }
    }
}
