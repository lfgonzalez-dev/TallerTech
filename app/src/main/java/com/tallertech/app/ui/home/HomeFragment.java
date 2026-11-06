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


public class HomeFragment  extends Fragment{
    public HomeFragment(){
        super(R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view,savedInstanceState);

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

}
