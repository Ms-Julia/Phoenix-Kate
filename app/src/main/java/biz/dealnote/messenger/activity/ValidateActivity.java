package biz.dealnote.messenger.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import biz.dealnote.messenger.Account_Types;
import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.api.Auth;
import biz.dealnote.messenger.api.util.VKStringUtils;
import biz.dealnote.messenger.model.Token;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.Logger;

import static biz.dealnote.messenger.util.Utils.nonEmpty;

public class ValidateActivity extends Activity {

    private static final String TAG = ValidateActivity.class.getSimpleName();
    private static final String EXTRA_VALIDATE = "validate";

    public static Intent createIntent(Context context, String validate_url) {
        return new Intent(context, LoginActivity.class)
                .putExtra(EXTRA_VALIDATE, validate_url);
    }

    private static String tryExtractAccessToken(String url) {
        return VKStringUtils.extractPattern(url, "access_token=(.*?)&");
    }

    private static ArrayList<Token> tryExtractAccessTokens(String url) throws Exception {
        Pattern p = Pattern.compile("access_token_(\\d*)=(.*?)&");

        ArrayList<Token> tokens = new ArrayList<>();

        Matcher matcher = p.matcher(url);
        while (matcher.find()) {
            String groupid = matcher.group(1);
            String token = matcher.group(2);

            if (nonEmpty(groupid) && nonEmpty(token)) {
                tokens.add(new Token(-Integer.parseInt(groupid), token));
            }
        }

        if (tokens.isEmpty()) {
            throw new Exception("Failed to parse redirect url " + url);
        }

        return tokens;
    }

    private static String tryExtractUserId(String url) {
        return VKStringUtils.extractPattern(url, "user_id=(\\d*)");
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(Settings.get().ui().getMainTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        WebView webview = findViewById(R.id.vkontakteview);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.clearCache(true);
        webview.getSettings().setUserAgentString(Constants.USER_AGENT(Account_Types.KATE));

        //Чтобы получать уведомления об окончании загрузки страницы
        webview.setWebViewClient(new VkontakteWebViewClient());

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookies(aBoolean -> Log.d(TAG, "Cookie removed: " + aBoolean));

        webview.loadUrl(getIntent().getStringExtra(EXTRA_VALIDATE));
    }

    private void parseUrl(String url) {
        try {
            if (url == null) {
                return;
            }

            Logger.d(TAG, "url=" + url);

            if (url.startsWith(Auth.redirect_url)) {
                if (!url.contains("error=")) {
                    Intent intent = new Intent();

                    try {
                        String accessToken = tryExtractAccessToken(url);
                        String userId = tryExtractUserId(url);
                        Settings.get().accounts().storeAccessToken(Integer.parseInt(userId), accessToken);
                    } catch (Exception ignored) {
                    }

                    setResult(RESULT_OK, intent);
                }

                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class VkontakteWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            parseUrl(url);
        }
    }
}
