package biz.dealnote.messenger.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.collection.LongSparseArray;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import java.lang.ref.WeakReference;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.fragment.base.BaseFragment;
import biz.dealnote.messenger.listener.BackPressCallback;
import biz.dealnote.messenger.model.Peer;
import biz.dealnote.messenger.util.Objects;

import static biz.dealnote.messenger.util.Objects.nonNull;

public class DialogsTabsFragment extends BaseFragment implements BackPressCallback {

    private int accountId;
    private int messagesOwnerId;
    private Peer peer;
    private ViewPager2 viewPager;
    private Adapter adapter;
    private int mCurrentTab;
    private int offset;

    public static DialogsTabsFragment newInstance(int accountId, int messagesOwnerId, Peer peer, int Offset) {
        DialogsTabsFragment fragment = new DialogsTabsFragment();
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.OWNER_ID, messagesOwnerId);
        args.putParcelable(Extra.PEER, peer);
        args.putInt(Extra.OFFSET, Offset);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        accountId = requireArguments().getInt(Extra.ACCOUNT_ID);
        messagesOwnerId = requireArguments().getInt(Extra.OWNER_ID);
        peer = requireArguments().getParcelable(Extra.PEER);
        offset = requireArguments().getInt(Extra.OFFSET);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dialogs_tabs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        viewPager = view.findViewById(R.id.fragment_dialogs_pager);
        viewPager.setOffscreenPageLimit(1);
        adapter = new Adapter(this);
        viewPager.setAdapter(adapter);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (mCurrentTab == 1 && position == 0) {
                    Fragment tt = adapter.findFragmentByPosition(mCurrentTab);
                    if (tt instanceof ChatFragment) {
                        ((ChatFragment) tt).saveState();
                    }
                }
                mCurrentTab = position;
                ((InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(viewPager.getWindowToken(), 0);
            }
        });

        viewPager.setCurrentItem(1, false);
    }

    @Override
    public boolean onBackPressed() {
        if (nonNull(adapter)) {
            Fragment fragment = adapter.findFragmentByPosition(mCurrentTab);

            boolean ret = !(fragment instanceof BackPressCallback) || ((BackPressCallback) fragment).onBackPressed();
            if (ret && viewPager.getCurrentItem() > 0) {
                viewPager.setCurrentItem(0, true);
                return false;
            }
            return ret;
        }
        return true;
    }

    public void DisableTouch(boolean Enable) {
        viewPager.setUserInputEnabled(Enable);
    }

    class Adapter extends FragmentStateAdapter {
        private final LongSparseArray<WeakReference<Fragment>> fragments;

        public Adapter(@NonNull Fragment fm) {
            super(fm);
            fragments = new LongSparseArray<>();
        }

        public void removeFragment(int position) {
            fragments.removeAt(position);
            notifyItemRemoved(position);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Fragment ret = null;
            switch (position) {
                case 0:
                    ret = DialogsFragment.newInstance(accountId, messagesOwnerId, null, offset);
                    break;
                case 1:
                    ret = ChatFragment.Companion.newInstance(accountId, messagesOwnerId, peer);
                    break;
            }
            if (ret == null)
                throw new UnsupportedOperationException();
            fragments.put(position, new WeakReference<>(ret));
            return ret;
        }

        @Override
        public int getItemCount() {
            return 2;
        }

        public Fragment findFragmentByPosition(int position) {
            WeakReference<Fragment> weak = fragments.get(position);
            return Objects.isNull(weak) ? null : weak.get();
        }
    }
}
