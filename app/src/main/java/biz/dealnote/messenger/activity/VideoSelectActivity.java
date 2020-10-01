package biz.dealnote.messenger.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.fragment.VideosFragment;
import biz.dealnote.messenger.fragment.VideosTabsFragment;
import biz.dealnote.messenger.fragment.search.SingleTabSearchFragment;
import biz.dealnote.messenger.mvp.view.IVideosListView;
import biz.dealnote.messenger.place.Place;
import biz.dealnote.messenger.place.PlaceProvider;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.util.Utils;

public class VideoSelectActivity extends NoMainActivity implements PlaceProvider {

    /**
     * @param accountId От чьего имени получать
     * @param ownerId   Чьи получать
     */
    public static Intent createIntent(Context context, int accountId, int ownerId) {
        return new Intent(context, VideoSelectActivity.class)
                .putExtra(Extra.ACCOUNT_ID, accountId)
                .putExtra(Extra.OWNER_ID, ownerId);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Objects.isNull(savedInstanceState)) {
            int accountId = getIntent().getExtras().getInt(Extra.ACCOUNT_ID);
            int ownerId = getIntent().getExtras().getInt(Extra.OWNER_ID);
            attachInitialFragment(accountId, ownerId);
        }
    }

    private void attachInitialFragment(int accountId, int ownerId) {
        VideosTabsFragment fragment = VideosTabsFragment.newInstance(accountId, ownerId, IVideosListView.ACTION_SELECT);
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.fragment_enter_pop, R.anim.fragment_exit_pop)
                .replace(getMainContainerViewId(), fragment)
                .addToBackStack("video-tabs")
                .commit();
    }

    @Override
    public void openPlace(Place place) {
        if (place.type == Place.VIDEO_ALBUM) {
            Fragment fragment = VideosFragment.newInstance(place.getArgs());
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.fragment_enter_pop, R.anim.fragment_exit_pop)
                    .replace(getMainContainerViewId(), fragment)
                    .addToBackStack("video-album")
                    .commit();
        } else if (place.type == Place.SINGLE_SEARCH) {
            SingleTabSearchFragment singleTabSearchFragment = SingleTabSearchFragment.newInstance(place.getArgs());
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.fragment_enter_pop, R.anim.fragment_exit_pop)
                    .replace(getMainContainerViewId(), singleTabSearchFragment)
                    .addToBackStack("video-search")
                    .commit();
        } else if (place.type == Place.VIDEO_PREVIEW) {
            Intent intent = new Intent();
            intent.putParcelableArrayListExtra(Extra.ATTACHMENTS, Utils.singletonArrayList(place.getArgs().getParcelable(Extra.VIDEO)));
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }
}
