package com.tallertech.app.ui.orders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tallertech.app.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderAdapter
        extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private final List<OrderUiModel> orders;
    private final List<OrderUiModel> filteredOrders;

    private final OnOrderClickListener listener;

    private String currentQuery = "";
    private String currentStatus = "Todas";

    // Comunica qué orden fue seleccionada.
    public interface OnOrderClickListener {
        void onOrderClick(OrderUiModel order);
    }

    public OrderAdapter(
            List<OrderUiModel> orders,
            OnOrderClickListener listener) {

        this.orders = new ArrayList<>(orders);
        this.filteredOrders = new ArrayList<>(orders);
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(
                        R.layout.item_order,
                        parent,
                        false
                );

        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull OrderViewHolder holder,
            int position) {

        OrderUiModel order =
                filteredOrders.get(position);

        holder.tvCode.setText(
                order.getCode()
        );

        holder.tvStatus.setText(
                order.getStatus()
        );

        holder.tvEquipment.setText(
                order.getEquipment()
        );

        holder.tvClient.setText(
                order.getClient()
        );

        holder.tvIssue.setText(
                order.getIssue()
        );

        holder.tvInfo.setText(
                order.getDate()
                        + " · "
                        + order.getTechnician()
        );

        // Abrir la orden seleccionada.
        holder.itemView.setOnClickListener(v ->
                listener.onOrderClick(order)
        );
    }

    @Override
    public int getItemCount() {
        return filteredOrders.size();
    }

    public void filtrarTexto(String query) {

        currentQuery =
                query.trim()
                        .toLowerCase(Locale.ROOT);

        aplicarFiltros();
    }

    public void filtrarEstado(String status) {

        currentStatus = status;

        aplicarFiltros();
    }

    public void setOrders(List<OrderUiModel> items) {
        orders.clear();
        orders.addAll(items);
        aplicarFiltros();
    }

    private void aplicarFiltros() {

        filteredOrders.clear();

        for (OrderUiModel order : orders) {

            String searchable =
                    order.getCode() + " " +
                            order.getClient() + " " +
                            order.getEquipment() + " " +
                            order.getIssue() + " " +
                            order.getTechnician();

            boolean coincideTexto =
                    currentQuery.isEmpty()
                            || searchable
                            .toLowerCase(Locale.ROOT)
                            .contains(currentQuery);

            boolean coincideEstado =
                    currentStatus.equals("Todas")
                            || order.getStatus()
                            .equalsIgnoreCase(currentStatus);

            if (coincideTexto
                    && coincideEstado) {

                filteredOrders.add(order);
            }
        }

        notifyDataSetChanged();
    }

    static class OrderViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvCode;
        TextView tvStatus;
        TextView tvEquipment;
        TextView tvClient;
        TextView tvIssue;
        TextView tvInfo;

        public OrderViewHolder(
                @NonNull View itemView) {

            super(itemView);

            tvCode =
                    itemView.findViewById(
                            R.id.tvOrderCode
                    );

            tvStatus =
                    itemView.findViewById(
                            R.id.tvOrderStatus
                    );

            tvEquipment =
                    itemView.findViewById(
                            R.id.tvOrderEquipment
                    );

            tvClient =
                    itemView.findViewById(
                            R.id.tvOrderClient
                    );

            tvIssue =
                    itemView.findViewById(
                            R.id.tvOrderIssue
                    );

            tvInfo =
                    itemView.findViewById(
                            R.id.tvOrderInfo
                    );
        }
    }
}
