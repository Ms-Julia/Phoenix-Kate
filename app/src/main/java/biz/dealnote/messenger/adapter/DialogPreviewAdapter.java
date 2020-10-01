package biz.dealnote.messenger.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso3.Transformation;

import org.jetbrains.annotations.NotNull;

import java.util.EventListener;
import java.util.List;

import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.model.Chat;
import biz.dealnote.messenger.model.Community;
import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.settings.CurrentTheme;
import biz.dealnote.messenger.util.ViewUtils;

public class DialogPreviewAdapter extends RecyclerView.Adapter<DialogPreviewAdapter.DialogPreviewHolder> {

    private final ActionListener actionListener;
    private final Transformation mTransformation;
    private List<Object> mData;

    public DialogPreviewAdapter(Context context, List<Object> items, ActionListener actionListener) {
        mData = items;
        mTransformation = CurrentTheme.createTransformationForAvatar(context);
        this.actionListener = actionListener;
    }

    @NotNull
    @Override
    public DialogPreviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DialogPreviewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dialog_preview, parent, false));
    }

    @Override
    public void onBindViewHolder(@NotNull DialogPreviewHolder holder, int position) {
        Object item = mData.get(position);

        if (item instanceof User) {
            bindUserDialog(holder, (User) item);
        }

        if (item instanceof Community) {
            bindGroupDialog(holder, (Community) item);
        }

        if (item instanceof Chat) {
            bindGroupChat(holder, (Chat) item);
        }

        holder.itemView.setOnClickListener(v -> actionListener.onEntryClick(item));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    private void bindUserDialog(DialogPreviewHolder holder, User item) {
        holder.mTitle.setText(item.getFullName());
        ViewUtils.displayAvatar(holder.mAvatar, mTransformation, item.get100photoOrSmaller(), Constants.PICASSO_TAG);
    }

    private void bindGroupDialog(DialogPreviewHolder holder, Community item) {
        holder.mTitle.setText(item.getFullName());
        ViewUtils.displayAvatar(holder.mAvatar, mTransformation, item.get100photoOrSmaller(), Constants.PICASSO_TAG);
    }

    private void bindGroupChat(DialogPreviewHolder holder, Chat item) {
        holder.mTitle.setText(item.getTitle());
        ViewUtils.displayAvatar(holder.mAvatar, mTransformation, item.get100orSmallerAvatar(), Constants.PICASSO_TAG, R.drawable.ic_group_chat);
    }

    public void setData(List<Object> data) {
        mData = data;
        notifyDataSetChanged();
    }

    public interface ActionListener extends EventListener {
        void onEntryClick(Object o);
    }

    static class DialogPreviewHolder extends RecyclerView.ViewHolder {

        private final ImageView mAvatar;
        private final TextView mTitle;

        DialogPreviewHolder(View itemView) {
            super(itemView);
            mTitle = itemView.findViewById(R.id.dialog_title);
            mAvatar = itemView.findViewById(R.id.item_chat_avatar);
        }
    }
}