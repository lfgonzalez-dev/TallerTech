package com.tallertech.app.ui.clients;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.tallertech.app.R;
import com.tallertech.app.ui.orders.NewOrderFragment;
import com.tallertech.app.data.local.entity.ClientEntity;
import com.tallertech.app.data.local.OrderDetails;
import com.tallertech.app.data.repository.ClientRepository;
import com.tallertech.app.data.repository.ServiceOrderRepository;
import com.tallertech.app.data.repository.ResultCallback;

public class ClientDetailFragment extends Fragment {

    private static final String ARG_INITIALS = "initials";
    private static final String ARG_NAME = "name";
    private static final String ARG_PHONE = "phone";
    private static final String ARG_EMAIL = "email";
    private static final String ARG_ADDRESS = "address";
    private static final String ARG_NOTES = "notes";
    private static final String ARG_EQUIPMENT = "equipment";

    private String clientName;
    private String clientPhone;

    public ClientDetailFragment() {
        super(R.layout.fragment_client_detail);
    }

    // Recibe los datos del cliente seleccionado.
    public static ClientDetailFragment newInstance(ClientUiModel client) {

        ClientDetailFragment fragment = new ClientDetailFragment();

        Bundle args = new Bundle();
        args.putString("id", client.getId());

        args.putString(ARG_INITIALS, client.getInitials());
        args.putString(ARG_NAME, client.getName());
        args.putString(ARG_PHONE, client.getPhone());
        args.putString(ARG_EMAIL, client.getEmail());
        args.putString(ARG_ADDRESS, client.getAddress());
        args.putString(ARG_NOTES, client.getNotes());
        args.putString(ARG_EQUIPMENT, client.getEquipment());

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        // Elementos de la interfaz.
        TextView tvInitials =
                view.findViewById(R.id.tvDetailClientInitials);

        TextView tvName =
                view.findViewById(R.id.tvDetailClientName);

        TextView tvContact =
                view.findViewById(R.id.tvDetailClientContact);

        TextView tvEquipment =
                view.findViewById(R.id.tvDetailClientEquipment);

        MaterialButton btnBack =
                view.findViewById(R.id.btnBackClientDetail);

        MaterialButton btnEdit =
                view.findViewById(R.id.btnEditClient);

        MaterialButton btnCall =
                view.findViewById(R.id.btnCallClient);

        MaterialButton btnNewOrder =
                view.findViewById(R.id.btnNewOrderClient);

        // Recuperamos los datos recibidos.
        Bundle args = getArguments();

        if (args != null) {

            String initials =
                    args.getString(ARG_INITIALS, "");

            clientName =
                    args.getString(ARG_NAME, "");

            clientPhone =
                    args.getString(ARG_PHONE, "");

            String email =
                    args.getString(ARG_EMAIL, "");

            String equipment =
                    args.getString(ARG_EQUIPMENT, "");

            tvInitials.setText(initials);
            tvName.setText(clientName);
            tvContact.setText(clientPhone + " · " + email);
            tvEquipment.setText(equipment);
        }

        // Regresar a la lista de clientes.
        btnBack.setOnClickListener(v ->
                requireActivity()
                        .getSupportFragmentManager()
                        .popBackStack()
        );

        // Abrir formulario de edición.
        btnEdit.setOnClickListener(v ->
                abrirEditarCliente()
        );

        // Abrir marcador telefónico.
        btnCall.setOnClickListener(v ->
                llamarCliente()
        );

        // Crear una nueva orden desde este cliente.
        btnNewOrder.setOnClickListener(v ->
                abrirNuevaOrden()
        );
    }

    private void abrirEditarCliente() {

        Bundle args = getArguments();

        if (args == null) {
            return;
        }

        EditClientFragment fragment =
                EditClientFragment.newInstance(
                        args.getString("id")
                );

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragmentContainer,
                        fragment
                )
                .addToBackStack(null)
                .commit();
    }

    private void llamarCliente() {

        if (clientPhone == null || clientPhone.isEmpty()) {

            Toast.makeText(
                    requireContext(),
                    "El cliente no tiene teléfono registrado",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // ACTION_DIAL abre el marcador sin realizar la llamada automáticamente.
        Intent intent = new Intent(Intent.ACTION_DIAL);

        intent.setData(
                Uri.parse("tel:" + clientPhone)
        );

        startActivity(intent);
    }

    @Override public void onResume() {
        super.onResume();
        View view = getView();
        if (view == null) return;
        ClientRepository repository = new ClientRepository(requireContext());
        String id = requireArguments().getString("id");
        repository.getById(id, new ResultCallback<ClientEntity>() {
            @Override public void onResult(ClientEntity client) {
                if (!isAdded() || getView() != view) return;
                clientName = client.getName();
                clientPhone = client.getPhone();
                ((TextView) view.findViewById(R.id.tvDetailClientName)).setText(clientName);
                ((TextView) view.findViewById(R.id.tvDetailClientInitials)).setText(
                        clientName == null || clientName.isEmpty() ? "CL" : clientName.substring(0, 1).toUpperCase());
                ((TextView) view.findViewById(R.id.tvDetailClientContact)).setText(clientPhone + " · " + client.getEmail());
            }
            @Override public void onError(Exception e) {
                if (!isAdded() || getView() != view) return;
                Toast.makeText(requireContext(), "Error al cargar cliente", Toast.LENGTH_SHORT).show();
            }
        });
        repository.countEquipment(id, new ResultCallback<Integer>() {
            @Override public void onResult(Integer count) {
                if (getView() != view) return;
                ((TextView) view.findViewById(R.id.tvDetailClientEquipment)).setText(count + (count == 1 ? " equipo" : " equipos"));
            }
            @Override public void onError(Exception e) {
                if (getView() == view) ((TextView) view.findViewById(R.id.tvDetailClientEquipment)).setText("—");
            }
        });
        new ServiceOrderRepository(requireContext()).getLatestByClient(id, new ResultCallback<OrderDetails>() {
            @Override public void onResult(OrderDetails result) {
                if (getView() != view) return;
                ((TextView) view.findViewById(R.id.tvClientRecentOrder)).setText(result == null ? "Sin órdenes" : result.order.getCode());
                ((TextView) view.findViewById(R.id.tvClientRecentEquipment)).setText(result == null ? "" : result.equipmentName + " · " + result.order.getStatus());
            }
            @Override public void onError(Exception e) {
                if (getView() == view) ((TextView) view.findViewById(R.id.tvClientRecentOrder)).setText("Error al cargar actividad");
            }
        });
    }

    private void abrirNuevaOrden() {

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragmentContainer,
                        new NewOrderFragment()
                )
                .addToBackStack(null)
                .commit();
    }
}
