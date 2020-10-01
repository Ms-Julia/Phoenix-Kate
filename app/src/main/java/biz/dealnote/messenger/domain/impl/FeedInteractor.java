package biz.dealnote.messenger.domain.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.api.interfaces.INetworker;
import biz.dealnote.messenger.api.model.VKApiNews;
import biz.dealnote.messenger.api.model.VKApiPost;
import biz.dealnote.messenger.api.model.VkApiFeedList;
import biz.dealnote.messenger.db.interfaces.IStorages;
import biz.dealnote.messenger.db.model.entity.FeedListEntity;
import biz.dealnote.messenger.db.model.entity.NewsEntity;
import biz.dealnote.messenger.db.model.entity.OwnerEntities;
import biz.dealnote.messenger.domain.IFeedInteractor;
import biz.dealnote.messenger.domain.IOwnersRepository;
import biz.dealnote.messenger.domain.mappers.Dto2Entity;
import biz.dealnote.messenger.domain.mappers.Dto2Model;
import biz.dealnote.messenger.domain.mappers.Entity2Model;
import biz.dealnote.messenger.fragment.search.criteria.NewsFeedCriteria;
import biz.dealnote.messenger.model.FeedList;
import biz.dealnote.messenger.model.FeedSourceCriteria;
import biz.dealnote.messenger.model.News;
import biz.dealnote.messenger.model.Owner;
import biz.dealnote.messenger.model.Post;
import biz.dealnote.messenger.model.criteria.FeedCriteria;
import biz.dealnote.messenger.settings.ISettings;
import biz.dealnote.messenger.util.Pair;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.messenger.util.VKOwnIds;
import io.reactivex.rxjava3.core.Single;

import static biz.dealnote.messenger.util.Utils.listEmptyIfNull;


public class FeedInteractor implements IFeedInteractor {

    private final INetworker networker;
    private final IStorages stores;
    private final IOwnersRepository ownersRepository;
    private final ISettings.IOtherSettings otherSettings;

    public FeedInteractor(INetworker networker, IStorages stores, ISettings.IOtherSettings otherSettings, IOwnersRepository ownersRepository) {
        this.networker = networker;
        this.stores = stores;
        this.otherSettings = otherSettings;
        this.ownersRepository = ownersRepository;
    }

    private static FeedList createFeedListFromEntity(FeedListEntity entity) {
        return new FeedList(entity.getId(), entity.getTitle());
    }

    @Override
    public Single<Pair<List<News>, String>> getActualFeed(int accountId, int count, String startFrom, String filters, Integer maxPhotos, String sourceIds) {

        if ("likes".equals(sourceIds)) {
            return networker.vkDefault(accountId)
                    .newsfeed()
                    .getFeedLikes(maxPhotos, startFrom, count, Constants.MAIN_OWNER_FIELDS)
                    .flatMap(response -> {
                        String nextFrom = response.nextFrom;
                        List<Owner> owners = Dto2Model.transformOwners(response.profiles, response.groups);
                        List<VKApiNews> feed = listEmptyIfNull(response.items);
                        List<NewsEntity> dbos = new ArrayList<>(feed.size());
                        VKOwnIds ownIds = new VKOwnIds();
                        for (VKApiNews news : feed) {
                            if (news.source_id == 0 || news.type.equals("ads")) {
                                continue;
                            }
                            dbos.add(Dto2Entity.mapNews(news));
                            ownIds.appendNews(news);
                        }
                        OwnerEntities ownerEntities = Dto2Entity.mapOwners(response.profiles, response.groups);
                        return stores.feed()
                                .store(accountId, dbos, ownerEntities, Utils.isEmpty(startFrom))
                                .flatMap(ints -> {
                                    otherSettings.storeFeedNextFrom(accountId, nextFrom);
                                    otherSettings.setFeedSourceIds(accountId, sourceIds);
                                    return ownersRepository.findBaseOwnersDataAsBundle(accountId, ownIds.getAll(), IOwnersRepository.MODE_ANY, owners)
                                            .map(owners1 -> {
                                                List<News> news = new ArrayList<>(feed.size());
                                                for (VKApiNews dto : feed) {
                                                    if (dto.source_id == 0 || dto.type.equals("ads")) {
                                                        continue;
                                                    }
                                                    news.add(Dto2Model.buildNews(dto, owners1));
                                                }
                                                return Pair.Companion.create(news, nextFrom);
                                            });
                                });
                    });
        } else if ("recommendation".equals(sourceIds)) {
            return networker.vkDefault(accountId)
                    .newsfeed()
                    .getRecommended(null, null, maxPhotos, startFrom, count, Constants.MAIN_OWNER_FIELDS)
                    .flatMap(response -> {
                        String nextFrom = response.nextFrom;
                        List<Owner> owners = Dto2Model.transformOwners(response.profiles, response.groups);
                        List<VKApiNews> feed = listEmptyIfNull(response.items);
                        List<NewsEntity> dbos = new ArrayList<>(feed.size());
                        VKOwnIds ownIds = new VKOwnIds();
                        for (VKApiNews news : feed) {
                            if (news.source_id == 0 || news.type.equals("ads")) {
                                continue;
                            }
                            dbos.add(Dto2Entity.mapNews(news));
                            ownIds.appendNews(news);
                        }
                        OwnerEntities ownerEntities = Dto2Entity.mapOwners(response.profiles, response.groups);
                        return stores.feed()
                                .store(accountId, dbos, ownerEntities, Utils.isEmpty(startFrom))
                                .flatMap(ints -> {
                                    otherSettings.storeFeedNextFrom(accountId, nextFrom);
                                    otherSettings.setFeedSourceIds(accountId, sourceIds);
                                    return ownersRepository.findBaseOwnersDataAsBundle(accountId, ownIds.getAll(), IOwnersRepository.MODE_ANY, owners)
                                            .map(owners1 -> {
                                                List<News> news = new ArrayList<>(feed.size());
                                                for (VKApiNews dto : feed) {
                                                    if (dto.source_id == 0 || dto.type.equals("ads")) {
                                                        continue;
                                                    }
                                                    news.add(Dto2Model.buildNews(dto, owners1));
                                                }
                                                return Pair.Companion.create(news, nextFrom);
                                            });
                                });
                    });
        } else {
            return networker.vkDefault(accountId)
                    .newsfeed()
                    .get(filters, null, null, null, maxPhotos, "updates".equals(sourceIds) ? null : sourceIds, startFrom, count, Constants.MAIN_OWNER_FIELDS)
                    .flatMap(response -> {
                        String nextFrom = response.nextFrom;
                        List<Owner> owners = Dto2Model.transformOwners(response.profiles, response.groups);
                        List<VKApiNews> feed = listEmptyIfNull(response.items);
                        List<NewsEntity> dbos = new ArrayList<>(feed.size());
                        VKOwnIds ownIds = new VKOwnIds();
                        for (VKApiNews news : feed) {
                            if (news.source_id == 0 || news.type.equals("ads")) {
                                continue;
                            }
                            dbos.add(Dto2Entity.mapNews(news));
                            ownIds.appendNews(news);
                        }
                        OwnerEntities ownerEntities = Dto2Entity.mapOwners(response.profiles, response.groups);
                        return stores.feed()
                                .store(accountId, dbos, ownerEntities, Utils.isEmpty(startFrom))
                                .flatMap(ints -> {
                                    otherSettings.storeFeedNextFrom(accountId, nextFrom);
                                    otherSettings.setFeedSourceIds(accountId, sourceIds);
                                    return ownersRepository.findBaseOwnersDataAsBundle(accountId, ownIds.getAll(), IOwnersRepository.MODE_ANY, owners)
                                            .map(owners1 -> {
                                                List<News> news = new ArrayList<>(feed.size());
                                                for (VKApiNews dto : feed) {
                                                    if (dto.source_id == 0 || dto.type.equals("ads")) {
                                                        continue;
                                                    }
                                                    news.add(Dto2Model.buildNews(dto, owners1));
                                                }
                                                return Pair.Companion.create(news, nextFrom);
                                            });
                                });
                    });
        }
    }

    @Override
    public Single<Pair<List<Post>, String>> search(int accountId, NewsFeedCriteria criteria, int count, String startFrom) {
        return networker.vkDefault(accountId)
                .newsfeed()
                .search(criteria.getQuery(), true, count, null, null, null, null, startFrom, Constants.MAIN_OWNER_FIELDS)
                .flatMap(response -> {
                    List<VKApiPost> dtos = listEmptyIfNull(response.items);
                    List<Owner> owners = Dto2Model.transformOwners(response.profiles, response.groups);

                    VKOwnIds ownIds = new VKOwnIds();
                    for (VKApiPost post : dtos) {
                        ownIds.append(post);
                    }

                    return ownersRepository.findBaseOwnersDataAsBundle(accountId, ownIds.getAll(), IOwnersRepository.MODE_ANY, owners)
                            .map(ownersBundle -> {
                                List<Post> posts = Dto2Model.transformPosts(dtos, ownersBundle);
                                return Pair.Companion.create(posts, response.nextFrom);
                            });
                });
    }

    @Override
    public Single<Integer> saveList(int accountId, String title, Collection<Integer> listIds) {
        return networker.vkDefault(accountId)
                .newsfeed().saveList(title, listIds).map(response -> response);
    }

    @Override
    public Single<Integer> deleteList(int accountId, Integer list_id) {
        return networker.vkDefault(accountId)
                .newsfeed().deleteList(list_id).map(response -> response);
    }

    @Override
    public Single<List<FeedList>> getCachedFeedLists(int accountId) {
        FeedSourceCriteria criteria = new FeedSourceCriteria(accountId);
        return stores.feed()
                .getAllLists(criteria)
                .map(entities -> {
                    List<FeedList> lists = new ArrayList<>(entities.size());
                    for (FeedListEntity entity : entities) {
                        lists.add(createFeedListFromEntity(entity));
                    }
                    return lists;
                });
    }

    @Override
    public Single<List<FeedList>> getActualFeedLists(int accountId) {
        return networker.vkDefault(accountId)
                .newsfeed()
                .getLists(null)
                .map(items -> listEmptyIfNull(items.getItems()))
                .flatMap(dtos -> {
                    List<FeedListEntity> entities = new ArrayList<>(dtos.size());
                    List<FeedList> lists = new ArrayList<>();

                    for (VkApiFeedList dto : dtos) {
                        FeedListEntity entity = new FeedListEntity(dto.id)
                                .setTitle(dto.title)
                                .setNoReposts(dto.no_reposts)
                                .setSourceIds(dto.source_ids);
                        entities.add(entity);
                        lists.add(createFeedListFromEntity(entity));
                    }

                    return stores.feed()
                            .storeLists(accountId, entities)
                            .andThen(Single.just(lists));
                });
    }

    @Override
    public Single<List<News>> getCachedFeed(int accountId) {
        FeedCriteria criteria = new FeedCriteria(accountId);

        return stores.feed()
                .findByCriteria(criteria)
                .flatMap(dbos -> {
                    VKOwnIds ownIds = new VKOwnIds();
                    for (NewsEntity dbo : dbos) {
                        Entity2Model.fillOwnerIds(ownIds, dbo);
                    }

                    return ownersRepository.findBaseOwnersDataAsBundle(accountId, ownIds.getAll(), IOwnersRepository.MODE_ANY)
                            .map(owners -> {
                                List<News> news = new ArrayList<>(dbos.size());
                                for (NewsEntity dbo : dbos) {
                                    news.add(Entity2Model.buildNewsFromDbo(dbo, owners));
                                }

                                return news;
                            });
                });
    }
}