package biz.dealnote.messenger.domain;

import java.util.Collection;
import java.util.List;

import biz.dealnote.messenger.fragment.search.criteria.NewsFeedCriteria;
import biz.dealnote.messenger.model.FeedList;
import biz.dealnote.messenger.model.News;
import biz.dealnote.messenger.model.Post;
import biz.dealnote.messenger.util.Pair;
import io.reactivex.rxjava3.core.Single;

public interface IFeedInteractor {
    Single<List<News>> getCachedFeed(int accountId);

    Single<Pair<List<News>, String>> getActualFeed(int accountId, int count, String nextFrom, String filters, Integer maxPhotos, String sourceIds);

    Single<Pair<List<Post>, String>> search(int accountId, NewsFeedCriteria criteria, int count, String startFrom);

    Single<List<FeedList>> getCachedFeedLists(int accountId);

    Single<List<FeedList>> getActualFeedLists(int accountId);

    Single<Integer> saveList(int accountId, String title, Collection<Integer> listIds);

    Single<Integer> deleteList(int accountId, Integer list_id);
}