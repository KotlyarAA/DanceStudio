package com.example.dancestudio;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class ReminderReceiver extends BroadcastReceiver {

    /**
     * Метод вызывается системой, когда приходит запланированное напоминание (AlarmManager).
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // Получение данных о занятии из переданного интента
        String direction = intent.getStringExtra("direction");
        String level = intent.getStringExtra("level");
        String trainer = intent.getStringExtra("trainer");
        String date = intent.getStringExtra("date");
        String time = intent.getStringExtra("time");

        // Формирование текста уведомления
        String message = "Напоминание: " + direction + " (" + level + ") с " + trainer + " сегодня в " + time;

        // Создание интента для открытия приложения при нажатии на уведомление
        Intent openIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Получаем системный менеджер уведомлений
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "class_reminder_channel"; // ID канала для Android 8+

        // Создание канала уведомлений (обязательно для Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Напоминания о занятиях",
                    NotificationManager.IMPORTANCE_HIGH // Высокий приоритет — уведомление будет видно
            );
            manager.createNotificationChannel(channel);
        }

        // Построение уведомления
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setContentTitle("Скоро занятие!") // Заголовок
                .setContentText(message)            // Основной текст
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Иконка
                .setContentIntent(pendingIntent)    // Действие при нажатии
                .setAutoCancel(true);               // Уведомление исчезает при нажатии

        // Отображение уведомления
        manager.notify((int) System.currentTimeMillis(), builder.build()); // Уникальный ID
    }
}
