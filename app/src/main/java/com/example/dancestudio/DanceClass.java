package com.example.dancestudio;

// Импорт интерфейса Serializable для возможности сериализации объекта,
// например, при передаче между активностями или сохранении состояния
import java.io.Serializable;

/**
 * Модель данных для описания танцевального занятия.
 * Класс используется для хранения и передачи информации о занятии:
 * направление, уровень, тренер, дата, время и ID.
 */
public class DanceClass implements Serializable {

    // Уникальный идентификатор занятия (например, используется как ID документа в Firestore)
    private String id;

    // Направление танца (например, Хип-хоп, Балет и т.д.)
    private String direction;

    // Уровень занятия (начальный, средний, продвинутый)
    private String level;

    // Имя или фамилия тренера, проводящего занятие
    private String trainer;

    // Дата проведения занятия (в формате "yyyy-MM-dd")
    private String date;

    // Время начала занятия (например, "18:00")
    private String time;

    // Пустой конструктор обязателен для Firebase Firestore
    // Он используется при автоматическом создании объекта из документа (toObject)
    public DanceClass() {}

    // Полный конструктор — позволяет сразу создать объект со всеми полями
    public DanceClass(String id, String direction, String level, String trainer, String date, String time) {
        this.id = id;
        this.direction = direction;
        this.level = level;
        this.trainer = trainer;
        this.date = date;
        this.time = time;
    }

    // Геттеры (методы для получения значений полей)

    public String getId() {
        return id;
    }

    public String getDirection() {
        return direction;
    }

    public String getLevel() {
        return level;
    }

    public String getTrainer() {
        return trainer;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

}
