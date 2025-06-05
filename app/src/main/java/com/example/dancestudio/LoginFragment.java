package com.example.dancestudio;

// Импорты для работы с интерфейсом и Firebase
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

// Фрагмент для входа в аккаунт (Login)
public class LoginFragment extends Fragment {

    // Конструктор, привязывающий фрагмент к XML-макету fragment_login.xml
    public LoginFragment() {
        super(R.layout.fragment_login);
    }

    // Метод вызывается после создания представления (view уже загружен из XML)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        // Получаем ссылки на поля ввода и кнопки из макета
        EditText emailField = view.findViewById(R.id.etEmail);        // Поле ввода email
        EditText passwordField = view.findViewById(R.id.etPassword);  // Поле ввода пароля
        Button btnLogin = view.findViewById(R.id.btnLogin);           // Кнопка "Войти"
        TextView tvRegister = view.findViewById(R.id.tvRegister);     // Надпись "Зарегистрироваться"

        // Получаем экземпляр Firebase Authentication
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        // Обработка нажатия кнопки "Войти"
        btnLogin.setOnClickListener(v -> {
            // Считываем введённые данные
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            // Проверка: если одно из полей пустое — показываем ошибку
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Введите email и пароль", Toast.LENGTH_SHORT).show();
                return;
            }

            // Выполняем вход в Firebase с email и паролем
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Успешный вход

                            // Показываем нижнее меню (например, если оно скрыто на экране входа)
                            ((MainActivity) requireActivity()).showBottomNav(true);

                            // Заменяем текущий фрагмент на фрагмент профиля
                            requireActivity().getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.fragment_container, new ScheduleFragment())
                                    .commit();
                        } else {
                            // Если вход не удался — выводим ошибку
                            Toast.makeText(getContext(), "Ошибка входа: " +
                                    task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Обработка нажатия на "Зарегистрироваться"
        tvRegister.setOnClickListener(v -> {
            // Переход на RegisterFragment (регистрация нового пользователя)
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new RegisterFragment()) // Меняем фрагмент
                    .addToBackStack(null) // Добавляем в back stack, чтобы можно было вернуться назад
                    .commit();
        });
    }
}
