package com.tallertech.app.ui.clients;

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

public class ClientAdapter
        extends RecyclerView.Adapter<ClientAdapter.ClientViewHolder> {

    private final List<ClientUiModel> clients;
    private final List<ClientUiModel> filteredClients;

    private final OnClientClickListener listener;

    // Interfaz para detectar qué cliente fue seleccionado.
    public interface OnClientClickListener {
        void onClientClick(ClientUiModel client);
    }

    public ClientAdapter(
            List<ClientUiModel> clients,
            OnClientClickListener listener) {

        this.clients = new ArrayList<>(clients);
        this.filteredClients = new ArrayList<>(clients);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ClientViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        // Crea una fila usando item_client.xml.
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_client, parent, false);

        return new ClientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ClientViewHolder holder,
            int position) {

        ClientUiModel client = filteredClients.get(position);

        holder.tvInitials.setText(client.getInitials());
        holder.tvName.setText(client.getName());
        holder.tvContact.setText(client.getContact());
        holder.tvEquipment.setText(client.getEquipment());

        // Notifica al Fragment qué cliente fue seleccionado.
        holder.itemView.setOnClickListener(v ->
                listener.onClientClick(client)
        );
    }

    public void setClients(
            List<ClientUiModel> newClients) {

        clients.clear();
        clients.addAll(newClients);

        filteredClients.clear();
        filteredClients.addAll(newClients);

        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return filteredClients.size();
    }

    public void filtrar(String texto) {

        filteredClients.clear();

        String query = texto
                .trim()
                .toLowerCase(Locale.ROOT);

        // Si el buscador está vacío, mostrar todos.
        if (query.isEmpty()) {

            filteredClients.addAll(clients);

        } else {

            for (ClientUiModel client : clients) {

                String name = client.getName()
                        .toLowerCase(Locale.ROOT);

                String contact = client.getContact()
                        .toLowerCase(Locale.ROOT);

                // Buscar por nombre, teléfono o correo.
                if (name.contains(query)
                        || contact.contains(query)) {

                    filteredClients.add(client);
                }
            }
        }

        notifyDataSetChanged();
    }

    static class ClientViewHolder extends RecyclerView.ViewHolder {

        TextView tvInitials;
        TextView tvName;
        TextView tvContact;
        TextView tvEquipment;

        public ClientViewHolder(@NonNull View itemView) {
            super(itemView);

            // Referencias a los elementos de una fila.
            tvInitials = itemView.findViewById(R.id.tvClientInitials);
            tvName = itemView.findViewById(R.id.tvClientName);
            tvContact = itemView.findViewById(R.id.tvClientContact);
            tvEquipment = itemView.findViewById(R.id.tvClientEquipment);
        }
    }
}