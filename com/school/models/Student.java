package com.school.models;

/**
 * Base Student class.
 * stores marks array parallel to subjects array in ResultProcessor (P7).
 */
public class Student {
    private int id;                   // encapsulated (P4)
    private String name;
    private int age;
    private double[] marks;           // marks per subject; -1 means "not entered"
    private String grade = "N/A";
    private boolean passed = false;

    public Student() {}

    // subjectCount defines size of marks array at creation
    public Student(int id, String name, int age, int subjectCount) {
        this.id = id;
        this.name = name;
        this.age = age;
        if (subjectCount < 0) subjectCount = 0;
        this.marks = new double[subjectCount];
        for (int i = 0; i < subjectCount; i++) this.marks[i] = -1; // -1 => not entered
    }

    // Getters and setters (encapsulated fields)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id > 0) this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name != null && !name.trim().isEmpty()) this.name = name.trim();
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        if (age > 0) this.age = age;
    }

    public double[] getMarks() {
        return marks;
    }

    // set whole marks array (used when subjects change)
    public void setMarksArray(double[] marks) {
        if (marks == null) return;
        this.marks = marks;
    }

    public void setMarkAt(int index, double value) {
        if (marks == null) return;
        if (index >= 0 && index < marks.length) marks[index] = value;
    }

    public double getMarkAt(int index) {
        if (marks == null) return -1;
        if (index >= 0 && index < marks.length) return marks[index];
        return -1;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        if (grade != null) this.grade = grade;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    /**
     * getSummary - can be overridden by subclasses (P6).
     */
    public String getSummary() {
        return "Student: " + name + " (ID: " + id + ")";
    }

    public void displayInfo() {
        System.out.println("ID   : " + id);
        System.out.println("Name : " + name);
        System.out.println("Age  : " + age);
    }

    /**
     * toFileString - format for saving to file: id|name|age|m1,m2,m3...
     */
    public String toFileString() {
        StringBuilder sb = new StringBuilder();
        sb.append(id).append("|").append(name).append("|").append(age).append("|");
        if (marks != null && marks.length > 0) {
            for (int i = 0; i < marks.length; i++) {
                sb.append((int)marks[i]); // store ints for readability, -1 preserved
                if (i < marks.length - 1) sb.append(",");
            }
        }
        return sb.toString();
    }

    // static helper to parse marks string into array
    public static double[] parseMarksString(String marksStr, int subjectCount) {
        double[] arr = new double[subjectCount];
        for (int i = 0; i < subjectCount; i++) arr[i] = -1;
        if (marksStr == null || marksStr.trim().isEmpty()) return arr;
        String[] parts = marksStr.split(",");
        for (int i = 0; i < subjectCount && i < parts.length; i++) {
            try {
                arr[i] = Double.parseDouble(parts[i].trim());
            } catch (NumberFormatException e) {
                arr[i] = -1;
            }
        }
        return arr;
    }
}
