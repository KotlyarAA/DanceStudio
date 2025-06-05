package com.example.dancestudio;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    public ProfileFragment() {
        super(R.layout.fragment_profile); // Указываем макет фрагмента
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Получаем ссылки на элементы интерфейса
        EditText emailField = view.findViewById(R.id.etUserEmail);
        Button logoutButton = view.findViewById(R.id.btnLogout);

        // Получаем текущего пользователя из Firebase
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Отображаем email пользователя
            emailField.setText(currentUser.getEmail());
        }

        // Обработка кнопки выхода
        logoutButton.setOnClickListener(v -> {
            // Выход из аккаунта Firebase
            FirebaseAuth.getInstance().signOut();

            // Скрыть нижнее меню
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).showBottomNav(false);
            }

            // Перейти на экран логина
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new LoginFragment())
                    .commit();
        });
    }
}
