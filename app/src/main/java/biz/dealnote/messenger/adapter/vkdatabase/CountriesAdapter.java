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
import biz.dealnote.messenger.model.database.Country;

public class CountriesAdapter extends RecyclerView.Adapter<CountriesAdapter.Holder> {

    private final Context mContext;
    private List<Country> mData;
    private Listener mListener;

    public CountriesAdapter(Context mContext, List<Country> mData) {
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
        Country country = mData.get(position);
        holder.name.setText(country.getTitle());

        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onClick(country);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setData(List<Country> data) {
        mData = data;
        notifyDataSetChanged();
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public interface Listener {
        void onClick(Country country);
    }

    public static class Holder extends RecyclerView.ViewHolder {

        TextView name;

        public Holder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
        }
    }
}