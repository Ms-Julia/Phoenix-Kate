package biz.dealnote.messenger.api;

import com.google.gson.Gson;

import biz.dealnote.messenger.Account_Types;
import biz.dealnote.messenger.settings.Settings;


class CustomTokenVkApiInterceptor extends AbsVkApiInterceptor {

    private final String token;

    private final @Account_Types
    int type;

    private final Integer account_id;

    CustomTokenVkApiInterceptor(String token, String v, Gson gson, @Account_Types int type, Integer account_id) {
        super(v, gson);
        this.token = token;
        this.type = type;
        this.account_id = account_id;
    }

    @Override
    protected String getToken() {
        return token;
    }

    @Override
    protected @Account_Types
    int getType() {
        if (type == Account_Types.BY_TYPE && account_id == null) {
            return Account_Types.KATE;
        } else if (type == Account_Types.BY_TYPE) {
            return Settings.get().accounts().getType(account_id);
        }
        return type;
    }

    @Override
    protected int getAccountId() {
        return account_id;
    }
}