package biz.dealnote.messenger.api;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Random;

import biz.dealnote.messenger.Account_Types;
import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.activity.ValidateActivity;
import biz.dealnote.messenger.api.model.Captcha;
import biz.dealnote.messenger.api.model.Error;
import biz.dealnote.messenger.api.model.response.VkReponse;
import biz.dealnote.messenger.exception.UnauthorizedException;
import biz.dealnote.messenger.service.ApiErrorCodes;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.PersistentLogger;
import biz.dealnote.messenger.util.Utils;
import io.reactivex.rxjava3.core.Completable;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static biz.dealnote.messenger.util.Objects.isNull;
import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.isEmpty;


abstract class AbsVkApiInterceptor implements Interceptor {

    private static final Random RANDOM = new Random();
    private final String version;
    private final Gson gson;

    AbsVkApiInterceptor(String version, Gson gson) {
        this.version = version;
        this.gson = gson;
    }

    private static String getIdToken() {
        return "d4gdb0joSiM:APA91bFAM-gVwLCkCABy5DJPPRH5TNDHW9xcGu_OLhmdUSA8zuUsBiU_DexHrTLLZWtzWHZTT5QUaVkBk_GJVQyCE_yQj9UId3pU3vxvizffCPQISmh2k93Fs7XH1qPbDvezEiMyeuLDXb5ebOVGehtbdk_9u5pwUw";
    }

    protected abstract String getToken();

    protected abstract @Account_Types
    int getType();

    /*
    private String getInstanceIdToken() {
        try {
            GoogleApiAvailability instance = GoogleApiAvailability.getInstance();
            int isGooglePlayServicesAvailable = instance.isGooglePlayServicesAvailable(Injection.provideApplicationContext());
            if (isGooglePlayServicesAvailable != 0) {
                return null;
            }
            return FirebaseInstanceId.getInstance().getToken("54740537194", "id" + getAccountId());
        } catch (Throwable th) {
            th.printStackTrace();
            return null;
        }
    }
     */

    protected abstract int getAccountId();

    private boolean upgradeToken() {
        String fcm = getIdToken();
        String oldToken = getToken();
        String token = Injection.provideNetworkInterfaces().vkDefault(getAccountId()).account().refreshToken(fcm).blockingGet().token;
        Log.w("refresh", oldToken + " " + token + " " + fcm);
        if (oldToken.equals(token) || isEmpty(token)) {
            return false;
        }
        Settings.get().accounts().storeAccessToken(getAccountId(), token);
        return true;
    }

    @SuppressLint("CheckResult")
    private void startValidateActivity(Context context, String url) {
        Completable.complete()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(() -> {
                    Intent intent = ValidateActivity.createIntent(context, url);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                });
    }

    @NotNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();

        String token = getToken();

        if (isEmpty(token)) {
            throw new UnauthorizedException("No authorization! Please, login and retry");
        }

        FormBody.Builder formBuiler = new FormBody.Builder();

        RequestBody body = original.body();

        boolean HasVersion = false;
        boolean HasDeviceId = false;
        if (body instanceof FormBody) {
            FormBody formBody = (FormBody) body;
            for (int i = 0; i < formBody.size(); i++) {
                String name = formBody.name(i);
                if (name.equals("v"))
                    HasVersion = true;
                else if (name.equals("device_id"))
                    HasDeviceId = true;
                String value = formBody.value(i);
                formBuiler.add(name, value);
            }
        }
        if (!HasVersion)
            formBuiler.add("v", version);

        formBuiler.add("access_token", token)
                .add("lang", Constants.DEVICE_COUNTRY_CODE)
                .add("https", "1");
        if (!HasDeviceId)
            formBuiler.add("device_id", Utils.getDiviceId(Injection.provideApplicationContext()));

        Request request = original.newBuilder()
                .method("POST", formBuiler.build())
                .build();

        Response response;
        ResponseBody responseBody;
        String responseBodyString;

        while (true) {
            response = chain.proceed(request);
            responseBody = response.body();
            assert responseBody != null;
            responseBodyString = responseBody.string();

            VkReponse vkReponse;
            try {
                vkReponse = gson.fromJson(responseBodyString, VkReponse.class);
            } catch (JsonSyntaxException ignored) {
                responseBodyString = "{ \"error\": { \"error_code\": -1, \"error_msg\": \"Internal json syntax error\" } }";
                return response.newBuilder().body(ResponseBody.create(responseBodyString, responseBody.contentType())).build();
            }

            Error error = isNull(vkReponse) ? null : vkReponse.error;

            if (nonNull(error)) {
                switch (error.errorCode) {
                    case ApiErrorCodes.TOO_MANY_REQUESTS_PER_SECOND:
                        break;
                    case ApiErrorCodes.CAPTCHA_NEED:
                        if (Settings.get().other().isDeveloper_mode()) {
                            PersistentLogger.logThrowable("Captcha request", new Exception("URL: " + request.url() + ", dump: " + new Gson().toJson(error)));
                        }
                        break;
                    default:
                        //FirebaseCrash.log("ApiError, method: " + error.method + ", code: " + error.errorCode + ", message: " + error.errorMsg);
                        break;
                }

                if (error.errorCode == ApiErrorCodes.TOO_MANY_REQUESTS_PER_SECOND) {
                    synchronized (AbsVkApiInterceptor.class) {
                        int sleepMs = 1000 + RANDOM.nextInt(500);
                        SystemClock.sleep(sleepMs);
                    }

                    continue;
                }

                if (error.errorCode == ApiErrorCodes.REFRESH_TOKEN) {
                    if (upgradeToken()) {
                        token = getToken();
                        formBuiler.add("access_token", token);

                        request = original.newBuilder()
                                .method("POST", formBuiler.build())
                                .build();
                        continue;
                    }
                }

                if (error.errorCode == ApiErrorCodes.VALIDATE_NEED) {
                    startValidateActivity(Injection.provideApplicationContext(), error.redirectUri);
                }

                if (error.errorCode == ApiErrorCodes.CAPTCHA_NEED) {
                    Captcha captcha = new Captcha(error.captchaSid, error.captchaImg);

                    ICaptchaProvider provider = Injection.provideCaptchaProvider();
                    provider.requestCaptha(captcha.getSid(), captcha);

                    String code = null;

                    while (true) {
                        try {
                            code = provider.lookupCode(captcha.getSid());

                            if (nonNull(code)) {
                                break;
                            } else {
                                SystemClock.sleep(1000);
                            }
                        } catch (OutOfDateException e) {
                            break;
                        }
                    }
                    if (Settings.get().other().isDeveloper_mode()) {
                        PersistentLogger.logThrowable("Captcha answer", new Exception("URL: " + request.url() + ", code: " + code + ", sid: " + captcha.getSid()));
                    }
                    if (nonNull(code)) {
                        formBuiler.add("captcha_sid", captcha.getSid());
                        formBuiler.add("captcha_key", code);

                        request = original.newBuilder()
                                .method("POST", formBuiler.build())
                                .build();
                        continue;
                    }
                }
            }
            break;
        }
        return response.newBuilder().body(ResponseBody.create(responseBodyString, responseBody.contentType())).build();
    }
}
