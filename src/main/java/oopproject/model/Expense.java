package oopproject.model;

import java.time.LocalDate;

// Stores one expense transaction for a user
public class Expense {

    private int id;
    private int userId;
    private String category;
    private String title;
    private double amount;
    private LocalDate date;
    private String note;

    public Expense(int id, int userId, String category, String title, double amount, LocalDate date, String note) {
        this.id = id;
        this.userId = userId;
        this.category = category;
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

    public String getCategory() {
        return category;
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