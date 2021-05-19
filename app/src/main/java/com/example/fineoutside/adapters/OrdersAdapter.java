package com.example.fineoutside.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fineoutside.R;
import com.example.fineoutside.data.Order;
import com.example.fineoutside.data.OrderItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrdersViewHolder> implements Filterable {

    private final boolean fromBusinessProfile;
    private final CloseOrderListener closeOrderListener;

    private final List<Order> orderList;
    private List<Order> orderListFiltered;

    public OrdersAdapter(List<Order> orderList, boolean fromBusinessProfile, CloseOrderListener closeOrderListener) {
        this.orderList = orderList;
        this.orderListFiltered = orderList;
        this.fromBusinessProfile = fromBusinessProfile;
        this.closeOrderListener = closeOrderListener;
    }

    @NonNull
    @Override
    public OrdersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new OrdersViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_order_cell, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull OrdersAdapter.OrdersViewHolder holder, int position) {
        Order order = orderListFiltered.get(position);
        holder.order_id_text.setText(new StringBuilder("Order #" + order.getOrder_id().substring(order.getOrder_id().length() - 5)));
        StringBuilder orderList = new StringBuilder();
        double sum = 0;
        for (OrderItem orderItem : order.getOrderItemList()) {
            if (orderItem.getQuantity() > 0) {
                orderList.append(orderItem.getName()).append(" X ").append(orderItem.getQuantity()).append("\n");
                sum += orderItem.getQuantity() * orderItem.getPrice();
            }
        }
        holder.order_list_text.setText(new StringBuilder(orderList + "\nTotal: " + sum + " ILS"));
        holder.order_time_text.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(order.getTime()));
        holder.distance_text.setText(new StringBuilder(order.getDistance() + " meters from you"));
        holder.phone_number_text.setVisibility(fromBusinessProfile ? View.VISIBLE : View.GONE);
        holder.phone_number_text.setText(order.getPhoneNumber());
        holder.close_order_button.setVisibility(fromBusinessProfile ? View.VISIBLE : View.GONE);
        holder.close_order_button.setOnClickListener(v -> {
            if (closeOrderListener != null) {
                closeOrderListener.onCloseOrder(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderListFiltered.size();
    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    orderListFiltered = orderList;
                } else {
                    List<Order> filteredList = new ArrayList<>();
                    for (Order order : orderList) {
                        for (OrderItem orderItem : order.getOrderItemList()) {
                            if (orderItem.getQuantity() > 0 && orderItem.getName().toLowerCase().contains(charString.toLowerCase())) {
                                filteredList.add(order);
                            }
                        }
                    }
                    orderListFiltered = filteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = orderListFiltered;
                return filterResults;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                orderListFiltered = (ArrayList<Order>) filterResults.values;
                // refresh the list with filtered data
                notifyDataSetChanged();
            }
        };
    }

    public interface CloseOrderListener {
        void onCloseOrder(Order order);
    }

    public static class OrdersViewHolder extends RecyclerView.ViewHolder {

        private final TextView order_id_text, order_list_text, order_time_text, distance_text, phone_number_text;
        private final Button close_order_button;

        public OrdersViewHolder(@NonNull View itemView) {
            super(itemView);
            order_id_text = itemView.findViewById(R.id.order_id_text);
            order_list_text = itemView.findViewById(R.id.order_list_text);
            order_time_text = itemView.findViewById(R.id.order_time_text);
            distance_text = itemView.findViewById(R.id.distance_text);
            phone_number_text = itemView.findViewById(R.id.phone_number_text);
            close_order_button = itemView.findViewById(R.id.close_order_button);
        }
    }
}
