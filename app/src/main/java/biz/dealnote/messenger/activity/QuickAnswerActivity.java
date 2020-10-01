package biz.dealnote.messenger.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso3.Transformation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.crypt.KeyLocationPolicy;
import biz.dealnote.messenger.domain.IMessagesRepository;
import biz.dealnote.messenger.domain.Repository;
import biz.dealnote.messenger.listener.TextWatcherAdapter;
import biz.dealnote.messenger.longpoll.NotificationHelper;
import biz.dealnote.messenger.model.Message;
import biz.dealnote.messenger.model.Peer;
import biz.dealnote.messenger.model.SaveMessageBuilder;
import biz.dealnote.messenger.place.Place;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.messenger.settings.CurrentTheme;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.task.TextingNotifier;
import biz.dealnote.messenger.util.AppTextUtils;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.messenger.util.ViewUtils;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

import static biz.dealnote.messenger.util.RxUtils.ignore;
import static biz.dealnote.messenger.util.Utils.isEmpty;

public class QuickAnswerActivity extends AppCompatActivity {

    public static final String PARAM_BODY = "body";

    public static final String EXTRA_FOCUS_TO_FIELD = "focus_to_field";
    public static final String EXTRA_LIVE_DELAY = "live_delay";
    private final CompositeDisposable mLiveSubscription = new CompositeDisposable();
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private TextInputEditText etText;
    private TextingNotifier notifier;
    private int accountId;
    private Message msg;
    private boolean messageIsRead;
    private IMessagesRepository messagesRepository;

    public static Intent forStart(Context context, int accountId, Message msg, String body, String imgUrl, String title) {
        Intent intent = new Intent(context, QuickAnswerActivity.class);
        intent.putExtra(PARAM_BODY, body);
        intent.putExtra(Extra.ACCOUNT_ID, accountId);
        intent.putExtra(Extra.MESSAGE, msg);
        intent.putExtra(Extra.TITLE, title);
        intent.putExtra(Extra.IMAGE, imgUrl);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Settings.get().main().isAmoledTheme() ? R.style.QuickReply_Amoled : R.style.QuickReply);
        super.onCreate(savedInstanceState);

        messagesRepository = Repository.INSTANCE.getMessages();

        boolean focusToField = getIntent().getBooleanExtra(EXTRA_FOCUS_TO_FIELD, true);

        if (!focusToField) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }

        msg = java.util.Objects.requireNonNull(getIntent().getExtras()).getParcelable(Extra.MESSAGE);
        accountId = getIntent().getExtras().getInt(Extra.ACCOUNT_ID);
        notifier = new TextingNotifier(accountId);

        setContentView(R.layout.activity_quick_answer);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.arrow_left);
        }

        setSupportActionBar(toolbar);

        TextView tvMessage = findViewById(R.id.item_message_text);
        TextView tvTime = findViewById(R.id.item_message_time);
        etText = findViewById(R.id.activity_quick_answer_edit_text);

        ImageView ivAvatar = findViewById(R.id.avatar);

        ImageButton btnToDialog = findViewById(R.id.activity_quick_answer_to_dialog);
        ImageButton btnSend = findViewById(R.id.activity_quick_answer_send);

        String messageTime = AppTextUtils.getDateFromUnixTime(this, msg.getDate());
        String title = getIntent().getStringExtra(Extra.TITLE);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }

        tvMessage.setText(getIntent().getStringExtra(PARAM_BODY), TextView.BufferType.SPANNABLE);
        tvTime.setText(messageTime);

        Transformation transformation = CurrentTheme.createTransformationForAvatar(this);
        String imgUrl = getIntent().getStringExtra(Extra.IMAGE);
        if (ivAvatar != null) {
            ViewUtils.displayAvatar(ivAvatar, transformation, imgUrl, null);
        }

        etText.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable editable) {
                if (!messageIsRead) {
                    setMessageAsRead();
                    messageIsRead = true;
                }

                cancelFinishWithDelay();

                if (Objects.nonNull(notifier)) {
                    notifier.notifyAboutTyping(msg.getPeerId());
                }
            }
        });

        btnSend.setOnClickListener(view -> send());
        btnToDialog.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setAction(MainActivity.ACTION_OPEN_PLACE);

            Place chatPlace = PlaceFactory.getChatPlace(accountId, accountId, new Peer(msg.getPeerId()).setAvaUrl(imgUrl).setTitle(title), 0);
            intent.putExtra(Extra.PLACE, chatPlace);
            startActivity(intent);
            finish();
        });

        boolean liveDelay = getIntent().getBooleanExtra(EXTRA_LIVE_DELAY, false);
        if (liveDelay) {
            finishWithDelay();
        }
    }

    private void finishWithDelay() {
        mLiveSubscription.add(Observable.just(new Object())
                .delay(1, TimeUnit.MINUTES)
                .subscribe(o -> finish()));
    }

    private void cancelFinishWithDelay() {
        mLiveSubscription.dispose();
    }

    @Override
    protected void onDestroy() {
        mLiveSubscription.dispose();
        compositeDisposable.dispose();
        super.onDestroy();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    /**
     * Отправка сообщения
     */
    private void send() {
        String trimmedtext = etText.getText().toString().trim();
        if (isEmpty(trimmedtext)) {
            Toast.makeText(this, getString(R.string.text_hint), Toast.LENGTH_SHORT).show();
            return;
        }

        boolean requireEncryption = Settings.get()
                .security()
                .isMessageEncryptionEnabled(accountId, msg.getPeerId());

        @KeyLocationPolicy
        int policy = KeyLocationPolicy.PERSIST;

        if (requireEncryption) {
            policy = Settings.get()
                    .security()
                    .getEncryptionLocationPolicy(accountId, msg.getPeerId());
        }

        SaveMessageBuilder builder = new SaveMessageBuilder(accountId, msg.getPeerId())
                .setBody(trimmedtext)
                .setForwardMessages(new ArrayList<>(Collections.singleton(msg)))
                .setRequireEncryption(requireEncryption)
                .setKeyLocationPolicy(policy);

        compositeDisposable.add(messagesRepository.put(builder)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onMessageSaved, this::onSavingError));
    }

    private void onSavingError(Throwable throwable) {
        Utils.showRedTopToast(this, throwable.toString());
    }

    @SuppressWarnings("unused")
    private void onMessageSaved(Message message) {
        NotificationHelper.tryCancelNotificationForPeer(this, accountId, msg.getPeerId());
        messagesRepository.runSendingQueue();
        finish();
    }

    private void setMessageAsRead() {
        compositeDisposable.add(messagesRepository.markAsRead(accountId, msg.getPeerId(), msg.getId())
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(RxUtils.dummy(), ignore()));
    }
}
