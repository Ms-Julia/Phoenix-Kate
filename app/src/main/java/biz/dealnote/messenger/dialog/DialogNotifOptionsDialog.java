package biz.dealnote.messenger.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.settings.Settings;

import static biz.dealnote.messenger.settings.NotificationsPrefs.FLAG_HIGH_PRIORITY;
import static biz.dealnote.messenger.settings.NotificationsPrefs.FLAG_LED;
import static biz.dealnote.messenger.settings.NotificationsPrefs.FLAG_SHOW_NOTIF;
import static biz.dealnote.messenger.settings.NotificationsPrefs.FLAG_SOUND;
import static biz.dealnote.messenger.settings.NotificationsPrefs.FLAG_VIBRO;
import static biz.dealnote.messenger.util.Utils.hasFlag;

public class DialogNotifOptionsDialog extends DialogFragment {

    protected int mask;
    private int peerId;
    private int accountId;
    private SwitchCompat scEnable;
    private SwitchCompat scHighPriority;
    private SwitchCompat scSound;
    private SwitchCompat scVibro;
    private SwitchCompat scLed;

    public static DialogNotifOptionsDialog newInstance(int aid, int peerId) {
        Bundle args = new Bundle();
        args.putInt(Extra.PEER_ID, peerId);
        args.putInt(Extra.ACCOUNT_ID, aid);
        DialogNotifOptionsDialog dialog = new DialogNotifOptionsDialog();
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        accountId = getArguments().getInt(Extra.ACCOUNT_ID);
        peerId = getArguments().getInt(Extra.PEER_ID);

        mask = Settings.get()
                .notifications()
                .getNotifPref(accountId, peerId);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View root = View.inflate(requireActivity(), R.layout.dialog_dialog_options, null);

        scEnable = root.findViewById(R.id.enable);
        scHighPriority = root.findViewById(R.id.priority);
        scSound = root.findViewById(R.id.sound);
        scVibro = root.findViewById(R.id.vibro);
        scLed = root.findViewById(R.id.led);

        scEnable.setChecked(hasFlag(mask, FLAG_SHOW_NOTIF));
        scEnable.setOnCheckedChangeListener((buttonView, isChecked) -> resolveOtherSwitches());

        scSound.setChecked(hasFlag(mask, FLAG_SOUND));
        scHighPriority.setChecked(hasFlag(mask, FLAG_HIGH_PRIORITY));
        scVibro.setChecked(hasFlag(mask, FLAG_VIBRO));
        scLed.setChecked(hasFlag(mask, FLAG_LED));

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.peer_notification_settings)
                .setPositiveButton(R.string.button_ok, (dialog, whichButton) -> onSaveClick())
                .setNeutralButton(R.string.set_default, (dialog, which) -> Settings.get()
                        .notifications()
                        .setDefault(accountId, peerId));

        builder.setView(root);
        resolveOtherSwitches();

        return builder.create();
    }

    private void onSaveClick() {
        int newMask = 0;
        if (scEnable.isChecked()) {
            newMask += FLAG_SHOW_NOTIF;
        }

        if (scHighPriority.isEnabled() && scHighPriority.isChecked()) {
            newMask += FLAG_HIGH_PRIORITY;
        }

        if (scSound.isEnabled() && scSound.isChecked()) {
            newMask += FLAG_SOUND;
        }

        if (scVibro.isEnabled() && scVibro.isChecked()) {
            newMask += FLAG_VIBRO;
        }

        if (scLed.isEnabled() && scLed.isChecked()) {
            newMask += FLAG_LED;
        }

        Settings.get()
                .notifications()
                .setNotifPref(accountId, peerId, newMask);
    }

    private void resolveOtherSwitches() {
        boolean enable = scEnable.isChecked();
        scHighPriority.setEnabled(enable);
        scSound.setEnabled(enable);
        scVibro.setEnabled(enable);
        scLed.setEnabled(enable);
    }
}
