package biz.dealnote.messenger.db.model.entity.feedback;

import biz.dealnote.messenger.db.model.entity.CommentEntity;
import biz.dealnote.messenger.db.model.entity.Entity;
import biz.dealnote.messenger.db.model.entity.EntityWrapper;

public class NewCommentEntity extends FeedbackEntity {

    private EntityWrapper commented = EntityWrapper.empty();

    private CommentEntity comment;

    public NewCommentEntity(int type) {
        super(type);
    }

    public CommentEntity getComment() {
        return comment;
    }

    public NewCommentEntity setComment(CommentEntity comment) {
        this.comment = comment;
        return this;
    }

    public Entity getCommented() {
        return commented.get();
    }

    public NewCommentEntity setCommented(Entity commented) {
        this.commented = new EntityWrapper(commented);
        return this;
    }
}