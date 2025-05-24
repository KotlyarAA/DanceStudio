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

public class MyRegistrationsFragment extends Fragment {

    public MyRegistrationsFragment() {
        super(R.layout.fragment_my_registrations);
    }

    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private final List<DanceClass> myList = new ArrayList<>();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewMyClasses);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        CollectionReference regRef = db
                .collection("users")
                .document(userId)
                .collection("registrations");

        regRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            myList.clear();
            WriteBatch batch = db.batch();

            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                DanceClass danceClass = doc.toObject(DanceClass.class);
                if (danceClass == null) continue;

                if (isClassInPast(danceClass)) {
                    DocumentReference attendedRef = db
                            .collection("users")
                            .document(currentUser.getUid())
                            .collection("attended")
                            .document(danceClass.getId());

                    batch.set(attendedRef, danceClass);
                    batch.delete(doc.getReference());
                } else {
                    myList.add(danceClass);
                }
            }

            batch.commit().addOnCompleteListener(task -> {
                recyclerView.setAdapter(new MyAdapter(myList));
            });
        });
    }

    private boolean isClassInPast(DanceClass danceClass) {
        try {
            String dateTime = danceClass.getDate() + " " + danceClass.getTime();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date classDate = format.parse(dateTime);
            return classDate != null && System.currentTimeMillis() > classDate.getTime();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    static class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private final List<DanceClass> list;

        MyAdapter(List<DanceClass> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_dance_class, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DanceClass item = list.get(position);
            holder.direction.setText(item.getDirection());
            holder.level.setText(item.getLevel());
            holder.trainer.setText(item.getTrainer());
            holder.date.setText(item.getDate());
            holder.time.setText(item.getTime());

            holder.registerButton.setText("Отменить запись");
            holder.registerButton.setEnabled(true);

            holder.registerButton.setOnClickListener(v -> {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) return;

                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(user.getUid())
                        .collection("registrations")
                        .document(item.getId())
                        .delete()
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(holder.itemView.getContext(), "Запись отменена", Toast.LENGTH_SHORT).show();

                            AlarmManager alarmManager = (AlarmManager) holder.itemView.getContext().getSystemService(Context.ALARM_SERVICE);
                            Intent intent = new Intent(holder.itemView.getContext(), ReminderReceiver.class);
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                                    holder.itemView.getContext(),
                                    item.getId().hashCode(),
                                    intent,
                                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                            );
                            alarmManager.cancel(pendingIntent);

                            list.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, list.size());
                        });
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

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
