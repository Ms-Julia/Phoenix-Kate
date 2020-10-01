package biz.dealnote.messenger.api.interfaces;

import biz.dealnote.messenger.api.model.LoginResponse;
import biz.dealnote.messenger.api.model.VkApiValidationResponce;
import io.reactivex.rxjava3.core.Single;


public interface IAuthApi {
    Single<LoginResponse> directLogin(String grantType, int clientId, String clientSecret,
                                      String username, String pass, String v, boolean twoFaSupported,
                                      String scope, String code, String captchaSid, String captchaKey, boolean forceSms);

    Single<VkApiValidationResponce> validatePhone(int apiId, int clientId, String clientSecret, String sid, String v);
}