package com.example.dancestudio;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.text.SimpleDateFormat;
import java.util.*;

// Фрагмент, отображающий список занятий, на которые пользователь записался
public class MyRegistrationsFragment extends Fragment {

    // Конструктор, задающий layout фрагмента
    public MyRegistrationsFragment() {
        super(R.layout.fragment_my_registrations);
    }

    // Переменные для Firestore и текущего пользователя
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    // Список классов, на которые пользователь записан
    private final List<DanceClass> myList = new ArrayList<>();

    // Метод вызывается после создания представления фрагмента
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Настройка RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewMyClasses);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance(); // Получаем экземпляр базы данных
        currentUser = FirebaseAuth.getInstance().getCurrentUser(); // Получаем текущего пользователя
        if (currentUser == null) return; // Если пользователь не авторизован — выход

        String userId = currentUser.getUid(); // UID пользователя

        // Ссылка на коллекцию "registrations" в Firestore
        CollectionReference regRef = db
                .collection("users")
                .document(userId)
                .collection("registrations");

        // Получаем все документы из этой коллекции
        regRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            myList.clear(); // Очищаем список перед обновлением
            WriteBatch batch = db.batch(); // Используем батч-запрос Firestore для атомарных операций

            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                DanceClass danceClass = doc.toObject(DanceClass.class); // Преобразуем документ в объект DanceClass
                if (danceClass == null) continue;

                if (isClassInPast(danceClass)) {
                    // Если занятие прошло, переносим его в коллекцию "attended"
                    DocumentReference attendedRef = db
                            .collection("users")
                            .document(currentUser.getUid())
                            .collection("attended")
                            .document(danceClass.getId());

                    batch.set(attendedRef, danceClass); // добавляем в "attended"
                    batch.delete(doc.getReference()); // удаляем из "registrations"
                } else {
                    // Иначе добавляем в список отображаемых занятий
                    myList.add(danceClass);
                }
            }

            // Применяем батч (выполнение всех операций сразу)
            batch.commit().addOnCompleteListener(task -> {
                // Обновляем адаптер RecyclerView
                recyclerView.setAdapter(new MyAdapter(myList));
            });
        });
    }

    // Метод проверяет, прошло ли занятие
    private boolean isClassInPast(DanceClass danceClass) {
        try {
            String dateTime = danceClass.getDate() + " " + danceClass.getTime(); // Объединяем дату и время
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date classDate = format.parse(dateTime); // Преобразуем строку в объект Date
            return classDate != null && System.currentTimeMillis() > classDate.getTime(); // Сравниваем с текущим временем
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Если произошла ошибка — считаем, что занятие не прошло
        }
    }

    // Адаптер для списка записей пользователя
    static class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private final List<DanceClass> list;

        MyAdapter(List<DanceClass> list) {
            this.list = list;
        }

        // Создаёт новый элемент списка (ViewHolder)
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_dance_class, parent, false);
            return new ViewHolder(view);
        }

        // Заполняет элемент списка данными
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DanceClass item = list.get(position); // Получаем элемент по позиции

            // Устанавливаем значения в TextView
            holder.direction.setText(item.getDirection());
            holder.level.setText(item.getLevel());
            holder.trainer.setText(item.getTrainer());
            holder.date.setText(item.getDate());
            holder.time.setText(item.getTime());

            // Кнопка "Отменить запись"
            holder.registerButton.setText("Отменить запись");
            holder.registerButton.setEnabled(true);

            // Обработка нажатия на кнопку
            holder.registerButton.setOnClickListener(v -> {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) return;

                // Удаление записи из Firestore
                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(user.getUid())
                        .collection("registrations")
                        .document(item.getId())
                        .delete()
                        .addOnSuccessListener(unused -> {
                            // Уведомление пользователя
                            Toast.makeText(holder.itemView.getContext(), "Запись отменена", Toast.LENGTH_SHORT).show();

                            // Отмена уведомления (если было установлено напоминание)
                            AlarmManager alarmManager = (AlarmManager) holder.itemView.getContext().getSystemService(Context.ALARM_SERVICE);
                            Intent intent = new Intent(holder.itemView.getContext(), ReminderReceiver.class);
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                                    holder.itemView.getContext(),
                                    item.getId().hashCode(), // ID напоминания
                                    intent,
                                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                            );
                            alarmManager.cancel(pendingIntent); // отменяем напоминание

                            // Удаляем элемент из списка и обновляем адаптер
                            list.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, list.size());
                        });
            });
        }

        // Возвращает общее количество элементов
        @Override
        public int getItemCount() {
            return list.size();
        }

        // ViewHolder — хранит ссылки на UI-элементы одного элемента списка
        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView direction, level, trainer, date, time;
            Button registerButton;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
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
