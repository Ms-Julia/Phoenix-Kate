package biz.dealnote.messenger.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.fragment.AddProxyFragment;
import biz.dealnote.messenger.fragment.ProxyManagerFrgament;
import biz.dealnote.messenger.place.Place;
import biz.dealnote.messenger.place.PlaceProvider;
import biz.dealnote.messenger.util.Objects;

public class ProxyManagerActivity extends NoMainActivity implements PlaceProvider {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Objects.isNull(savedInstanceState)) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.fragment_enter_pop, R.anim.fragment_exit_pop)
                    .replace(getMainContainerViewId(), ProxyManagerFrgament.newInstance())
                    .addToBackStack("proxy-manager")
                    .commit();
        }
    }

    @Override
    public void openPlace(Place place) {
        if (place.type == Place.PROXY_ADD) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.fragment_enter_pop, R.anim.fragment_exit_pop)
                    .replace(getMainContainerViewId(), AddProxyFragment.newInstance())
                    .addToBackStack("proxy-add")
                    .commit();
        }
    }
}
