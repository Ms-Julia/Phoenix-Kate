package biz.dealnote.messenger.domain.mappers;

import java.util.Collection;
import java.util.List;

import biz.dealnote.messenger.api.model.AttachmentsTokenCreator;
import biz.dealnote.messenger.api.model.IAttachmentToken;
import biz.dealnote.messenger.model.AbsModel;
import biz.dealnote.messenger.model.Article;
import biz.dealnote.messenger.model.Audio;
import biz.dealnote.messenger.model.AudioPlaylist;
import biz.dealnote.messenger.model.Call;
import biz.dealnote.messenger.model.Document;
import biz.dealnote.messenger.model.Graffiti;
import biz.dealnote.messenger.model.Link;
import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.model.PhotoAlbum;
import biz.dealnote.messenger.model.Poll;
import biz.dealnote.messenger.model.Post;
import biz.dealnote.messenger.model.Story;
import biz.dealnote.messenger.model.Video;
import biz.dealnote.messenger.util.Utils;

public class Model2Dto {

    /*public static List<IAttachmentToken> createTokens(Attachments attachments){
        List<IAttachmentToken> tokens = new ArrayList<>(nonNull(attachments) ? attachments.size() : 0);

        if(nonNull(attachments)){
            tokens.addAll(createTokens(attachments.getAudios()));
            tokens.addAll(createTokens(attachments.getPhotos()));
            tokens.addAll(createTokens(attachments.getDocs()));
            tokens.addAll(createTokens(attachments.getVideos()));
            tokens.addAll(createTokens(attachments.getPosts()));
            tokens.addAll(createTokens(attachments.getLinks()));
            tokens.addAll(createTokens(attachments.getPolls()));
            tokens.addAll(createTokens(attachments.getPages()));
            tokens.addAll(createTokens(attachments.getVoiceMessages()));
        }

        return tokens;
    }*/

    public static List<IAttachmentToken> createTokens(Collection<? extends AbsModel> models) {
        return MapUtil.mapAll(models, Model2Dto::createToken);
    }

    public static IAttachmentToken createToken(AbsModel model) {
        if (model instanceof Document) {
            Document document = (Document) model;
            return AttachmentsTokenCreator.ofDocument(document.getId(), document.getOwnerId(), document.getAccessKey());
        }

        if (model instanceof Audio) {
            Audio audio = (Audio) model;
            return AttachmentsTokenCreator.ofAudio(audio.getId(), audio.getOwnerId(), audio.getAccessKey());
        }

        if (model instanceof Link) {
            return AttachmentsTokenCreator.ofLink(((Link) model).getUrl());
        }

        if (model instanceof Photo) {
            Photo photo = (Photo) model;
            return AttachmentsTokenCreator.ofPhoto(photo.getId(), photo.getOwnerId(), photo.getAccessKey());
        }

        if (model instanceof Poll) {
            Poll poll = (Poll) model;
            return AttachmentsTokenCreator.ofPoll(poll.getId(), poll.getOwnerId());
        }

        if (model instanceof Post) {
            Post post = (Post) model;
            return AttachmentsTokenCreator.ofPost(post.getVkid(), post.getOwnerId());
        }

        if (model instanceof Video) {
            Video video = (Video) model;
            return AttachmentsTokenCreator.ofVideo(video.getId(), video.getOwnerId(), video.getAccessKey());
        }

        if (model instanceof Article) {
            Article article = (Article) model;
            return AttachmentsTokenCreator.ofArticle(article.getId(), article.getOwnerId(), article.getAccessKey());
        }

        if (model instanceof Story) {
            Story story = (Story) model;
            return AttachmentsTokenCreator.ofStory(story.getId(), story.getOwnerId(), story.getAccessKey());
        }

        if (model instanceof Graffiti) {
            Graffiti graffity = (Graffiti) model;
            return AttachmentsTokenCreator.ofStory(graffity.getId(), graffity.getOwner_id(), graffity.getAccess_key());
        }

        if (model instanceof PhotoAlbum) {
            PhotoAlbum album = (PhotoAlbum) model;
            return AttachmentsTokenCreator.ofPhotoAlbum(album.getId(), album.getOwnerId());
        }

        if (model instanceof Call) {
            Call call = (Call) model;
            return AttachmentsTokenCreator.ofCall(call.getInitiator_id(), call.getReceiver_id(), call.getState(), call.getTime());
        }

        if (model instanceof AudioPlaylist) {
            AudioPlaylist playlist = (AudioPlaylist) model;
            if (Utils.isEmpty(playlist.getOriginal_access_key()) || playlist.getOriginal_id() == 0 || playlist.getOriginal_owner_id() == 0) {
                return AttachmentsTokenCreator.ofAudioPlaylist(playlist.getId(), playlist.getOwnerId(), playlist.getAccess_key());
            } else {
                return AttachmentsTokenCreator.ofAudioPlaylist(playlist.getOriginal_id(), playlist.getOriginal_owner_id(), playlist.getOriginal_access_key());
            }
        }

        throw new UnsupportedOperationException("Token for class " + model.getClass() + " not supported");
    }
}