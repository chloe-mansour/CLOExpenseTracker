package oopproject.model;

import java.time.LocalDate;

// Stores one income transaction for a user
public class Income {

    private int id;
    private int userId;
    private String source;
    private String title;
    private double amount;
    private LocalDate date;
    private String note;

    public Income(int id, int userId, String source, String title, double amount, LocalDate date, String note) {
        this.id = id;
        this.userId = userId;
        this.source = source;
        this.title = title;
        this.amount = amount;
        this.date = date;
        this.note = note;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getSource() {
        return source;
    }

    public String getTitle() {
        return title;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getNote() {
        return note;
    }
}
