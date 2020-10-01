package biz.dealnote.messenger.api.model.feedback;

import biz.dealnote.messenger.api.model.Commentable;
import biz.dealnote.messenger.api.model.VKApiComment;

public class VkApiLikeCommentFeedback extends VkApiBaseFeedback {

    public UserArray users;

    public VKApiComment comment;

    public Commentable commented;
}
