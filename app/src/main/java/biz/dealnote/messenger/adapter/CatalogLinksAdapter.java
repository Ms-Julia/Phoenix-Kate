package biz.dealnote.messenger.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso3.Transformation;

import java.util.EventListener;
import java.util.List;

import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.adapter.base.RecyclerBindableAdapter;
import biz.dealnote.messenger.model.Link;
import biz.dealnote.messenger.model.PhotoSizes;
import biz.dealnote.messenger.picasso.PicassoInstance;
import biz.dealnote.messenger.settings.CurrentTheme;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.messenger.util.ViewUtils;

import static biz.dealnote.messenger.util.Objects.nonNull;


public class CatalogLinksAdapter extends RecyclerBindableAdapter<Link, CatalogLinksAdapter.LinkViewHolder> {

    private final Transformation transformation;
    private ActionListener mActionListner;

    public CatalogLinksAdapter(List<Link> data, Context context) {
        super(data);
        transformation = CurrentTheme.createTransformationForAvatar(context);
    }

    public void setActionListener(ActionListener listener) {
        mActionListner = listener;
    }

    public String getImageUrl(Link link) {

        if (link.getPhoto() == null && link.getPreviewPhoto() != null)
            return link.getPreviewPhoto();

        if (nonNull(link.getPhoto()) && nonNull(link.getPhoto().getSizes())) {
            PhotoSizes sizes = link.getPhoto().getSizes();
            return sizes.getUrlForSize(Settings.get().main().getPrefPreviewImageSize(), true);
        }

        return null;
    }

    @Override
    protected void onBindItemViewHolder(LinkViewHolder holder, int position, int type) {
        Link item = getItem(position);

        if (Utils.isEmpty(item.getTitle()))
            holder.tvTitle.setVisibility(View.INVISIBLE);
        else {
            holder.tvTitle.setVisibility(View.VISIBLE);
            holder.tvTitle.setText(item.getTitle());
        }
        if (Utils.isEmpty(item.getDescription()))
            holder.tvDescription.setVisibility(View.INVISIBLE);
        else {
            holder.tvDescription.setVisibility(View.VISIBLE);
            holder.tvDescription.setText(item.getDescription());
        }

        String imageUrl = getImageUrl(item);
        if (imageUrl != null) {
            ViewUtils.displayAvatar(holder.ivImage, transformation, imageUrl, Constants.PICASSO_TAG);
        } else {
            PicassoInstance.with().cancelRequest(holder.ivImage);
            holder.ivImage.setImageResource(R.drawable.ic_avatar_unknown);
        }

        holder.itemView.setOnClickListener(v -> {
            if (nonNull(mActionListner)) {
                mActionListner.onLinkClick(holder.getBindingAdapterPosition(), item);
            }
        });
    }

    @Override
    protected LinkViewHolder viewHolder(View view, int type) {
        return new LinkViewHolder(view);
    }

    @Override
    protected int layoutId(int type) {
        return R.layout.item_catalog_link;
    }

    public interface ActionListener extends EventListener {
        void onLinkClick(int index, @NonNull Link doc);
    }

    static class LinkViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvTitle;
        TextView tvDescription;

        private LinkViewHolder(View root) {
            super(root);
            ivImage = root.findViewById(R.id.item_link_pic);
            tvTitle = root.findViewById(R.id.item_link_name);
            tvDescription = root.findViewById(R.id.item_link_description);
        }
    }
}
