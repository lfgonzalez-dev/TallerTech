package com.tallertech.app.ui.clients;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.tallertech.app.R;
import com.tallertech.app.data.local.entity.ClientEntity;
import com.tallertech.app.data.repository.ClientRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClientsFragment extends Fragment {

    private RecyclerView rvClients;
    private ClientAdapter clientAdapter;

    private ClientRepository clientRepository;

    public ClientsFragment() {
        super(R.layout.fragments_clients);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        rvClients =
                view.findViewById(R.id.rvClients);

        TextInputEditText etSearchClient =
                view.findViewById(R.id.etSearchClient);

        ExtendedFloatingActionButton fabNewClient =
                view.findViewById(R.id.fabNewClient);

        // Acceso a Room mediante Repository.
        clientRepository =
                new ClientRepository(requireContext());

        configurarRecyclerView();

        // Buscar mientras el usuario escribe.
        etSearchClient.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count) {

                        clientAdapter
                                .filtrar(s.toString());
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s) {
                    }
                }
        );

        fabNewClient.setOnClickListener(v ->
                abrirRegistroCliente()
        );
    }

    @Override
    public void onResume() {

        super.onResume();

        // Recargar después de regresar de Registrar cliente.
        cargarClientes();
    }

    private void configurarRecyclerView() {

        rvClients.setLayoutManager(
                new LinearLayoutManager(
                        requireContext()
                )
        );

        // Inicialmente la lista está vacía.
        clientAdapter =
                new ClientAdapter(
                        new ArrayList<>(),
                        this::abrirDetalleCliente
                );

        rvClients.setAdapter(clientAdapter);
    }

    private void cargarClientes() {

        clientRepository.getAllWithEquipmentCounts(
                new ClientRepository.ClientsWithCountsCallback() {

                    @Override
                    public void onResult(
                            List<ClientEntity> clients, Map<String, Integer> counts) {
                        if (!isAdded() || getView() == null) return;

                        List<ClientUiModel> uiClients =
                                new ArrayList<>();

                        for (ClientEntity client : clients) {

                            uiClients.add(
                                    convertirAUiModel(client, counts.get(client.getId()))
                            );
                        }

                        clientAdapter.setClients(
                                uiClients
                        );
                    }

                    @Override
                    public void onError(Exception e) {
                        if (!isAdded() || getView() == null) return;

                        Toast.makeText(
                                requireContext(),
                                "Error al cargar clientes",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private ClientUiModel convertirAUiModel(
            ClientEntity client, int count) {

        String initials =
                obtenerIniciales(
                        client.getName()
                );

        return new ClientUiModel(
                initials,
                client.getName(),
                client.getPhone(),
                client.getEmail(),
                client.getAddress(),
                client.getNotes(),
                count + (count == 1 ? " equipo" : " equipos")
        );
    }

    private String obtenerIniciales(
            String name) {

        if (name == null
                || name.trim().isEmpty()) {

            return "CL";
        }

        String[] parts =
                name.trim().split("\\s+");

        if (parts.length == 1) {

            return parts[0]
                    .substring(0, 1)
                    .toUpperCase();
        }

        return (
                parts[0].substring(0, 1)
                        + parts[1].substring(0, 1)
        ).toUpperCase();
    }

    private void abrirDetalleCliente(
            ClientUiModel client) {

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragmentContainer,
                        ClientDetailFragment
                                .newInstance(client)
                )
                .addToBackStack(null)
                .commit();
    }

    private void abrirRegistroCliente() {

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragmentContainer,
                        new RegisterClientFragment()
                )
                .addToBackStack(null)
                .commit();
    }
}