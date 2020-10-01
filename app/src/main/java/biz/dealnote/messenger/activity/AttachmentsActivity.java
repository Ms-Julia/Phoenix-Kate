package biz.dealnote.messenger.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.fragment.DocsFragment;
import biz.dealnote.messenger.fragment.VideosFragment;
import biz.dealnote.messenger.fragment.VideosTabsFragment;
import biz.dealnote.messenger.model.Types;
import biz.dealnote.messenger.mvp.presenter.DocsListPresenter;
import biz.dealnote.messenger.mvp.view.IVideosListView;
import biz.dealnote.messenger.place.Place;
import biz.dealnote.messenger.place.PlaceProvider;

public class AttachmentsActivity extends NoMainActivity implements PlaceProvider {

    public static Intent createIntent(Context context, int accountId, int type) {
        return new Intent(context, AttachmentsActivity.class)
                .putExtra(Extra.TYPE, type)
                .putExtra(Extra.ACCOUNT_ID, accountId);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            Fragment fragment = null;

            int type = getIntent().getExtras().getInt(Extra.TYPE);
            int accountId = getIntent().getExtras().getInt(Extra.ACCOUNT_ID);

            switch (type) {
                case Types.DOC:
                    fragment = DocsFragment.newInstance(accountId, accountId, DocsListPresenter.ACTION_SELECT);
                    break;

                case Types.VIDEO:
                    fragment = VideosTabsFragment.newInstance(accountId, accountId, IVideosListView.ACTION_SELECT);
                    break;
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit)
                    .replace(R.id.fragment, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void openPlace(Place place) {
        if (place.type == Place.VIDEO_ALBUM) {
            Fragment fragment = VideosFragment.newInstance(place.getArgs());
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.fragment_enter_pop, R.anim.fragment_exit_pop)
                    .replace(R.id.fragment, fragment)
                    .addToBackStack("video_album")
                    .commit();
        }
    }
}
