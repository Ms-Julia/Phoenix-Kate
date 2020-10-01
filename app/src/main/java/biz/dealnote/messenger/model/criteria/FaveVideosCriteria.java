package biz.dealnote.messenger.model.criteria;

import biz.dealnote.messenger.db.DatabaseIdRange;

public class FaveVideosCriteria extends Criteria {

    private final int accountId;

    private DatabaseIdRange range;

    public FaveVideosCriteria(int accountId) {
        this.accountId = accountId;
    }

    public DatabaseIdRange getRange() {
        return range;
    }

    public FaveVideosCriteria setRange(DatabaseIdRange range) {
        this.range = range;
        return this;
    }

    public int getAccountId() {
        return accountId;
    }
}
