package biz.dealnote.messenger.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.model.PhotoAlbum;
import biz.dealnote.messenger.model.PhotoSize;
import biz.dealnote.messenger.picasso.PicassoInstance;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.AppTextUtils;
import biz.dealnote.messenger.util.Utils;

import static biz.dealnote.messenger.util.Utils.nonEmpty;

public class PhotoAlbumsAdapter extends RecyclerView.Adapter<PhotoAlbumsAdapter.Holder> {

    private final Context context;
    @PhotoSize
    private final int mPhotoPreviewSize;
    private List<PhotoAlbum> data;
    private ClickListener clickListener;

    public PhotoAlbumsAdapter(List<PhotoAlbum> data, Context context) {
        this.data = data;
        this.context = context;
        mPhotoPreviewSize = Settings.get().main().getPrefPreviewImageSize();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_photo_album, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        PhotoAlbum album = data.get(position);

        String url = album.getSizes().getUrlForSize(mPhotoPreviewSize, false);

        if (nonEmpty(url)) {
            PicassoInstance.with()
                    .load(url)
                    .placeholder(R.drawable.background_gray)
                    .tag(Constants.PICASSO_TAG)
                    .into(holder.thumb);

            holder.thumb.setVisibility(View.VISIBLE);
        } else {
            PicassoInstance.with().cancelRequest(holder.thumb);
            holder.thumb.setVisibility(View.INVISIBLE);
        }

        holder.count.setText(context.getString(R.string.photos_count, album.getSize()));
        holder.name.setText(album.getTitle());
        if (Utils.isEmpty(album.getDescription()))
            holder.description.setVisibility(View.GONE);
        else {
            holder.description.setVisibility(View.VISIBLE);
            holder.description.setText(album.getDescription());
        }

        holder.update.setText(AppTextUtils.getDateFromUnixTime(context, album.getUpdatedTime()));
        holder.album_container.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onAlbumClick(holder.getBindingAdapterPosition(), album);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<PhotoAlbum> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public interface ClickListener {
        void onAlbumClick(int index, PhotoAlbum album);
    }

    public static class Holder extends RecyclerView.ViewHolder {

        ImageView thumb;
        TextView name;
        TextView description;
        TextView count;
        TextView update;
        View album_container;

        public Holder(View itemView) {
            super(itemView);
            thumb = itemView.findViewById(R.id.item_thumb);
            name = itemView.findViewById(R.id.item_title);
            count = itemView.findViewById(R.id.item_count);
            album_container = itemView.findViewById(R.id.album_container);
            description = itemView.findViewById(R.id.item_description);
            update = itemView.findViewById(R.id.item_time);
        }
    }
}
