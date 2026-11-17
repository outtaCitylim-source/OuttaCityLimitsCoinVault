package com.outtacitylimits.vault;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import java.util.ArrayList;
import java.util.List;

public class CoinAdapter extends RecyclerView.Adapter<CoinAdapter.CoinViewHolder> {
    private List<Product> coins = new ArrayList<>();

    public void setCoins(List<Product> newCoins) {
        this.coins = newCoins;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CoinViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // You will need to create R.layout.item_coin in res/layout/
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_coin, parent, false);
        return new CoinViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CoinViewHolder holder, int position) {
        Product coin = coins.get(position);
        holder.title.setText(coin.getTitle());
        holder.description.setText(coin.getDescription());
        holder.price.setText(String.format("$%.2f", coin.getPrice()));
        holder.status.setText(coin.getStatus() != null ? coin.getStatus().toUpperCase() : "AVAILABLE");
    }

    @Override
    public int getItemCount() {
        return coins.size();
    }

    static class CoinViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, price;
        Chip status;

        CoinViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.coinTitle);
            description = itemView.findViewById(R.id.coinDescription);
            price = itemView.findViewById(R.id.coinPrice);
            status = itemView.findViewById(R.id.coinStatus);
        }
    }
}
