package com.tallertech.app.ui.home;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.tallertech.app.R;
import com.tallertech.app.ui.clients.RegisterClientFragment;
import com.tallertech.app.ui.orders.NewOrderFragment;
import com.tallertech.app.data.repository.StatisticsRepository;
import com.tallertech.app.data.repository.ResultCallback;
import android.widget.TextView;
import android.widget.Toast;


public class HomeFragment  extends Fragment{
    public HomeFragment(){
        super(R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        com.tallertech.app.data.local.LocalDataObserver.observe(this, this::reloadLocalData);

        //Obtenemos los botones del layout del fragment
        MaterialButton btnNewOrder =
                view.findViewById(R.id.btnNewOrder);

        MaterialButton btnNewClient =
                view.findViewById(R.id.btnNewClient);

        //Abrir formuluario de nueva orden
        btnNewOrder.setOnClickListener(v ->
                abrirFragment(new NewOrderFragment())
        );

        //Abrir formulario de registro cliente
        btnNewClient.setOnClickListener(v ->
                abrirFragment(new RegisterClientFragment())
        );
    }

    private void abrirFragment(Fragment fragment) {

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void reloadLocalData() {
        View view = getView();
        if (view == null) return;
        new StatisticsRepository(requireContext()).load(new ResultCallback<StatisticsRepository.Statistics>() {
            @Override public void onResult(StatisticsRepository.Statistics stats) {
                if (!isAdded() || getView() != view) return;
                ((TextView) view.findViewById(R.id.tvActiveOrders)).setText(String.valueOf(stats.activeOrders));
                ((TextView) view.findViewById(R.id.tvEquipmentCount)).setText(String.valueOf(stats.activeEquipment));
                ((TextView) view.findViewById(R.id.tvHomeRecentOrder)).setText(stats.latestOrder == null ? "Sin órdenes" : stats.latestOrder.order.getCode());
                ((TextView) view.findViewById(R.id.tvHomeRecentEquipment)).setText(stats.latestOrder == null ? "" :
                        stats.latestOrder.equipmentName + " · " + stats.latestOrder.order.getStatus());
            }
            @Override public void onError(Exception e) {
                if (!isAdded() || getView() != view) return;
                Toast.makeText(requireContext(), "Error al cargar estadísticas", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
