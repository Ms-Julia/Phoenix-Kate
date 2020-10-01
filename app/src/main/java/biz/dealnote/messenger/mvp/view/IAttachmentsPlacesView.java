package biz.dealnote.messenger.mvp.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import biz.dealnote.messenger.fragment.search.SearchContentType;
import biz.dealnote.messenger.fragment.search.criteria.BaseSearchCriteria;
import biz.dealnote.messenger.model.Audio;
import biz.dealnote.messenger.model.AudioPlaylist;
import biz.dealnote.messenger.model.Commented;
import biz.dealnote.messenger.model.Document;
import biz.dealnote.messenger.model.Link;
import biz.dealnote.messenger.model.Message;
import biz.dealnote.messenger.model.Peer;
import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.model.PhotoAlbum;
import biz.dealnote.messenger.model.Poll;
import biz.dealnote.messenger.model.Post;
import biz.dealnote.messenger.model.Story;
import biz.dealnote.messenger.model.Video;
import biz.dealnote.messenger.model.WikiPage;


public interface IAttachmentsPlacesView {

    void openChatWith(int accountId, int messagesOwnerId, @NonNull Peer peer);

    void openLink(int accountId, @NonNull Link link);

    void openUrl(int accountId, @NonNull String url);

    void openWikiPage(int accountId, @NonNull WikiPage page);

    void openSimplePhotoGallery(int accountId, @NonNull ArrayList<Photo> photos, int index, boolean needUpdate);

    void openPost(int accountId, @NonNull Post post);

    void goToMessagesLookupFWD(int accountId, int peerId, int messageId);

    void openDocPreview(int accountId, @NonNull Document document);

    void openOwnerWall(int accountId, int ownerId);

    void openForwardMessages(int accountId, @NonNull ArrayList<Message> messages);

    void playAudioList(int accountId, int position, @NonNull ArrayList<Audio> apiAudio);

    void openVideo(int accountId, @NonNull Video apiVideo);

    void openHistoryVideo(int accountId, @NonNull ArrayList<Story> stories, int index);

    void openPoll(int accoountId, @NonNull Poll apiPoll);

    void openSearch(int accountId, @SearchContentType int type, @Nullable BaseSearchCriteria criteria);

    void openComments(int accountId, Commented commented, Integer focusToCommentId);

    void goToLikes(int accountId, String type, int ownerId, int id);

    void goToReposts(int accountId, String type, int ownerId, int id);

    void repostPost(int accountId, @NonNull Post post);

    void openStory(int accountId, @NotNull Story story);

    void openAudioPlaylist(int accountId, @NotNull AudioPlaylist playlist);

    void openPhotoAlbum(int accountId, @NotNull PhotoAlbum album);
}
