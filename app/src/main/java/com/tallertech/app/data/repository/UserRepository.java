package com.tallertech.app.data.repository;

import android.content.Context;
import com.tallertech.app.data.remote.*;
import com.tallertech.app.data.remote.api.TallerTechApi;
import com.tallertech.app.data.remote.dto.auth.*;
import java.util.*;
import retrofit2.Response;

public final class UserRepository {
    private final TallerTechApi api;
    private final com.tallertech.app.data.local.TallerTechDatabase database;
    public UserRepository(Context context) {
        api = RetrofitClient.getApi(context);
        database = com.tallertech.app.data.local.TallerTechDatabase.getInstance(context);
    }
    public static boolean isAdmin(UserDto user) {
        return user != null && ("ADMIN".equalsIgnoreCase(user.role) || "Administrador".equalsIgnoreCase(user.role));
    }
    public static String roleLabel(String role) {
        if ("ADMIN".equalsIgnoreCase(role) || "Administrador".equalsIgnoreCase(role)) return "Administrador";
        return "Técnico";
    }
    private void requireAdmin() {
        if (!isAdmin(AuthRepository.getCurrentUser())) throw new IllegalStateException("Solo un administrador puede gestionar usuarios");
    }
    private <T> T read(Response<T> response) throws Exception {
        try {
            if (response.code() == 401) AuthRepository.invalidateSession();
            if (!response.isSuccessful() || response.body() == null) throw new IllegalStateException(ApiErrors.message(response));
            return response.body();
        } finally { if (response.errorBody() != null) response.errorBody().close(); }
    }
    public void list(ResultCallback<List<UserDto>> callback) {
        RepositoryExecutor.run(() -> { requireAdmin(); return read(api.getUsers().execute()); }, callback);
    }
    public void get(String id, ResultCallback<UserDto> callback) {
        RepositoryExecutor.run(() -> { requireAdmin(); return read(api.getUser(id).execute()); }, callback);
    }
    public void save(String id, UserRequest request, ResultCallback<UserDto> callback) {
        RepositoryExecutor.run(() -> {
            requireAdmin();
            UserDto saved = read(id == null ? api.createUser(request).execute() : api.updateUser(id, request).execute());
            UserDto current = AuthRepository.getCurrentUser();
            if (current != null && current.id.equals(saved.id)) {
                database.userDao().insert(new com.tallertech.app.data.local.entity.UserEntity(
                        saved.id, saved.name, saved.email, saved.role, saved.updatedAt));
                AuthRepository.updateCurrentUser(saved);
            }
            return saved;
        }, callback);
    }
}
