package biz.dealnote.messenger.api;

import android.annotation.SuppressLint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import biz.dealnote.messenger.api.adapters.ArticleDtoAdapter;
import biz.dealnote.messenger.api.adapters.AttachmentsDtoAdapter;
import biz.dealnote.messenger.api.adapters.AttachmentsEntryDtoAdapter;
import biz.dealnote.messenger.api.adapters.AudioDtoAdapter;
import biz.dealnote.messenger.api.adapters.AudioPlaylistDtoAdapter;
import biz.dealnote.messenger.api.adapters.BooleanAdapter;
import biz.dealnote.messenger.api.adapters.ChatDtoAdapter;
import biz.dealnote.messenger.api.adapters.ChatUserDtoAdapter;
import biz.dealnote.messenger.api.adapters.ChatsInfoAdapter;
import biz.dealnote.messenger.api.adapters.CommentDtoAdapter;
import biz.dealnote.messenger.api.adapters.CommunityDtoAdapter;
import biz.dealnote.messenger.api.adapters.CustomCommentsResponseAdapter;
import biz.dealnote.messenger.api.adapters.DocsEntryDtoAdapter;
import biz.dealnote.messenger.api.adapters.FaveLinkDtoAdapter;
import biz.dealnote.messenger.api.adapters.FeedbackDtoAdapter;
import biz.dealnote.messenger.api.adapters.FeedbackUserArrayDtoAdapter;
import biz.dealnote.messenger.api.adapters.GroupSettingsAdapter;
import biz.dealnote.messenger.api.adapters.JsonStringDtoAdapter;
import biz.dealnote.messenger.api.adapters.LikesListAdapter;
import biz.dealnote.messenger.api.adapters.LongpollUpdateAdapter;
import biz.dealnote.messenger.api.adapters.MessageDtoAdapter;
import biz.dealnote.messenger.api.adapters.NewsAdapter;
import biz.dealnote.messenger.api.adapters.NewsfeedCommentDtoAdapter;
import biz.dealnote.messenger.api.adapters.PhotoAlbumDtoAdapter;
import biz.dealnote.messenger.api.adapters.PhotoDtoAdapter;
import biz.dealnote.messenger.api.adapters.PostDtoAdapter;
import biz.dealnote.messenger.api.adapters.PostSourceDtoAdapter;
import biz.dealnote.messenger.api.adapters.PrivacyDtoAdapter;
import biz.dealnote.messenger.api.adapters.ProfileInfoResponceDtoAdapter;
import biz.dealnote.messenger.api.adapters.SchoolClazzDtoAdapter;
import biz.dealnote.messenger.api.adapters.SearchDialogsResponseAdapter;
import biz.dealnote.messenger.api.adapters.StoryDtoAdapter;
import biz.dealnote.messenger.api.adapters.TopicDtoAdapter;
import biz.dealnote.messenger.api.adapters.UserDtoAdapter;
import biz.dealnote.messenger.api.adapters.VKApiCatalogLinkDtoAdapter;
import biz.dealnote.messenger.api.adapters.VideoAlbumDtoAdapter;
import biz.dealnote.messenger.api.adapters.VideoDtoAdapter;
import biz.dealnote.messenger.api.adapters.local_json.ChatJsonResponseDtoAdapter;
import biz.dealnote.messenger.api.model.ChatUserDto;
import biz.dealnote.messenger.api.model.FaveLinkDto;
import biz.dealnote.messenger.api.model.GroupSettingsDto;
import biz.dealnote.messenger.api.model.VKApiArticle;
import biz.dealnote.messenger.api.model.VKApiAudio;
import biz.dealnote.messenger.api.model.VKApiAudioPlaylist;
import biz.dealnote.messenger.api.model.VKApiCatalogLink;
import biz.dealnote.messenger.api.model.VKApiChat;
import biz.dealnote.messenger.api.model.VKApiComment;
import biz.dealnote.messenger.api.model.VKApiCommunity;
import biz.dealnote.messenger.api.model.VKApiMessage;
import biz.dealnote.messenger.api.model.VKApiNews;
import biz.dealnote.messenger.api.model.VKApiPhoto;
import biz.dealnote.messenger.api.model.VKApiPhotoAlbum;
import biz.dealnote.messenger.api.model.VKApiPost;
import biz.dealnote.messenger.api.model.VKApiStory;
import biz.dealnote.messenger.api.model.VKApiTopic;
import biz.dealnote.messenger.api.model.VKApiUser;
import biz.dealnote.messenger.api.model.VKApiVideo;
import biz.dealnote.messenger.api.model.VKApiVideoAlbum;
import biz.dealnote.messenger.api.model.VkApiAttachments;
import biz.dealnote.messenger.api.model.VkApiDoc;
import biz.dealnote.messenger.api.model.VkApiJsonString;
import biz.dealnote.messenger.api.model.VkApiPostSource;
import biz.dealnote.messenger.api.model.VkApiPrivacy;
import biz.dealnote.messenger.api.model.VkApiProfileInfoResponce;
import biz.dealnote.messenger.api.model.database.SchoolClazzDto;
import biz.dealnote.messenger.api.model.feedback.UserArray;
import biz.dealnote.messenger.api.model.feedback.VkApiBaseFeedback;
import biz.dealnote.messenger.api.model.local_json.ChatJsonResponse;
import biz.dealnote.messenger.api.model.longpoll.AbsLongpollEvent;
import biz.dealnote.messenger.api.model.response.ChatsInfoResponse;
import biz.dealnote.messenger.api.model.response.CustomCommentsResponse;
import biz.dealnote.messenger.api.model.response.LikesListResponse;
import biz.dealnote.messenger.api.model.response.NewsfeedCommentsResponse;
import biz.dealnote.messenger.api.model.response.SearchDialogsResponse;
import biz.dealnote.messenger.settings.IProxySettings;
import biz.dealnote.messenger.settings.Settings;
import io.reactivex.rxjava3.core.Single;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static biz.dealnote.messenger.util.Objects.isNull;
import static biz.dealnote.messenger.util.Objects.nonNull;

public class VkRetrofitProvider implements IVkRetrofitProvider {

    private static final Gson VKGSON = new GsonBuilder()
            .registerTypeAdapter(VkApiAttachments.Entry.class, new AttachmentsEntryDtoAdapter())
            .registerTypeAdapter(VkApiDoc.Entry.class, new DocsEntryDtoAdapter())
            .registerTypeAdapter(VKApiPhoto.class, new PhotoDtoAdapter())
            .registerTypeAdapter(boolean.class, new BooleanAdapter())
            .registerTypeAdapter(VkApiPrivacy.class, new PrivacyDtoAdapter())
            .registerTypeAdapter(VKApiPhotoAlbum.class, new PhotoAlbumDtoAdapter())
            .registerTypeAdapter(VKApiVideoAlbum.class, new VideoAlbumDtoAdapter())
            .registerTypeAdapter(VkApiAttachments.class, new AttachmentsDtoAdapter())
            .registerTypeAdapter(VKApiAudio.class, new AudioDtoAdapter())
            .registerTypeAdapter(VKApiPost.class, new PostDtoAdapter())
            .registerTypeAdapter(VkApiPostSource.class, new PostSourceDtoAdapter())
            .registerTypeAdapter(VKApiUser.class, new UserDtoAdapter())
            .registerTypeAdapter(VKApiCommunity.class, new CommunityDtoAdapter())
            .registerTypeAdapter(VkApiBaseFeedback.class, new FeedbackDtoAdapter())
            .registerTypeAdapter(VKApiComment.class, new CommentDtoAdapter())
            .registerTypeAdapter(VKApiVideo.class, new VideoDtoAdapter())
            .registerTypeAdapter(UserArray.class, new FeedbackUserArrayDtoAdapter())
            .registerTypeAdapter(VKApiMessage.class, new MessageDtoAdapter())
            .registerTypeAdapter(VKApiNews.class, new NewsAdapter())
            .registerTypeAdapter(AbsLongpollEvent.class, new LongpollUpdateAdapter())
            .registerTypeAdapter(ChatsInfoResponse.class, new ChatsInfoAdapter())
            .registerTypeAdapter(VKApiChat.class, new ChatDtoAdapter())
            .registerTypeAdapter(ChatUserDto.class, new ChatUserDtoAdapter())
            .registerTypeAdapter(SchoolClazzDto.class, new SchoolClazzDtoAdapter())
            .registerTypeAdapter(LikesListResponse.class, new LikesListAdapter())
            .registerTypeAdapter(SearchDialogsResponse.class, new SearchDialogsResponseAdapter())
            .registerTypeAdapter(NewsfeedCommentsResponse.Dto.class, new NewsfeedCommentDtoAdapter())
            .registerTypeAdapter(VKApiTopic.class, new TopicDtoAdapter())
            .registerTypeAdapter(GroupSettingsDto.class, new GroupSettingsAdapter())
            .registerTypeAdapter(CustomCommentsResponse.class, new CustomCommentsResponseAdapter())
            .registerTypeAdapter(VKApiAudioPlaylist.class, new AudioPlaylistDtoAdapter())
            .registerTypeAdapter(VKApiStory.class, new StoryDtoAdapter())
            .registerTypeAdapter(FaveLinkDto.class, new FaveLinkDtoAdapter())
            .registerTypeAdapter(VKApiArticle.class, new ArticleDtoAdapter())
            .registerTypeAdapter(VKApiCatalogLink.class, new VKApiCatalogLinkDtoAdapter())
            .registerTypeAdapter(ChatJsonResponse.class, new ChatJsonResponseDtoAdapter())
            .registerTypeAdapter(VkApiJsonString.class, new JsonStringDtoAdapter())
            .registerTypeAdapter(VkApiProfileInfoResponce.class, new ProfileInfoResponceDtoAdapter())
            .create();

    private static final GsonConverterFactory GSON_CONVERTER_FACTORY = GsonConverterFactory.create(VKGSON);
    private static final RxJava3CallAdapterFactory RX_ADAPTER_FACTORY = RxJava3CallAdapterFactory.create();

    private final IProxySettings proxyManager;
    private final IVkMethodHttpClientFactory clientFactory;
    private final Object retrofitCacheLock = new Object();
    private final Object serviceRetrofitLock = new Object();
    @SuppressLint("UseSparseArrays")
    private final Map<Integer, RetrofitWrapper> retrofitCache = Collections.synchronizedMap(new HashMap<>(1));
    private volatile RetrofitWrapper serviceRetrofit;

    public VkRetrofitProvider(IProxySettings proxySettings, IVkMethodHttpClientFactory clientFactory) {
        proxyManager = proxySettings;
        this.clientFactory = clientFactory;
        proxyManager.observeActive()
                .subscribe(optional -> onProxySettingsChanged());
    }

    public static Gson getVkgson() {
        return VKGSON;
    }

    private void onProxySettingsChanged() {
        synchronized (retrofitCacheLock) {
            for (Map.Entry<Integer, RetrofitWrapper> entry : retrofitCache.entrySet()) {
                entry.getValue().cleanup();
            }

            retrofitCache.clear();
        }
    }

    @Override
    public Single<RetrofitWrapper> provideNormalRetrofit(int accountId) {
        return Single.fromCallable(() -> {
            RetrofitWrapper retrofit;

            synchronized (retrofitCacheLock) {
                retrofit = retrofitCache.get(accountId);

                if (nonNull(retrofit)) {
                    return retrofit;
                }

                OkHttpClient client = clientFactory.createDefaultVkHttpClient(accountId, VKGSON, proxyManager.getActiveProxy());
                retrofit = createDefaultVkApiRetrofit(client);
                retrofitCache.put(accountId, retrofit);
            }

            return retrofit;
        });
    }

    @Override
    public Single<RetrofitWrapper> provideCustomRetrofit(int accountId, String token) {
        return Single.fromCallable(() -> {
            OkHttpClient client = clientFactory.createCustomVkHttpClient(accountId, token, VKGSON, proxyManager.getActiveProxy());
            return createDefaultVkApiRetrofit(client);
        });
    }

    @Override
    public Single<RetrofitWrapper> provideServiceRetrofit() {
        return Single.fromCallable(() -> {
            if (isNull(serviceRetrofit)) {
                synchronized (serviceRetrofitLock) {
                    if (isNull(serviceRetrofit)) {
                        OkHttpClient client = clientFactory.createServiceVkHttpClient(VKGSON, proxyManager.getActiveProxy());
                        serviceRetrofit = createDefaultVkApiRetrofit(client);
                    }
                }
            }

            return serviceRetrofit;
        });
    }

    @Override
    public Single<OkHttpClient> provideNormalHttpClient(int accountId) {
        return Single.fromCallable(() -> clientFactory.createDefaultVkHttpClient(accountId, VKGSON, proxyManager.getActiveProxy()));
    }

    private RetrofitWrapper createDefaultVkApiRetrofit(OkHttpClient okHttpClient) {
        return RetrofitWrapper.wrap(new Retrofit.Builder()
                .baseUrl("https://" + Settings.get().other().get_Api_Domain() + "/method/")
                .addConverterFactory(GSON_CONVERTER_FACTORY)
                .addCallAdapterFactory(RX_ADAPTER_FACTORY)
                .client(okHttpClient)
                .build());
    }
}