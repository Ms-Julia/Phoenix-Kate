package biz.dealnote.messenger.adapter;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.EventListener;
import java.util.List;

import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.adapter.base.RecyclerBindableAdapter;
import biz.dealnote.messenger.model.Document;
import biz.dealnote.messenger.model.PhotoSize;
import biz.dealnote.messenger.picasso.PicassoInstance;
import biz.dealnote.messenger.settings.CurrentTheme;
import biz.dealnote.messenger.util.AppTextUtils;
import biz.dealnote.messenger.util.Utils;

import static biz.dealnote.messenger.util.Objects.nonNull;

public class DocsAdapter extends RecyclerBindableAdapter<Document, DocsAdapter.DocViewHolder> {

    private ActionListener mActionListener;

    public DocsAdapter(List<Document> data) {
        super(data);
    }

    public void setActionListener(ActionListener listener) {
        mActionListener = listener;
    }

    @Override
    protected void onBindItemViewHolder(DocViewHolder holder, int position, int type) {
        Document item = getItem(position);

        String targetExt = "." + item.getExt().toUpperCase();

        holder.tvExt.setText(targetExt);
        holder.tvTitle.setText(item.getTitle());
        holder.tvSize.setText(AppTextUtils.getSizeString((int) item.getSize()));

        String previewUrl = item.getPreviewWithSize(PhotoSize.M, false);
        boolean withImage = !TextUtils.isEmpty(previewUrl);

        holder.ivImage.setVisibility(withImage ? View.VISIBLE : View.GONE);
        holder.ivImage.setBackgroundColor(Color.TRANSPARENT);

        if (withImage) {
            PicassoInstance.with()
                    .load(previewUrl)
                    .tag(Constants.PICASSO_TAG)
                    .into(holder.ivImage);
        }

        holder.itemView.setOnClickListener(v -> {
            if (nonNull(mActionListener)) {
                mActionListener.onDocClick(holder.getBindingAdapterPosition(), item);
            }
        });

        holder.itemView.setOnLongClickListener(v -> nonNull(mActionListener)
                && mActionListener.onDocLongClick(holder.getBindingAdapterPosition(), item));
    }

    @Override
    protected DocViewHolder viewHolder(View view, int type) {
        return new DocViewHolder(view);
    }

    @Override
    protected int layoutId(int type) {
        return R.layout.item_document_big;
    }

    public interface ActionListener extends EventListener {
        void onDocClick(int index, @NonNull Document doc);

        boolean onDocLongClick(int index, @NonNull Document doc);
    }

    static class DocViewHolder extends RecyclerView.ViewHolder {

        TextView tvExt;
        ImageView ivImage;
        TextView tvTitle;
        TextView tvSize;

        private DocViewHolder(View root) {
            super(root);
            tvExt = root.findViewById(R.id.item_document_big_ext);
            ivImage = root.findViewById(R.id.item_document_big_image);
            tvTitle = root.findViewById(R.id.item_document_big_title);
            tvSize = root.findViewById(R.id.item_document_big_size);
            Utils.setColorFilter(tvExt.getBackground(), CurrentTheme.getColorPrimary(root.getContext()));
        }
    }
}