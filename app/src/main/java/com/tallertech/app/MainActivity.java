package com.tallertech.app;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tallertech.app.ui.clients.ClientsFragment;
import com.tallertech.app.ui.equipment.EquipmentFragment;
import com.tallertech.app.ui.home.HomeFragment;
import com.tallertech.app.ui.orders.OrdersFragment;
import com.tallertech.app.ui.profile.ProfileFragment;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (com.tallertech.app.data.repository.AuthRepository.getCurrentUser() == null) {
            startActivity(new android.content.Intent(this, LoginActivity.class)
                    .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
            return;
        }

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main);
        com.tallertech.app.data.repository.AuthRepository.expiredSession.observe(this, expired -> {
            if (Boolean.TRUE.equals(expired)) {
                startActivity(new android.content.Intent(this, LoginActivity.class)
                        .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
            }
        });

        //Obtener la barra de navegación del XML
        bottomNavigation = findViewById(R.id.bottomNavigation);

        //Mostrar Inicio solamente cuando la Activity se crea por primera vez
        if (savedInstanceState == null) {
            cambiarFragment(new HomeFragment());
        }

        //Detectar qué opción toca el usuario
        bottomNavigation.setOnItemSelectedListener(item -> {

            Fragment fragmentSeleccionado;

            int id = item.getItemId();

            if (id == R.id.nav_home) {

                fragmentSeleccionado = new HomeFragment();

            } else if (id == R.id.nav_orders) {

                fragmentSeleccionado = new OrdersFragment();

            } else if (id == R.id.nav_clients) {

                fragmentSeleccionado = new ClientsFragment();

            } else if (id == R.id.nav_equipment) {

                fragmentSeleccionado = new EquipmentFragment();

            } else if (id == R.id.nav_profile) {

                fragmentSeleccionado = new ProfileFragment();

            } else {

                return false;
            }

            cambiarFragment(fragmentSeleccionado);

            return true;
        });

        //Ajustar la interfaz a las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {

                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );
    }

    @Override protected void onResume() {
        super.onResume();
        if (com.tallertech.app.data.repository.AuthRepository.getCurrentUser() != null)
            com.tallertech.app.data.sync.SyncManager.request(this);
    }
    private void cambiarFragment(Fragment fragment) {

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
