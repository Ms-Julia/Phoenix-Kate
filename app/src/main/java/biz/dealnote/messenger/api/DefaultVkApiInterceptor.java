package biz.dealnote.messenger.api;

import com.google.gson.Gson;

import biz.dealnote.messenger.Account_Types;
import biz.dealnote.messenger.settings.Settings;

public class DefaultVkApiInterceptor extends AbsVkApiInterceptor {

    private final int accountId;

    DefaultVkApiInterceptor(int accountId, String v, Gson gson) {
        super(v, gson);
        this.accountId = accountId;
    }

    @Override
    protected String getToken() {
        return Settings.get()
                .accounts()
                .getAccessToken(accountId);
    }

    @Override
    protected @Account_Types
    int getType() {
        return Settings.get()
                .accounts()
                .getType(accountId);
    }

    @Override
    protected int getAccountId() {
        return accountId;
    }
}