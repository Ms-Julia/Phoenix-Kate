package biz.dealnote.messenger.mvp.presenter.search;

import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.List;

import biz.dealnote.messenger.domain.IOwnersRepository;
import biz.dealnote.messenger.domain.Repository;
import biz.dealnote.messenger.fragment.search.criteria.PeopleSearchCriteria;
import biz.dealnote.messenger.fragment.search.nextfrom.IntNextFrom;
import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.mvp.view.search.IPeopleSearchView;
import biz.dealnote.messenger.util.Pair;
import io.reactivex.rxjava3.core.Single;

public class PeopleSearchPresenter extends AbsSearchPresenter<IPeopleSearchView, PeopleSearchCriteria, User, IntNextFrom> {

    private final IOwnersRepository ownersRepository;

    public PeopleSearchPresenter(int accountId, @Nullable PeopleSearchCriteria criteria, @Nullable Bundle savedInstanceState) {
        super(accountId, criteria, savedInstanceState);
        ownersRepository = Repository.INSTANCE.getOwners();
    }

    @Override
    IntNextFrom getInitialNextFrom() {
        return new IntNextFrom(0);
    }

    @Override
    boolean isAtLast(IntNextFrom startFrom) {
        return startFrom.getOffset() == 0;
    }

    @Override
    Single<Pair<List<User>, IntNextFrom>> doSearch(int accountId, PeopleSearchCriteria criteria, IntNextFrom startFrom) {
        int offset = startFrom.getOffset();
        int nextOffset = offset + 50;

        return ownersRepository.searchPeoples(accountId, criteria, 50, offset)
                .map(users -> Pair.Companion.create(users, new IntNextFrom(nextOffset)));
    }

    @Override
    PeopleSearchCriteria instantiateEmptyCriteria() {
        return new PeopleSearchCriteria("");
    }

    @Override
    boolean canSearch(PeopleSearchCriteria criteria) {
        return true;
    }

    public void fireUserClick(User user) {
        getView().openUserWall(getAccountId(), user);
    }
}