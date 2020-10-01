package biz.dealnote.messenger.view.emoji;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso3.Callback;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.model.Sticker;
import biz.dealnote.messenger.picasso.PicassoInstance;
import biz.dealnote.messenger.util.Utils;

public class StickersKeyWordsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private List<Sticker> stickers;
    private EmojiconsPopup.OnStickerClickedListener stickerClickedListener;

    public StickersKeyWordsAdapter(Context context, List<Sticker> stickers) {
        this.context = context;
        this.stickers = stickers;
    }

    public void setStickerClickedListener(EmojiconsPopup.OnStickerClickedListener listener) {
        stickerClickedListener = listener;
    }

    public void setData(List<Sticker> data) {
        if (Utils.isEmpty(data)) {
            stickers = Collections.emptyList();
        } else {
            stickers = data;
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new StickerHolder(LayoutInflater.from(context).inflate(R.layout.sticker_keyword_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Sticker item = stickers.get(position);
        StickerHolder normalHolder = (StickerHolder) holder;
        normalHolder.image.setVisibility(View.VISIBLE);
        String url = item.getImage(256, context).getUrl();

        if (Utils.isEmpty(url)) {
            PicassoInstance.with().cancelRequest(normalHolder.image);
            normalHolder.image.setImageResource(R.drawable.ic_avatar_unknown);
        } else {
            PicassoInstance.with()
                    .load(url)
                    //.networkPolicy(NetworkPolicy.OFFLINE)
                    .tag(Constants.PICASSO_TAG)
                    .into(normalHolder.image, new LoadOnErrorCallback(normalHolder.image, url));
            normalHolder.root.setOnClickListener(v -> stickerClickedListener.onStickerClick(item));
        }
    }

    @Override
    public int getItemCount() {
        return stickers.size();
    }

    private static class LoadOnErrorCallback implements Callback {

        final WeakReference<ImageView> ref;
        final String link;

        private LoadOnErrorCallback(ImageView view, String link) {
            ref = new WeakReference<>(view);
            this.link = link;
        }

        @Override
        public void onSuccess() {
            // do nothink
        }

        @Override
        public void onError(@NotNull Throwable e) {
            ImageView view = ref.get();
            try {
                if (view != null) {
                    PicassoInstance.with()
                            .load(link)
                            .into(view);
                }
            } catch (Exception ignored) {

            }
        }
    }

    static final class StickerHolder extends RecyclerView.ViewHolder {
        final View root;
        final ImageView image;

        StickerHolder(@NonNull View itemView) {
            super(itemView);
            root = itemView.getRootView();
            image = itemView.findViewById(R.id.sticker);
        }
    }
}
