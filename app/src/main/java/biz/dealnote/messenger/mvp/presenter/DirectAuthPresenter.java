package biz.dealnote.messenger.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.Nullable;

import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.api.Auth;
import biz.dealnote.messenger.api.CaptchaNeedException;
import biz.dealnote.messenger.api.NeedValidationException;
import biz.dealnote.messenger.api.interfaces.INetworker;
import biz.dealnote.messenger.api.model.LoginResponse;
import biz.dealnote.messenger.model.Captcha;
import biz.dealnote.messenger.mvp.presenter.base.RxSupportPresenter;
import biz.dealnote.messenger.mvp.view.IDirectAuthView;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.mvp.reflect.OnGuiCreated;

import static biz.dealnote.messenger.util.Utils.getCauseIfRuntime;
import static biz.dealnote.messenger.util.Utils.isEmpty;
import static biz.dealnote.messenger.util.Utils.nonEmpty;
import static biz.dealnote.messenger.util.Utils.trimmedNonEmpty;


public class DirectAuthPresenter extends RxSupportPresenter<IDirectAuthView> {

    private final INetworker networker;

    private Captcha requieredCaptcha;
    private boolean requireSmsCode;
    private boolean requireAppCode;

    private boolean loginNow;

    private String username;
    private String pass;
    private String smsCode;
    private String captcha;
    private String appCode;
    private String RedirectUrl;

    public DirectAuthPresenter(@Nullable Bundle savedInstanceState) {
        super(savedInstanceState);
        networker = Injection.provideNetworkInterfaces();
    }

    public void fireLoginClick() {
        doLogin(false);
    }

    private void doLogin(boolean forceSms) {
        getView().hideKeyboard();

        String trimmedUsername = nonEmpty(username) ? username.trim() : "";
        String trimmedPass = nonEmpty(pass) ? pass.trim() : "";
        String captchaSid = Objects.nonNull(requieredCaptcha) ? requieredCaptcha.getSid() : null;
        String captchaCode = nonEmpty(captcha) ? captcha.trim() : null;

        String code;

        if (requireSmsCode) {
            code = (nonEmpty(smsCode) ? smsCode.trim() : null);
        } else if (requireAppCode) {
            code = (nonEmpty(appCode) ? appCode.trim() : null);
        } else {
            code = null;
        }

        setLoginNow(true);
        appendDisposable(networker.vkDirectAuth()
                .directLogin("password", Constants.API_ID, Constants.SECRET,
                        trimmedUsername, trimmedPass, Constants.AUTH_VERSION, false,
                        Auth.getScope(), code, captchaSid, captchaCode, forceSms)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onLoginResponse, t -> onLoginError(getCauseIfRuntime(t))));
    }

    private void onLoginError(Throwable t) {
        setLoginNow(false);

        requieredCaptcha = null;
        requireAppCode = false;
        requireSmsCode = false;

        if (t instanceof CaptchaNeedException) {
            String sid = ((CaptchaNeedException) t).getSid();
            String img = ((CaptchaNeedException) t).getImg();
            requieredCaptcha = new Captcha(sid, img);
        } else if (t instanceof NeedValidationException) {
            RedirectUrl = ((NeedValidationException) t).getValidationURL();
            if (!isEmpty(RedirectUrl)) {
                onValidate();
            }

            /*
            if ("2fa_sms".equalsIgnoreCase(type) || "2fa_libverify".equalsIgnoreCase(type)) {
                requireSmsCode = true;
                RedirectUrl = ((NeedValidationException) t).getValidationURL();
            } else if ("2fa_app".equalsIgnoreCase(type)) {
                requireAppCode = true;
            }
            if (!isEmpty(sid)) {
                appendDisposable(networker.vkAuth()
                        .validatePhone(Constants.API_ID, Constants.API_ID, Constants.SECRET, sid, Constants.AUTH_VERSION)
                        .compose(RxUtils.applySingleIOToMainSchedulers())
                        .subscribe(result -> {
                        }, ex -> showError(getView(), getCauseIfRuntime(t))));
            }
             */
        } else {
            t.printStackTrace();
            showError(getView(), t);
        }

        resolveCaptchaViews();
        resolveSmsRootVisibility();
        resolveAppCodeRootVisibility();
        resolveButtonLoginState();

        if (Objects.nonNull(requieredCaptcha)) {
            callView(IDirectAuthView::moveFocusToCaptcha);
        } else if (requireSmsCode) {
            callView(IDirectAuthView::moveFocusToSmsCode);
        } else if (requireAppCode) {
            callView(IDirectAuthView::moveFocusToAppCode);
        }
    }

    @OnGuiCreated
    private void resolveSmsRootVisibility() {
        if (isGuiReady()) {
            getView().setSmsRootVisible(requireSmsCode);
        }
    }

    @OnGuiCreated
    private void resolveAppCodeRootVisibility() {
        if (isGuiReady()) {
            getView().setAppCodeRootVisible(requireAppCode);
        }
    }

    @OnGuiCreated
    private void resolveCaptchaViews() {
        if (isGuiReady()) {
            getView().setCaptchaRootVisible(Objects.nonNull(requieredCaptcha));

            if (Objects.nonNull(requieredCaptcha)) {
                getView().displayCaptchaImage(requieredCaptcha.getImg());
            }
        }
    }

    private void onLoginResponse(LoginResponse response) {
        setLoginNow(false);

        String TwFa = "none";
        if (nonEmpty(response.access_token) && response.user_id > 0) {
            String Pass = nonEmpty(pass) ? pass.trim() : "";
            if (requireSmsCode)
                TwFa = "2fa_sms";
            else if (requireAppCode)
                TwFa = "2fa_app";
            String TwFafin = TwFa;
            callView(view -> view.returnSuccessToParent(response.user_id, response.access_token, nonEmpty(username) ? username.trim() : "", Pass, TwFafin));
        }
    }

    public void onValidate() {
        callView(view -> view.returnSuccessValidation(RedirectUrl, nonEmpty(username) ? username.trim() : "", nonEmpty(pass) ? pass.trim() : "", "web_validation"));
    }

    private void setLoginNow(boolean loginNow) {
        this.loginNow = loginNow;
        resolveLoadingViews();
    }

    @OnGuiCreated
    private void resolveLoadingViews() {
        if (isGuiReady()) {
            getView().displayLoading(loginNow);
        }
    }

    public void fireLoginViaWebClick() {
        getView().returnLoginViaWebAction();
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveButtonLoginState();
    }

    private void resolveButtonLoginState() {
        if (isGuiResumed()) {
            getView().setLoginButtonEnabled(trimmedNonEmpty(username)
                    && nonEmpty(pass)
                    && (Objects.isNull(requieredCaptcha) || trimmedNonEmpty(captcha))
                    && (!requireSmsCode || trimmedNonEmpty(smsCode))
                    && (!requireAppCode || trimmedNonEmpty(appCode)));
        }
    }

    public void fireLoginEdit(CharSequence sequence) {
        username = sequence.toString();
        resolveButtonLoginState();
    }

    public void firePasswordEdit(CharSequence s) {
        pass = s.toString();
        resolveButtonLoginState();
    }

    public void fireSmsCodeEdit(CharSequence sequence) {
        smsCode = sequence.toString();
        resolveButtonLoginState();
    }

    public void fireCaptchaEdit(CharSequence s) {
        captcha = s.toString();
        resolveButtonLoginState();
    }

    public void fireButtonSendCodeViaSmsClick() {
        doLogin(true);
    }

    public void fireAppCodeEdit(CharSequence s) {
        appCode = s.toString();
        resolveButtonLoginState();
    }
}