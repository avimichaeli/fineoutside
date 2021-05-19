package com.example.fineoutside.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fineoutside.R;
import com.example.fineoutside.data.OrderItem;

import java.util.ArrayList;
import java.util.List;

public class OrderItemsAdapter extends RecyclerView.Adapter<OrderItemsAdapter.OrderItemViewHolder> implements Filterable {

    private final List<OrderItem> orderItemsList;
    private List<OrderItem> orderItemsListFiltered;

    public OrderItemsAdapter(List<OrderItem> orderItemsList) {
        this.orderItemsList = orderItemsList;
        this.orderItemsListFiltered = orderItemsList;
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new OrderItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_order_item_cell, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        OrderItem orderItem = orderItemsListFiltered.get(position);
        holder.item_image.setBackgroundResource(orderItem.getResource());
        holder.item_name_text.setText(orderItem.getName());
        holder.item_description_text.setText(orderItem.getDescription());
        holder.price_text.setText(new StringBuilder(orderItem.getPrice() + " ILS"));
        if (orderItem.getQuantity() > 0) {
            holder.quantity_text.setVisibility(View.VISIBLE);
            holder.quantity_text.setText(new StringBuilder("X " + orderItem.getQuantity()));
        } else {
            holder.quantity_text.setVisibility(View.INVISIBLE);
        }
        holder.plus_image.setOnClickListener(v -> {
            orderItem.setQuantity(orderItem.getQuantity() + 1);
            notifyDataSetChanged();
        });
        holder.minus_image.setOnClickListener(v -> {
            orderItem.setQuantity(orderItem.getQuantity() == 0 ? 0 : orderItem.getQuantity() - 1);
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return orderItemsListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    orderItemsListFiltered = orderItemsList;
                } else {
                    List<OrderItem> filteredList = new ArrayList<>();
                    for (OrderItem orderItem : orderItemsList) {
                        if (orderItem.getName().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(orderItem);
                        }
                    }
                    orderItemsListFiltered = filteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = orderItemsListFiltered;
                return filterResults;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                orderItemsListFiltered = (List<OrderItem>) filterResults.values;
                // refresh the list with filtered data
                notifyDataSetChanged();
            }
        };
    }

    public List<OrderItem> getOrderItemsList() {
        return orderItemsListFiltered;
    }

    public static class OrderItemViewHolder extends RecyclerView.ViewHolder {

        private final ImageView item_image;
        private final TextView item_name_text, item_description_text, quantity_text, price_text;
        private final ImageButton plus_image, minus_image;

        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            item_image = itemView.findViewById(R.id.item_image);
            item_name_text = itemView.findViewById(R.id.item_name_text);
            item_description_text = itemView.findViewById(R.id.item_description_text);
            quantity_text = itemView.findViewById(R.id.quantity_text);
            price_text = itemView.findViewById(R.id.price_text);
            plus_image = itemView.findViewById(R.id.plus_image);
            minus_image = itemView.findViewById(R.id.minus_image);
        }
    }
}
