package oopproject.model;

import java.time.LocalDate;

// Stores all account and profile data for one user
public class User {
    private int id;
    private String fullName;
    private String email;
    private String password;
    private LocalDate dateOfBirth;
    private String studyField;
    private boolean employed;
    private String jobTitle;

    // Empty constructor
    public User() {
    }

    // Full constructor used when creating or loading a user
    public User(int id, String fullName, String email, String password,
                LocalDate dateOfBirth, String studyField,
                boolean employed, String jobTitle) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.dateOfBirth = dateOfBirth;
        this.studyField = studyField;
        this.employed = employed;
        this.jobTitle = jobTitle;
    }

    public int getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getStudyField() {
        return studyField;
    }

    public boolean isEmployed() {
        return employed;
    }

    public String getJobTitle() {
        return jobTitle;
    }
}