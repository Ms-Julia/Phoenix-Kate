package biz.dealnote.messenger.mvp.presenter.search;

import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.List;

import biz.dealnote.messenger.domain.IDocsInteractor;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.fragment.search.criteria.DocumentSearchCriteria;
import biz.dealnote.messenger.fragment.search.nextfrom.IntNextFrom;
import biz.dealnote.messenger.model.Document;
import biz.dealnote.messenger.mvp.view.search.IDocSearchView;
import biz.dealnote.messenger.util.Pair;
import biz.dealnote.messenger.util.Utils;
import io.reactivex.rxjava3.core.Single;

public class DocsSearchPresenter extends AbsSearchPresenter<IDocSearchView, DocumentSearchCriteria, Document, IntNextFrom> {

    private final IDocsInteractor docsInteractor;

    public DocsSearchPresenter(int accountId, @Nullable DocumentSearchCriteria criteria, @Nullable Bundle savedInstanceState) {
        super(accountId, criteria, savedInstanceState);
        docsInteractor = InteractorFactory.createDocsInteractor();
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
    Single<Pair<List<Document>, IntNextFrom>> doSearch(int accountId, DocumentSearchCriteria criteria, IntNextFrom startFrom) {
        int offset = startFrom.getOffset();
        IntNextFrom nextFrom = new IntNextFrom(50 + offset);
        return docsInteractor.search(accountId, criteria, 50, offset)
                .map(documents -> Pair.Companion.create(documents, nextFrom));
    }

    @Override
    DocumentSearchCriteria instantiateEmptyCriteria() {
        return new DocumentSearchCriteria("");
    }

    @Override
    boolean canSearch(DocumentSearchCriteria criteria) {
        return Utils.nonEmpty(criteria.getQuery());
    }
}