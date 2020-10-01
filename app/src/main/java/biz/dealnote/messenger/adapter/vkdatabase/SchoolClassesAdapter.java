package biz.dealnote.messenger.adapter.vkdatabase;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.model.database.SchoolClazz;

public class SchoolClassesAdapter extends RecyclerView.Adapter<SchoolClassesAdapter.Holder> {

    private final Context mContext;
    private final List<SchoolClazz> mData;
    private Listener mListener;

    public SchoolClassesAdapter(Context mContext, List<SchoolClazz> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @NotNull
    @Override
    public Holder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(mContext).inflate(R.layout.item_country, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        SchoolClazz schoolClazzDto = mData.get(position);
        holder.name.setText(schoolClazzDto.getTitle());

        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onClick(schoolClazzDto);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public interface Listener {
        void onClick(SchoolClazz schoolClazz);
    }

    public static class Holder extends RecyclerView.ViewHolder {

        TextView name;

        public Holder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
        }
    }
}
