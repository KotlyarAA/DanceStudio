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

    public AttendedClassesFragment() {
        super(R.layout.fragment_attended_classes);
    }

    private final List<DanceClass> attendedList = new ArrayList<>();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewAttended);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .collection("attended")
                .get()
                .addOnSuccessListener(query -> {
                    attendedList.clear();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        DanceClass danceClass = doc.toObject(DanceClass.class);
                        if (danceClass != null) {
                            attendedList.add(danceClass);
                        }
                    }
                    recyclerView.setAdapter(new AttendedAdapter(attendedList));
                });
    }

    static class AttendedAdapter extends RecyclerView.Adapter<AttendedAdapter.ViewHolder> {
        private final List<DanceClass> list;

        AttendedAdapter(List<DanceClass> list) {
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

            holder.registerButton.setText("Посещено");
            holder.registerButton.setEnabled(false);
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
