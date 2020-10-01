package biz.dealnote.messenger.link.types;

public class PollLink extends AbsLink {

    public int ownerId;
    public int Id;

    public PollLink(int ownerId, int Id) {
        super(POLL);
        this.Id = Id;
        this.ownerId = ownerId;
    }

    @Override
    public String toString() {
        return "PollLink{" +
                "ownerId=" + ownerId +
                ", Id=" + Id +
                '}';
    }
}
