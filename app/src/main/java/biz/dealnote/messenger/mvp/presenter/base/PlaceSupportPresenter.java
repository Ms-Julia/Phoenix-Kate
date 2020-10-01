package biz.dealnote.messenger.mvp.presenter.base;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import biz.dealnote.messenger.domain.ILikesInteractor;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.fragment.search.SearchContentType;
import biz.dealnote.messenger.fragment.search.criteria.NewsFeedCriteria;
import biz.dealnote.messenger.model.Article;
import biz.dealnote.messenger.model.Audio;
import biz.dealnote.messenger.model.AudioPlaylist;
import biz.dealnote.messenger.model.Commented;
import biz.dealnote.messenger.model.Document;
import biz.dealnote.messenger.model.Link;
import biz.dealnote.messenger.model.Message;
import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.model.PhotoAlbum;
import biz.dealnote.messenger.model.Poll;
import biz.dealnote.messenger.model.Post;
import biz.dealnote.messenger.model.Story;
import biz.dealnote.messenger.model.Video;
import biz.dealnote.messenger.model.WikiPage;
import biz.dealnote.messenger.mvp.view.IAttachmentsPlacesView;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.mvp.core.IMvpView;

public abstract class PlaceSupportPresenter<V extends IMvpView & IAttachmentsPlacesView & IAccountDependencyView>
        extends AccountDependencyPresenter<V> {

    public PlaceSupportPresenter(int accountId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
    }

    public void fireLinkClick(@NonNull Link link) {
        getView().openLink(getAccountId(), link);
    }

    public void fireUrlClick(@NonNull String url) {
        getView().openUrl(getAccountId(), url);
    }

    public void fireWikiPageClick(@NonNull WikiPage page) {
        getView().openWikiPage(getAccountId(), page);
    }

    public void fireStoryClick(@NonNull Story story) {
        getView().openStory(getAccountId(), story);
    }

    public void firePhotoClick(@NonNull ArrayList<Photo> photos, int index, boolean refresh) {
        getView().openSimplePhotoGallery(getAccountId(), photos, index, refresh);
    }

    public void firePostClick(@NonNull Post post) {
        getView().openPost(getAccountId(), post);
    }

    public void fireDocClick(@NonNull Document document) {
        getView().openDocPreview(getAccountId(), document);
    }

    public void fireOwnerClick(int ownerId) {
        getView().openOwnerWall(getAccountId(), ownerId);
    }

    public void fireGoToMessagesLookup(@NonNull Message message) {
        getView().goToMessagesLookupFWD(getAccountId(), message.getPeerId(), message.getId());
    }

    public void fireForwardMessagesClick(@NonNull ArrayList<Message> messages) {
        getView().openForwardMessages(getAccountId(), messages);
    }

    public void fireAudioPlayClick(int position, @NonNull ArrayList<Audio> apiAudio) {
        getView().playAudioList(getAccountId(), position, apiAudio);
    }

    public void fireVideoClick(@NonNull Video apiVideo) {
        getView().openVideo(getAccountId(), apiVideo);
    }

    public void fireAudioPlaylistClick(@NotNull AudioPlaylist playlist) {
        getView().openAudioPlaylist(getAccountId(), playlist);
    }

    public void firePollClick(@NonNull Poll poll) {
        getView().openPoll(getAccountId(), poll);
    }

    public void fireHashtagClick(String hashTag) {
        getView().openSearch(getAccountId(), SearchContentType.NEWS, new NewsFeedCriteria(hashTag));
    }

    public void fireShareClick(Post post) {
        getView().repostPost(getAccountId(), post);
    }

    public void fireCommentsClick(Post post) {
        getView().openComments(getAccountId(), Commented.from(post), null);
    }

    public void firePhotoAlbumClick(@NotNull PhotoAlbum album) {
        getView().openPhotoAlbum(getAccountId(), album);
    }

    public void fireFaveArticleClick(@NotNull Article article) {
        if (!article.getIsFavorite()) {
            appendDisposable(InteractorFactory.createFaveInteractor().addArticle(getAccountId(), article.getURL())
                    .compose(RxUtils.applyCompletableIOToMainSchedulers())
                    .subscribe(RxUtils.dummy(), t -> {
                    }));
        } else {
            appendDisposable(InteractorFactory.createFaveInteractor().removeArticle(getAccountId(), article.getOwnerId(), article.getId())
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(i -> {
                    }, t -> {
                    }));
        }
    }

    public final void fireCopiesLikesClick(String type, int ownerId, int itemId, String filter) {
        if (ILikesInteractor.FILTER_LIKES.equals(filter)) {
            getView().goToLikes(getAccountId(), type, ownerId, itemId);
        } else if (ILikesInteractor.FILTER_COPIES.equals(filter)) {
            getView().goToReposts(getAccountId(), type, ownerId, itemId);
        }
    }
}