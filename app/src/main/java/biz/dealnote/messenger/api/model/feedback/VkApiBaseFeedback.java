package biz.dealnote.messenger.api.model.feedback;

import biz.dealnote.messenger.api.model.VKApiComment;

public abstract class VkApiBaseFeedback {

    public String type;
    public long date;

    public VKApiComment reply;


}
