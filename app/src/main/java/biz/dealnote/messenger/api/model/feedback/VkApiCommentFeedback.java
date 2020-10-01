package biz.dealnote.messenger.api.model.feedback;

import biz.dealnote.messenger.api.model.Commentable;
import biz.dealnote.messenger.api.model.VKApiComment;

public class VkApiCommentFeedback extends VkApiBaseFeedback {
    public Commentable comment_of;
    public VKApiComment comment;
}