package biz.dealnote.messenger.adapter.horizontal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.adapter.base.RecyclerBindableAdapter;
import biz.dealnote.messenger.model.AudioPlaylist;
import biz.dealnote.messenger.picasso.transforms.ImageHelper;
import biz.dealnote.messenger.picasso.transforms.PolyTransformation;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.AppTextUtils;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.messenger.util.ViewUtils;

public class HorizontalPlaylistAdapter extends RecyclerBindableAdapter<AudioPlaylist, HorizontalPlaylistAdapter.Holder> {

    private Listener listener;

    public HorizontalPlaylistAdapter(List<AudioPlaylist> data) {
        super(data);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindItemViewHolder(Holder holder, int position, int type) {
        AudioPlaylist playlist = getItem(position);

        Context context = holder.itemView.getContext();

        if (!Utils.isEmpty(playlist.getThumb_image()))
            ViewUtils.displayAvatar(holder.thumb, new PolyTransformation(), playlist.getThumb_image(), Constants.PICASSO_TAG);
        else
            holder.thumb.setImageBitmap(ImageHelper.getElpsedBitmap(BitmapFactory.decodeResource(context.getResources(), Settings.get().ui().isDarkModeEnabled(context) ? R.drawable.generic_audio_nowplaying_dark : R.drawable.generic_audio_nowplaying_light), 20, 20));
        holder.count.setText(playlist.getCount() + " " + context.getString(R.string.audios_pattern_count));
        holder.name.setText(playlist.getTitle());
        if (Utils.isEmpty(playlist.getDescription()))
            holder.description.setVisibility(View.GONE);
        else {
            holder.description.setVisibility(View.VISIBLE);
            holder.description.setText(playlist.getDescription());
            holder.description.setOnClickListener(view -> {
                if (holder.description.getMaxLines() == 1)
                    holder.description.setMaxLines(6);
                else
                    holder.description.setMaxLines(1);
            });
        }
        if (Utils.isEmpty(playlist.getArtist_name()))
            holder.artist.setVisibility(View.GONE);
        else {
            holder.artist.setVisibility(View.VISIBLE);
            holder.artist.setText(playlist.getArtist_name());
        }
        if (playlist.getYear() == 0)
            holder.year.setVisibility(View.GONE);
        else {
            holder.year.setVisibility(View.VISIBLE);
            holder.year.setText(String.valueOf(playlist.getYear()));
        }
        if (Utils.isEmpty(playlist.getGenre()))
            holder.genre.setVisibility(View.GONE);
        else {
            holder.genre.setVisibility(View.VISIBLE);
            holder.genre.setText(playlist.getGenre());
        }
        holder.update.setText(AppTextUtils.getDateFromUnixTime(context, playlist.getUpdate_time()));
        holder.add.setOnClickListener(v -> listener.onPlayListClick(playlist, position));
        if (playlist.getOwnerId() == Settings.get().accounts().getCurrent())
            holder.add.setImageResource(R.drawable.ic_outline_delete);
        else
            holder.add.setImageResource(R.drawable.plus);
    }

    @Override
    protected Holder viewHolder(View view, int type) {
        return new Holder(view);
    }

    @Override
    protected int layoutId(int type) {
        return R.layout.item_internal_audio_playlist;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener {
        void onPlayListClick(AudioPlaylist item, int pos);
    }

    static class Holder extends RecyclerView.ViewHolder {

        ImageView thumb;
        TextView name;
        TextView description;
        TextView count;
        TextView year;
        TextView artist;
        TextView genre;
        TextView update;
        View playlist_container;
        FloatingActionButton add;

        public Holder(View itemView) {
            super(itemView);
            thumb = itemView.findViewById(R.id.item_thumb);
            name = itemView.findViewById(R.id.item_name);
            count = itemView.findViewById(R.id.item_count);
            playlist_container = itemView.findViewById(R.id.playlist_container);
            description = itemView.findViewById(R.id.item_description);
            update = itemView.findViewById(R.id.item_time);
            year = itemView.findViewById(R.id.item_year);
            artist = itemView.findViewById(R.id.item_artist);
            genre = itemView.findViewById(R.id.item_genre);
            add = itemView.findViewById(R.id.add_playlist);
        }
    }
}
