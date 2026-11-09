package com.tallertech.app.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.tallertech.app.LoginActivity;
import com.tallertech.app.R;
import com.tallertech.app.data.repository.AuthRepository;

public class ProfileFragment extends Fragment {
    public ProfileFragment() { super(R.layout.fragment_profile); }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);
        var user = AuthRepository.getCurrentUser();
        if (user != null) {
            ((TextView) view.findViewById(R.id.sessionUser)).setText(user.name);
            ((TextView) view.findViewById(R.id.profileEmail)).setText(user.email);
            ((TextView) view.findViewById(R.id.profileRole)).setText(com.tallertech.app.data.repository.UserRepository.roleLabel(user.role));
            StringBuilder initials = new StringBuilder();
            for (String part : user.name.trim().split("\\s+")) {
                if (!part.isEmpty() && initials.length() < 2) initials.append(part.substring(0, 1));
            }
            ((TextView) view.findViewById(R.id.profileInitials)).setText(initials.toString().toUpperCase(java.util.Locale.ROOT));
        }
        view.findViewById(R.id.btnProfileSync).setOnClickListener(v -> open(new com.tallertech.app.ui.sync.SyncFragment()));
        view.findViewById(R.id.btnProfileHistory).setOnClickListener(v -> open(new com.tallertech.app.ui.history.HistoryFragment()));
        View users = view.findViewById(R.id.btnProfileUsers);
        users.setVisibility(com.tallertech.app.data.repository.UserRepository.isAdmin(user) ? View.VISIBLE : View.GONE);
        users.setOnClickListener(v -> open(new com.tallertech.app.ui.users.UsersFragment()));
        view.findViewById(R.id.btnLogout).setOnClickListener(v -> {
            v.setEnabled(false);
            android.app.Activity activity = requireActivity();
            new AuthRepository(activity).logout(() -> {
                if (activity.isDestroyed()) return;
                activity.startActivity(new Intent(activity, LoginActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)); activity.finish();
            });
        });
    }
    private void open(Fragment fragment) {
        getParentFragmentManager().beginTransaction().replace(R.id.fragmentContainer, fragment).addToBackStack(null).commit();
    }
}
