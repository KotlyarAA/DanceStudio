package com.example.dancestudio;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.*;

public class ScheduleFragment extends Fragment {

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private RecyclerView recyclerView;
    private ScheduleAdapter adapter;
    private List<DanceClass> allClasses = new ArrayList<>();
    private List<DanceClass> filteredClasses = new ArrayList<>();

    private Spinner spinnerDirection, spinnerLevel, spinnerTrainer;

    public ScheduleFragment() {
        super(R.layout.fragment_schedule);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        recyclerView = view.findViewById(R.id.recyclerViewSchedule);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ScheduleAdapter(filteredClasses);
        recyclerView.setAdapter(adapter);

        spinnerDirection = view.findViewById(R.id.spinnerDirection);
        spinnerLevel = view.findViewById(R.id.spinnerLevel);
        spinnerTrainer = view.findViewById(R.id.spinnerTrainer);

        loadMockData();
        setupSpinners();
    }

    private void setupSpinners() {
        // Предзаполненные фильтры
        List<String> directions = Arrays.asList("Все", "Хип-хоп", "Балет", "Сальса", "Танго");
        List<String> levels = Arrays.asList("Все", "Начальный", "Средний", "Продвинутый");
        List<String> trainers = Arrays.asList("Все", "Алексей", "Мария", "Ирина", "Влад");

        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, directions);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, levels);
        ArrayAdapter<String> adapter3 = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, trainers);

        spinnerDirection.setAdapter(adapter1);
        spinnerLevel.setAdapter(adapter2);
        spinnerTrainer.setAdapter(adapter3);

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        };

        spinnerDirection.setOnItemSelectedListener(listener);
        spinnerLevel.setOnItemSelectedListener(listener);
        spinnerTrainer.setOnItemSelectedListener(listener);
    }

    private void applyFilters() {
        String dir = spinnerDirection.getSelectedItem().toString();
        String lvl = spinnerLevel.getSelectedItem().toString();
        String trn = spinnerTrainer.getSelectedItem().toString();

        filteredClasses.clear();
        for (DanceClass cls : allClasses) {
            boolean match = (dir.equals("Все") || cls.getDirection().equals(dir))
                    && (lvl.equals("Все") || cls.getLevel().equals(lvl))
                    && (trn.equals("Все") || cls.getTrainer().equals(trn));
            if (match) filteredClasses.add(cls);
        }
        adapter.notifyDataSetChanged();
    }

    private void loadMockData() {
        allClasses.add(new DanceClass("c0", "Хип-хоп", "Средний", "Алексей", "2025-05-10", "19:20"));
        allClasses.add(new DanceClass("c1", "Хип-хоп", "Начальный", "Алексей", "2024-05-15", "18:00"));
        allClasses.add(new DanceClass("c2", "Балет", "Средний", "Мария", "2025-05-16", "17:00"));
        allClasses.add(new DanceClass("c3", "Сальса", "Продвинутый", "Ирина", "2025-05-17", "19:30"));
        allClasses.add(new DanceClass("c4", "Танго", "Начальный", "Влад", "2025-05-18", "20:00"));

        filteredClasses.addAll(allClasses);
        adapter.notifyDataSetChanged();
    }

    class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {
        private final List<DanceClass> classList;

        ScheduleAdapter(List<DanceClass> classList) {
            this.classList = classList;
        }

        @NonNull
        @Override
        public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_dance_class, parent, false);
            return new ScheduleViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
            DanceClass cls = classList.get(position);

            holder.direction.setText(cls.getDirection());
            holder.level.setText(cls.getLevel());
            holder.trainer.setText( cls.getTrainer());
            holder.date.setText(cls.getDate());
            holder.time.setText(cls.getTime());


            String userId = currentUser.getUid();
            DocumentReference regRef = db.collection("users").document(userId)
                    .collection("registrations").document(cls.getId());

            regRef.get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    holder.button.setText("Вы записаны");
                    holder.button.setEnabled(false);
                } else {
                    holder.button.setText("Записаться");
                    holder.button.setEnabled(true);
                    holder.button.setOnClickListener(v -> {
                        regRef.set(cls).addOnSuccessListener(unused -> {
                            Toast.makeText(getContext(), "Вы записались", Toast.LENGTH_SHORT).show();
                            notifyItemChanged(position);
                        });
                    });
                }
            });
            long triggerTimeMillis = getReminderTimeMillis(cls.getDate(), cls.getTime());

            Intent intent = new Intent(getContext(), ReminderReceiver.class);
            intent.putExtra("direction", cls.getDirection());
            intent.putExtra("level", cls.getLevel());
            intent.putExtra("trainer", cls.getTrainer());
            intent.putExtra("date", cls.getDate());
            intent.putExtra("time", cls.getTime());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    getContext(),
                    cls.getId().hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );


            AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent);
                } else {
                    Toast.makeText(getContext(), "Нет разрешения на точные напоминания. Уведомление может не сработать вовремя.", Toast.LENGTH_LONG).show();
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent); // fallback
                }
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent);
            }


        }

        @Override
        public int getItemCount() {
            return classList.size();
        }

        class ScheduleViewHolder extends RecyclerView.ViewHolder {
            TextView direction, level, trainer, date, time;
            Button button;

            ScheduleViewHolder(@NonNull View itemView) {
                super(itemView);
                direction = itemView.findViewById(R.id.classDirection);
                level = itemView.findViewById(R.id.classLevel);
                trainer = itemView.findViewById(R.id.classTrainer);
                date = itemView.findViewById(R.id.classDate);
                time = itemView.findViewById(R.id.classTime);
                button = itemView.findViewById(R.id.btnRegisterClass);
            }
        }

    }
    private long getReminderTimeMillis(String date, String time) {
        try {
            String dateTime = date + " " + time; // формат "2024-05-15 18:00"
            java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date classDateTime = format.parse(dateTime);

            long triggerMillis = classDateTime.getTime() - 6 * 60 * 60 * 1000; // минус 6 часов
            return Math.max(triggerMillis, System.currentTimeMillis() + 10000); // минимум через 10 сек
        } catch (Exception e) {
            e.printStackTrace();
            return System.currentTimeMillis() + 10000;
        }
    }

}
