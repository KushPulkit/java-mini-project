package com.school.models;

/**
 * Subject model.
 * Enforces encapsulation for maxMarks (private).
 */
public class Subject {
    private String name;
    private int maxMarks; // encapsulated (P4)

    public Subject() {}

    public Subject(String name, int maxMarks) {
        this.name = name;
        this.maxMarks = maxMarks;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name != null && !name.trim().isEmpty()) this.name = name.trim();
    }

    public int getMaxMarks() {
        return maxMarks;
    }

    public void setMaxMarks(int maxMarks) {
        if (maxMarks > 0) this.maxMarks = maxMarks;
    }

    @Override
    public String toString() {
        return name + " (Max: " + maxMarks + ")";
    }
}
