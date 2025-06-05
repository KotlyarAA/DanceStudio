package com.example.dancestudio;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

public class RegisterFragment extends Fragment {

    // Конструктор, указываем layout-файл фрагмента регистрации
    public RegisterFragment() {
        super(R.layout.fragment_register);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Получаем ссылки на элементы пользовательского интерфейса
        EditText emailField = view.findViewById(R.id.etRegisterEmail);
        EditText passwordField = view.findViewById(R.id.etRegisterPassword);
        Button btnRegister = view.findViewById(R.id.btnRegister);
        TextView tvToLogin = view.findViewById(R.id.tvToLogin);

        // Получаем экземпляр FirebaseAuth для регистрации
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        // Обработка нажатия кнопки "Зарегистрироваться"
        btnRegister.setOnClickListener(v -> {
            // Получаем введённые данные
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            // Проверка: оба поля должны быть заполнены
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(getContext(), "Заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }

            // Создание нового пользователя с помощью Firebase
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Успешная регистрация
                            Toast.makeText(getContext(), "Регистрация успешна", Toast.LENGTH_SHORT).show();

                            // Переход на экран логина
                            requireActivity().getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.fragment_container, new LoginFragment())
                                    .commit();
                        } else {
                            // Ошибка регистрации
                            Toast.makeText(getContext(),
                                    "Ошибка: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // Обработка нажатия на текст "Уже есть аккаунт? Войти"
        tvToLogin.setOnClickListener(v -> {
            // Переход на экран логина
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new LoginFragment())
                    .commit();
        });
    }
}
