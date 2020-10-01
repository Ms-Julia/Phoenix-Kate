package biz.dealnote.messenger.mvp.presenter;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.Arrays;
import java.util.List;

import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.domain.IOwnersRepository;
import biz.dealnote.messenger.domain.Repository;
import biz.dealnote.messenger.model.Owner;
import biz.dealnote.messenger.mvp.presenter.base.RxSupportPresenter;
import biz.dealnote.messenger.mvp.view.IEnterPinView;
import biz.dealnote.messenger.settings.ISettings;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.mvp.reflect.OnGuiCreated;

import static biz.dealnote.messenger.util.Utils.isEmpty;

public class EnterPinPresenter extends RxSupportPresenter<IEnterPinView> {

    private static final String SAVE_VALUE = "save_value";
    private static final int LAST_CIRCLE_VISIBILITY_DELAY = 200;
    private static final int NO_VALUE = -1;
    private static final int MAX_ATTEMPT_DELAY = 3 * 60 * 1000;
    private final IOwnersRepository ownersRepository;
    private final ISettings.ISecuritySettings securitySettings;
    private final int[] mValues;
    private final Fragment myContext;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final Runnable mOnFullyEnteredRunnable = this::onFullyEntered;
    private Owner mOwner;

    public EnterPinPresenter(Fragment Context, @Nullable Bundle savedState) {
        super(savedState);
        myContext = Context;
        securitySettings = Settings.get().security();
        ownersRepository = Repository.INSTANCE.getOwners();

        if (savedState != null) {
            mValues = savedState.getIntArray(SAVE_VALUE);
        } else {
            mValues = new int[Constants.PIN_DIGITS_COUNT];
            resetPin();
        }

        if (Objects.isNull(mOwner)) {
            loadOwnerInfo();
        }

        if (securitySettings.isEntranceByFingerprintAllowed() && canAuthenticateWithBiometrics()) {
            showBiometricPrompt();
        }
    }

    private void loadOwnerInfo() {
        int accountId = Settings.get()
                .accounts()
                .getCurrent();

        if (accountId != ISettings.IAccountsSettings.INVALID_ID) {
            appendDisposable(ownersRepository.getBaseOwnerInfo(accountId, accountId, IOwnersRepository.MODE_ANY)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(this::onOwnerInfoReceived, t -> {/*ignore*/}));
        }
    }

    private void onOwnerInfoReceived(Owner owner) {
        mOwner = owner;
        resolveAvatarView();
    }

    public void onFingerprintClicked() {
        if (!securitySettings.isEntranceByFingerprintAllowed()) {
            getView().getPhoenixToast().showToastError(R.string.error_login_by_fingerprint_not_allowed);
            return;
        }
        if (canAuthenticateWithBiometrics())
            showBiometricPrompt();
        else
            getView().getPhoenixToast().showToastError(R.string.biometric_not_support);
    }

    private void onFingerprintRecognizeSuccess() {
        if (isGuiReady()) {
            getView().sendSuccessAndClose();
        }
    }

    @OnGuiCreated
    private void resolveAvatarView() {
        if (!isGuiReady()) return;

        String avatar = Objects.isNull(mOwner) ? null : mOwner.getMaxSquareAvatar();
        if (isEmpty(avatar)) {
            getView().displayDefaultAvatar();
        } else {
            getView().displayAvatarFromUrl(avatar);
        }
    }

    private long getNextPinAttemptTimeout() {
        List<Long> history = Settings.get()
                .security()
                .getPinEnterHistory();

        if (history.size() < Settings.get().security().getPinHistoryDepth()) {
            return 0;
        }

        long howLongAgoWasFirstAttempt = System.currentTimeMillis() - history.get(0);
        return howLongAgoWasFirstAttempt < MAX_ATTEMPT_DELAY ? MAX_ATTEMPT_DELAY - howLongAgoWasFirstAttempt : 0;
    }

    @OnGuiCreated
    private void refreshViewCirclesVisibility() {
        if (isGuiReady()) {
            getView().displayPin(mValues, NO_VALUE);
        }
    }

    public void onBackspaceClicked() {
        int currentIndex = getCurrentIndex();
        if (currentIndex == -1) {
            mValues[mValues.length - 1] = NO_VALUE;
        } else if (currentIndex > 0) {
            mValues[currentIndex - 1] = NO_VALUE;
        }

        refreshViewCirclesVisibility();
    }

    private void onFullyEntered() {
        if (!isFullyEntered()) return;

        long timeout = getNextPinAttemptTimeout();
        if (timeout > 0) {
            safeShowError(getView(), R.string.limit_exceeded_number_of_attempts_message, timeout / 1000);

            resetPin();
            refreshViewCirclesVisibility();
            return;
        }

        Settings.get()
                .security()
                .firePinAttemptNow();

        if (Settings.get().security().isPinValid(mValues)) {
            onEnteredRightPin();
        } else {
            onEnteredWrongPin();
        }
    }

    private void onEnteredRightPin() {
        Settings.get()
                .security()
                .clearPinHistory();

        getView().sendSuccessAndClose();
    }

    private void onEnteredWrongPin() {
        resetPin();
        refreshViewCirclesVisibility();

        getView().showError(R.string.pin_is_invalid_message);
        getView().displayErrorAnimation();
    }

    public void onNumberClicked(int value) {
        if (isFullyEntered()) return;

        mValues[getCurrentIndex()] = value;
        refreshViewCirclesVisibility();

        if (isFullyEntered()) {
            mHandler.removeCallbacks(mOnFullyEnteredRunnable);
            mHandler.postDelayed(mOnFullyEnteredRunnable, LAST_CIRCLE_VISIBILITY_DELAY);
        }
    }

    private boolean isFullyEntered() {
        for (int value : mValues) {
            if (value == NO_VALUE) {
                return false;
            }
        }

        return true;
    }

    private int getCurrentIndex() {
        for (int i = 0; i < mValues.length; i++) {
            if (mValues[i] == NO_VALUE) {
                return i;
            }
        }

        return -1;
    }

    private void resetPin() {
        Arrays.fill(mValues, NO_VALUE);
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);
        outState.putIntArray(SAVE_VALUE, mValues);
    }

    private void showBiometricPrompt() {

        BiometricPrompt.AuthenticationCallback authenticationCallback = getAuthenticationCallback();
        BiometricPrompt mBiometricPrompt = new BiometricPrompt(myContext, ContextCompat.getMainExecutor(getApplicationContext()), authenticationCallback);

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getApplicationContext().getString(R.string.biometric))
                .setNegativeButtonText(getApplicationContext().getString(R.string.cancel))
                .build();
        mBiometricPrompt.authenticate(promptInfo);
    }

    private boolean canAuthenticateWithBiometrics() {
        BiometricManager biometricManager = BiometricManager.from(getApplicationContext());
        if (biometricManager != null) {
            return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS;
        }
        return false;
    }

    private BiometricPrompt.AuthenticationCallback getAuthenticationCallback() {
        return new BiometricPrompt.AuthenticationCallback() {

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                onFingerprintRecognizeSuccess();
            }
        };
    }
}