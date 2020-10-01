package biz.dealnote.messenger.api;

import android.annotation.SuppressLint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import biz.dealnote.messenger.Account_Types;
import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.api.adapters.LongpollUpdateAdapter;
import biz.dealnote.messenger.api.adapters.LongpollUpdatesAdapter;
import biz.dealnote.messenger.api.model.longpoll.AbsLongpollEvent;
import biz.dealnote.messenger.api.model.longpoll.VkApiLongpollUpdates;
import biz.dealnote.messenger.settings.IProxySettings;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.Objects;
import io.reactivex.rxjava3.core.Single;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static biz.dealnote.messenger.util.Objects.nonNull;


public class OtherVkRetrofitProvider implements IOtherVkRetrofitProvider {

    private final IProxySettings proxySettings;
    private final Object longpollRetrofitLock = new Object();
    private final Object amazonaudiocoverRetrofitLock = new Object();
    private RetrofitWrapper longpollRetrofitInstance;
    private RetrofitWrapper amazonaudiocoverRetrofitInstance;

    @SuppressLint("CheckResult")
    public OtherVkRetrofitProvider(IProxySettings proxySettings) {
        this.proxySettings = proxySettings;
        this.proxySettings.observeActive()
                .subscribe(ignored -> onProxySettingsChanged());
    }

    private void onProxySettingsChanged() {
        synchronized (longpollRetrofitLock) {
            if (nonNull(longpollRetrofitInstance)) {
                longpollRetrofitInstance.cleanup();
                longpollRetrofitInstance = null;
            }
        }
        synchronized (amazonaudiocoverRetrofitLock) {
            if (nonNull(amazonaudiocoverRetrofitInstance)) {
                amazonaudiocoverRetrofitInstance.cleanup();
                amazonaudiocoverRetrofitInstance = null;
            }
        }
    }

    @Override
    public Single<RetrofitWrapper> provideAuthRetrofit() {
        return Single.fromCallable(() -> {

            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .readTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(HttpLogger.DEFAULT_LOGGING_INTERCEPTOR).addInterceptor(chain -> {
                        Request request = chain.request().newBuilder().addHeader("User-Agent", Constants.USER_AGENT(Account_Types.KATE)).build();
                        return chain.proceed(request);
                    });

            ProxyUtil.applyProxyConfig(builder, proxySettings.getActiveProxy());
            Gson gson = new GsonBuilder().create();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://" + Settings.get().other().get_Auth_Domain() + "/")
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                    .client(builder.build())
                    .build();

            return RetrofitWrapper.wrap(retrofit, false);
        });
    }

    @Override
    public Single<RetrofitWrapper> provideAuthServiceRetrofit() {
        return Single.fromCallable(() -> {

            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .readTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(HttpLogger.DEFAULT_LOGGING_INTERCEPTOR).addInterceptor(chain -> {
                        Request request = chain.request().newBuilder().addHeader("User-Agent", Constants.USER_AGENT(Account_Types.BY_TYPE)).build();
                        return chain.proceed(request);
                    });

            ProxyUtil.applyProxyConfig(builder, proxySettings.getActiveProxy());
            Gson gson = new GsonBuilder().create();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://" + Settings.get().other().get_Api_Domain() + "/method/")
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                    .client(builder.build())
                    .build();

            return RetrofitWrapper.wrap(retrofit, false);
        });
    }

    private Retrofit createAmazonAudioCoverRetrofit() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(HttpLogger.DEFAULT_LOGGING_INTERCEPTOR).addInterceptor(chain -> {
                    Request request = chain.request().newBuilder().addHeader("User-Agent", Constants.USER_AGENT(Account_Types.BY_TYPE)).build();
                    return chain.proceed(request);
                });

        ProxyUtil.applyProxyConfig(builder, proxySettings.getActiveProxy());

        return new Retrofit.Builder()
                .baseUrl("https://axzodu785h.execute-api.us-east-1.amazonaws.com/")
                .addConverterFactory(GsonConverterFactory.create(new Gson()))
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .client(builder.build())
                .build();
    }

    private Retrofit createLongpollRetrofitInstance() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(HttpLogger.DEFAULT_LOGGING_INTERCEPTOR).addInterceptor(chain -> {
                    Request request = chain.request().newBuilder().addHeader("User-Agent", Constants.USER_AGENT(Account_Types.BY_TYPE)).build();
                    return chain.proceed(request);
                });

        ProxyUtil.applyProxyConfig(builder, proxySettings.getActiveProxy());

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(VkApiLongpollUpdates.class, new LongpollUpdatesAdapter())
                .registerTypeAdapter(AbsLongpollEvent.class, new LongpollUpdateAdapter())
                .create();

        return new Retrofit.Builder()
                .baseUrl("https://" + Settings.get().other().get_Api_Domain() + "/method/") // dummy
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .client(builder.build())
                .build();
    }

    @Override
    public Single<RetrofitWrapper> provideAmazonAudioCoverRetrofit() {
        return Single.fromCallable(() -> {

            if (Objects.isNull(amazonaudiocoverRetrofitInstance)) {
                synchronized (amazonaudiocoverRetrofitLock) {
                    if (Objects.isNull(amazonaudiocoverRetrofitInstance)) {
                        amazonaudiocoverRetrofitInstance = RetrofitWrapper.wrap(createAmazonAudioCoverRetrofit());
                    }
                }
            }

            return amazonaudiocoverRetrofitInstance;
        });
    }

    @Override
    public Single<RetrofitWrapper> provideLongpollRetrofit() {
        return Single.fromCallable(() -> {

            if (Objects.isNull(longpollRetrofitInstance)) {
                synchronized (longpollRetrofitLock) {
                    if (Objects.isNull(longpollRetrofitInstance)) {
                        longpollRetrofitInstance = RetrofitWrapper.wrap(createLongpollRetrofitInstance());
                    }
                }
            }

            return longpollRetrofitInstance;
        });
    }
}
