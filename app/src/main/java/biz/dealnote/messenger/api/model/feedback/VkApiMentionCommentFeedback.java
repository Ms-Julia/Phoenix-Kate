package biz.dealnote.messenger.api.model.feedback;

import biz.dealnote.messenger.api.model.Commentable;
import biz.dealnote.messenger.api.model.VKApiComment;

public class VkApiMentionCommentFeedback extends VkApiBaseFeedback {

    public VKApiComment where;
    public Commentable comment_of;

}
