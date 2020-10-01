package biz.dealnote.messenger.db.model.entity.feedback;

import biz.dealnote.messenger.db.model.entity.Entity;
import biz.dealnote.messenger.db.model.entity.EntityWrapper;

public class LikeEntity extends FeedbackEntity {

    private int[] likesOwnerIds;

    private EntityWrapper liked = EntityWrapper.empty();

    public LikeEntity(int type) {
        super(type);
    }

    public int[] getLikesOwnerIds() {
        return likesOwnerIds;
    }

    public LikeEntity setLikesOwnerIds(int[] likesOwnerIds) {
        this.likesOwnerIds = likesOwnerIds;
        return this;
    }

    public Entity getLiked() {
        return liked.get();
    }

    public LikeEntity setLiked(Entity liked) {
        this.liked = new EntityWrapper(liked);
        return this;
    }
}