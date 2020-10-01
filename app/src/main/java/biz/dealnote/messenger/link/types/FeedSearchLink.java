package biz.dealnote.messenger.link.types;

public class FeedSearchLink extends AbsLink {

    private final String q;

    public FeedSearchLink(String q) {
        super(FEED_SEARCH);
        this.q = q;
    }

    public String getQ() {
        return q;
    }
}