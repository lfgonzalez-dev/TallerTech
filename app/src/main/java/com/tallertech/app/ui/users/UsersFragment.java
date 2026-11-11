package com.tallertech.app.ui.users;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.tallertech.app.R;
import com.tallertech.app.data.remote.ApiErrors;
import com.tallertech.app.data.remote.dto.auth.*;
import com.tallertech.app.data.repository.*;
import com.tallertech.app.ui.common.RecordAdapter;
import java.util.*;

public final class UsersFragment extends Fragment {
    private RecordAdapter adapter;
    private boolean busy;
    public UsersFragment() { super(R.layout.fragment_records); }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);
        ((TextView) view.findViewById(R.id.recordsTitle)).setText("Usuarios");
        ((TextView) view.findViewById(R.id.recordsSubtitle)).setText(R.string.users_subtitle);
        view.findViewById(R.id.recordsSummary).setVisibility(View.VISIBLE);
        view.findViewById(R.id.btnRecordsBack).setOnClickListener(v -> getParentFragmentManager().popBackStack());
        Button create = view.findViewById(R.id.btnRecordsAction); create.setText("Crear usuario");
        create.setVisibility(UserRepository.isAdmin(AuthRepository.getCurrentUser()) ? View.VISIBLE : View.GONE);
        create.setOnClickListener(v -> editor(null));
        adapter = new RecordAdapter();
        RecyclerView list = view.findViewById(R.id.recordsList);
        list.setLayoutManager(new LinearLayoutManager(requireContext())); list.setAdapter(adapter);
        load();
    }
    private String message(Exception error) {
        return error instanceof java.io.IOException ? ApiErrors.message(error) : error.getMessage();
    }
    private void load() {
        View view = getView();
        if (view == null) return;
        view.findViewById(R.id.btnRecordsAction).setVisibility(
                UserRepository.isAdmin(AuthRepository.getCurrentUser()) ? View.VISIBLE : View.GONE);
        ((TextView) view.findViewById(R.id.recordsStatus)).setText("Consultando servidor…");
        new UserRepository(requireContext()).list(new ResultCallback<List<UserDto>>() {
            @Override public void onResult(List<UserDto> users) {
                if (getView() != view) return;
                List<RecordAdapter.Row> rows = new ArrayList<>();
                for (UserDto user : users) rows.add(RecordAdapter.Row.user(
                        user.name, user.email, UserRepository.roleLabel(user.role), () -> detail(user.id)));
                adapter.submit(rows);
                ((TextView) view.findViewById(R.id.recordsCount)).setText(getString(R.string.users_count, users.size()));
                ((TextView) view.findViewById(R.id.recordsStatus)).setText(users.isEmpty() ? "Sin usuarios" : "Selecciona un usuario para consultar o editar");
            }
            @Override public void onError(Exception error) {
                if (getView() != view) return;
                ((TextView) view.findViewById(R.id.recordsStatus)).setText(message(error) + ". Este módulo requiere conexión.");
                view.findViewById(R.id.recordsStatus).setOnClickListener(v -> load());
            }
        });
    }
    private void detail(String id) {
        if (busy) return;
        busy = true; View view = getView();
        new UserRepository(requireContext()).get(id, new ResultCallback<UserDto>() {
            @Override public void onResult(UserDto user) { busy = false; if (getView() == view) editor(user); }
            @Override public void onError(Exception error) { busy = false; if (getView() == view) Toast.makeText(requireContext(), message(error), Toast.LENGTH_LONG).show(); }
        });
    }
    private void editor(UserDto user) {
        View fields = getLayoutInflater().inflate(R.layout.dialog_user, null);
        TextInputEditText name = fields.findViewById(R.id.userName), email = fields.findViewById(R.id.userEmail),
                password = fields.findViewById(R.id.userPassword);
        Spinner role = fields.findViewById(R.id.userRole);
        role.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, new String[]{"Técnico", "Administrador"}));
        if (user != null) {
            name.setText(user.name); email.setText(user.email);
            role.setSelection(UserRepository.isAdmin(user) ? 1 : 0);
            ((com.google.android.material.textfield.TextInputLayout) fields.findViewById(R.id.tilUserPassword))
                    .setHint("Nueva contraseña (opcional)");
        }
        var dialog = new MaterialAlertDialogBuilder(requireContext()).setTitle(user == null ? "Crear usuario" : "Editar usuario")
                .setView(fields).setNegativeButton("Cancelar", null).setPositiveButton("Guardar", null).create();
        dialog.setOnDismissListener(d -> password.setText(""));
        dialog.setOnShowListener(d -> dialog.getButton(-1).setOnClickListener(v -> {
            UserRequest request = new UserRequest();
            request.name = name.getText().toString().trim(); request.email = email.getText().toString().trim();
            request.role = role.getSelectedItemPosition() == 1 ? "ADMIN" : "TECNICO";
            String secret = password.getText().toString();
            if (request.name.isBlank()) { name.setError("Ingresa el nombre"); return; }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(request.email).matches()) { email.setError("Correo inválido"); return; }
            if (user == null || !secret.isEmpty()) {
                if (secret.length() < 12 || secret.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
                    password.setError("Usa al menos 12 caracteres y máximo 72 bytes"); return;
                }
                request.password = secret;
            }
            dialog.getButton(-1).setEnabled(false); dialog.getButton(-2).setEnabled(false); dialog.setCancelable(false);
            View view = getView();
            new UserRepository(requireContext()).save(user == null ? null : user.id, request, new ResultCallback<UserDto>() {
                @Override public void onResult(UserDto saved) {
                    request.password = null; password.setText(""); dialog.dismiss();
                    if (getView() == view) load();
                }
                @Override public void onError(Exception error) {
                    request.password = null; password.setText("");
                    dialog.getButton(-1).setEnabled(true); dialog.getButton(-2).setEnabled(true); dialog.setCancelable(true);
                    if (getView() == view) Toast.makeText(requireContext(), message(error), Toast.LENGTH_LONG).show();
                }
            });
        }));
        dialog.show();
    }
}
