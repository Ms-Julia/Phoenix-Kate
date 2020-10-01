package biz.dealnote.messenger.activity;

import android.os.Bundle;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.fragment.LocalImageAlbumsFragment;
import biz.dealnote.messenger.fragment.LocalPhotosFragment;
import biz.dealnote.messenger.fragment.SinglePhotoFragment;
import biz.dealnote.messenger.model.LocalImageAlbum;
import biz.dealnote.messenger.place.Place;
import biz.dealnote.messenger.place.PlaceProvider;

public class PhotosActivity extends NoMainActivity implements PlaceProvider {

    public static final String EXTRA_MAX_SELECTION_COUNT = "max_selection_count";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            attachAlbumsFragment();
        }
    }

    private void attachAlbumsFragment() {
        LocalImageAlbumsFragment ignoredFragment = new LocalImageAlbumsFragment();
        ignoredFragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit)
                .replace(R.id.fragment, ignoredFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void openPlace(Place place) {
        if (place.type == Place.LOCAL_IMAGE_ALBUM) {
            int maxSelectionCount = getIntent().getIntExtra(EXTRA_MAX_SELECTION_COUNT, 10);
            LocalImageAlbum album = place.getArgs().getParcelable(Extra.ALBUM);
            LocalPhotosFragment localPhotosFragment = LocalPhotosFragment.newInstance(maxSelectionCount, album, false);
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.fragment_enter_pop, R.anim.fragment_exit_pop)
                    .replace(R.id.fragment, localPhotosFragment)
                    .addToBackStack("photos")
                    .commit();
        } else if (place.type == Place.SINGLE_PHOTO) {
            SinglePhotoFragment localPhotosFragment = SinglePhotoFragment.newInstance(place.getArgs());
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.fragment_enter_pop, R.anim.fragment_exit_pop)
                    .replace(R.id.fragment, localPhotosFragment)
                    .addToBackStack("preview")
                    .commit();
        }
    }
}
