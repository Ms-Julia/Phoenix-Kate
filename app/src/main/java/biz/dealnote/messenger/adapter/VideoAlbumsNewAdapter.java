package biz.dealnote.messenger.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.model.VideoAlbum;
import biz.dealnote.messenger.picasso.PicassoInstance;

import static biz.dealnote.messenger.util.Utils.isEmpty;
import static biz.dealnote.messenger.util.Utils.nonEmpty;

public class VideoAlbumsNewAdapter extends RecyclerView.Adapter<VideoAlbumsNewAdapter.ViewHolder> {

    public static final String PICASSO_TAG = "VideoAlbumsNewAdapter";

    private final Context context;
    private List<VideoAlbum> data;
    private Listener listener;

    public VideoAlbumsNewAdapter(Context context, List<VideoAlbum> data) {
        this.context = context;
        this.data = data;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_video_album, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        VideoAlbum item = data.get(position);

        holder.tvCount.setText(context.getString(R.string.videos_albums_videos_counter, item.getCount()));
        holder.tvTitle.setText(item.getTitle());
        String photoUrl = item.getImage();

        holder.ivPhoto.setVisibility(isEmpty(photoUrl) ? View.INVISIBLE : View.VISIBLE);

        if (nonEmpty(photoUrl)) {
            PicassoInstance.with()
                    .load(photoUrl)
                    .tag(PICASSO_TAG)
                    .into(holder.ivPhoto);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<VideoAlbum> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener {
        void onClick(VideoAlbum album);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto;
        TextView tvCount;
        TextView tvTitle;

        ViewHolder(View root) {
            super(root);
            ivPhoto = root.findViewById(R.id.item_video_album_image);
            tvCount = root.findViewById(R.id.item_video_album_count);
            tvTitle = root.findViewById(R.id.item_video_album_title);
        }
    }
}