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
        if (user != null) ((TextView) view.findViewById(R.id.sessionUser)).setText(user.name + "\n" + user.email + "\n" + user.role);
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
}
