package biz.dealnote.messenger.db.model.entity.feedback;

import biz.dealnote.messenger.db.model.entity.CommentEntity;
import biz.dealnote.messenger.db.model.entity.Entity;
import biz.dealnote.messenger.db.model.entity.EntityWrapper;

public class LikeCommentEntity extends FeedbackEntity {

    private int[] likesOwnerIds;

    private EntityWrapper commented = EntityWrapper.empty();

    private CommentEntity liked;

    public LikeCommentEntity(int type) {
        super(type);
    }

    public CommentEntity getLiked() {
        return liked;
    }

    public LikeCommentEntity setLiked(CommentEntity liked) {
        this.liked = liked;
        return this;
    }

    public Entity getCommented() {
        return commented.get();
    }

    public LikeCommentEntity setCommented(Entity commented) {
        this.commented = new EntityWrapper(commented);
        return this;
    }

    public int[] getLikesOwnerIds() {
        return likesOwnerIds;
    }

    public LikeCommentEntity setLikesOwnerIds(int[] likesOwnerIds) {
        this.likesOwnerIds = likesOwnerIds;
        return this;
    }
}