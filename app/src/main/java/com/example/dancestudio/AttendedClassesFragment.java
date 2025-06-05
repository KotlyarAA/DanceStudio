package com.example.dancestudio;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class AttendedClassesFragment extends Fragment {

    // Конструктор фрагмента, указываем layout XML
    public AttendedClassesFragment() {
        super(R.layout.fragment_attended_classes);
    }

    // Список посещённых занятий (объекты DanceClass)
    private final List<DanceClass> attendedList = new ArrayList<>();

    // Метод вызывается после создания представления фрагмента
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Находим RecyclerView из макета
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewAttended);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext())); // вертикальный список

        // Получаем текущего пользователя через Firebase
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return; // если пользователь не авторизован — выходим

        // Обращаемся к Firestore, получаем коллекцию посещённых занятий текущего пользователя
        FirebaseFirestore.getInstance()
                .collection("users")                // основная коллекция пользователей
                .document(user.getUid())           // документ по ID текущего пользователя
                .collection("attended")            // вложенная коллекция посещённых занятий
                .get()                             // получаем все документы
                .addOnSuccessListener(query -> {   // обработка успешного получения
                    attendedList.clear();          // очищаем список перед обновлением
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        // Преобразуем каждый документ в объект DanceClass
                        DanceClass danceClass = doc.toObject(DanceClass.class);
                        if (danceClass != null) {
                            attendedList.add(danceClass); // добавляем в список
                        }
                    }
                    // Устанавливаем адаптер для RecyclerView с обновлённым списком
                    recyclerView.setAdapter(new AttendedAdapter(attendedList));
                });
    }

    // Адаптер для отображения списка посещённых занятий
    static class AttendedAdapter extends RecyclerView.Adapter<AttendedAdapter.ViewHolder> {
        private final List<DanceClass> list;

        // Конструктор адаптера, принимает список занятий
        AttendedAdapter(List<DanceClass> list) {
            this.list = list;
        }

        // Создание нового ViewHolder (отображение одного элемента)
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // "Раздуваем" XML-разметку для элемента списка
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_dance_class, parent, false);
            return new ViewHolder(view);
        }

        // Привязка данных к элементу списка (вызывается для каждого элемента)
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DanceClass item = list.get(position); // получаем объект по позиции

            // Устанавливаем текстовые данные занятия
            holder.direction.setText(item.getDirection());
            holder.level.setText(item.getLevel());
            holder.trainer.setText(item.getTrainer());
            holder.date.setText(item.getDate());
            holder.time.setText(item.getTime());

            // Настраиваем кнопку как "Посещено" и делаем её неактивной
            holder.registerButton.setText("Посещено");
            holder.registerButton.setEnabled(false);
        }

        // Возвращает количество элементов в списке
        @Override
        public int getItemCount() {
            return list.size();
        }

        // ViewHolder — класс, хранящий ссылки на виджеты внутри одного элемента списка
        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView direction, level, trainer, date, time;
            Button registerButton;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                // Инициализация всех текстовых полей и кнопки
                direction = itemView.findViewById(R.id.classDirection);
                level = itemView.findViewById(R.id.classLevel);
                trainer = itemView.findViewById(R.id.classTrainer);
                date = itemView.findViewById(R.id.classDate);
                time = itemView.findViewById(R.id.classTime);
                registerButton = itemView.findViewById(R.id.btnRegisterClass);
            }
        }
    }
}
