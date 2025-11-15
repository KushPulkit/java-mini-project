package com.school.ui;

import com.school.models.Student;
import com.school.models.Subject;
import com.school.models.GraduatingStudent;
import com.school.processor.ResultProcessor;

import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * Console UI: Main entry point.
 * Implements the required menus with admin (password) and student access.
 * Saves data before exiting.
 */
public class MainUI {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "1234";

    private Scanner sc = new Scanner(System.in);
    private ResultProcessor processor;

    public MainUI() {
        processor = new ResultProcessor(200, 5); // max 200 students, up to 5 subjects
    }

    public void start() {
        System.out.println("====================================");
        System.out.println("  Student Result Processing System  ");
        System.out.println("====================================");

        boolean running = true;
        while (running) {
            System.out.println("\nMain Menu:");
            System.out.println("1. Admin Login");
            System.out.println("2. Student Access (View Report)");
            System.out.println("0. Exit");
            System.out.print("Choice: ");
            int choice = readIntSafe();

            switch (choice) {
                case 1 -> { if (adminLogin()) adminMenu(); }
                case 2 -> studentAccess();
                case 0 -> {
                    try { processor.saveStudentsToFile(); } catch (Exception e) {}
                    try { processor.saveSubjectsToFile(); } catch (Exception e) {}
                    System.out.println("Exiting... Goodbye.");
                    running = false;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
        sc.close();
    }

    // -----------------------------------
    // ADMIN LOGIN
    // -----------------------------------
    private boolean adminLogin() {
        System.out.print("Enter username: ");
        String u = sc.next();
        System.out.print("Enter password: ");
        String p = sc.next();
        if (ADMIN_USERNAME.equals(u) && ADMIN_PASSWORD.equals(p)) {
            System.out.println("Admin login successful.");
            return true;
        } else {
            System.out.println("Invalid credentials.");
            return false;
        }
    }

    // -----------------------------------
    // ADMIN MENU
    // -----------------------------------
    private void adminMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\nAdmin Menu:");
            System.out.println("1. Add Student");
            System.out.println("2. Update Student Name");
            System.out.println("3. Delete Student");
            System.out.println("4. Enter/Edit Student Marks");
            System.out.println("5. View All Students");
            System.out.println("6. Manage Subjects");
            System.out.println("7. View Student Result");
            System.out.println("8. Subject-wise Analysis");
            System.out.println("9. Update Graduation Details");
            System.out.println("0. Logout");
            System.out.print("Choice: ");
            int ch = readIntSafe();

            switch (ch) {
                case 1 -> addStudentFlow();
                case 2 -> updateStudentFlow();
                case 3 -> deleteStudentFlow();
                case 4 -> enterMarksFlow();
                case 5 -> processor.listAllStudentsBrief();
                case 6 -> manageSubjectsMenu();
                case 7 -> viewStudentResultAdmin();
                case 8 -> processor.displaySubjectAnalysis();
                case 9 -> updateGraduationFlow();
                case 0 -> back = true;
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    // -----------------------------------
    // NEW FUNCTIONALITY: Admin View Result
    // -----------------------------------
    private void viewStudentResultAdmin() {
        System.out.println("\n-- View Student Result --");
        System.out.print("Enter Student ID: ");
        int id = readIntSafe();
        processor.displayStudentDetails(id);
    }

    // -----------------------------------
    // STUDENT FUNCTIONS
    // -----------------------------------
    private void addStudentFlow() {
        System.out.println("\n-- Add Student --");
        System.out.print("Enter ID: ");
        int id = readIntSafe();

        if (processor.getStudentById(id) != null) {
            System.out.println("A student with that ID already exists.");
            return;
        }

        System.out.print("Enter Name: ");
        String name = readLineTrim();

        System.out.print("Enter Age: ");
        int age = readIntSafe();

        // ask if graduating student
        System.out.print("Is this a graduating student? (y/n): ");
        String isGrad = readLineTrim();

        Student s;
        if (isGrad.equalsIgnoreCase("y")) {
            System.out.print("Enter transcript notes (or leave blank): ");
            String transcript = readLineTrim();
            System.out.print("Has the student graduated? (y/n): ");
            boolean status = readLineTrim().equalsIgnoreCase("y");
            s = new GraduatingStudent(id, name, age, processor.getSubjectCount(), transcript, status);
        } else {
            s = new Student(id, name, age, processor.getSubjectCount());
        }

        boolean ok = processor.addStudent(s);
        System.out.println(ok ? "Student added." : "Could not add student.");
    }

    private void updateStudentFlow() {
        System.out.println("\n-- Update Student Name --");
        System.out.print("Enter ID: ");
        int id = readIntSafe();

        Student s = processor.getStudentById(id);
        if (s == null) {
            System.out.println("Student not found.");
            return;
        }

        System.out.print("Enter new name: ");
        String name = readLineTrim();

        boolean ok = processor.updateStudentName(id, name);
        System.out.println(ok ? "Name updated." : "Update failed.");
    }

    private void deleteStudentFlow() {
        System.out.println("\n-- Delete Student --");
        System.out.print("Enter ID: ");
        int id = readIntSafe();

        boolean ok = processor.deleteStudent(id);
        System.out.println(ok ? "Deleted." : "No such student.");
    }

    private void enterMarksFlow() {
        if (processor.getSubjectCount() == 0) {
            System.out.println("No subjects configured. Please add subjects first.");
            return;
        }

        System.out.println("\n-- Enter/Edit Marks --");
        System.out.print("Enter Student ID: ");
        int id = readIntSafe();

        Student s = processor.getStudentById(id);
        if (s == null) {
            System.out.println("Student not found.");
            return;
        }

        processor.listSubjects();

        System.out.print("Enter Subject name exactly as shown: ");
        String subj = readLineTrim();

        System.out.print("Enter marks (0 - maxMarks): ");
        int marks = readIntSafe();

        boolean ok = processor.updateMarkForStudent(id, subj, marks);
        System.out.println(ok ? "Marks updated." : "Failed to update marks.");
    }

    // -----------------------------------
    // SUBJECT MANAGEMENT
    // -----------------------------------
    private void manageSubjectsMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\nSubjects Management:");
            System.out.println("1. List Subjects");
            System.out.println("2. Add Subject");
            System.out.println("3. Remove Subject");
            System.out.println("0. Back");
            System.out.print("Choice: ");
            int ch = readIntSafe();

            switch (ch) {
                case 1 -> processor.listSubjects();
                case 2 -> addSubjectFlow();
                case 3 -> removeSubjectFlow();
                case 0 -> back = true;
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void addSubjectFlow() {
        System.out.println("\n-- Add Subject --");

        if (processor.getSubjectCount() >= 5) {
            System.out.println("Max 5 subjects allowed.");
            return;
        }

        System.out.print("Enter subject name: ");
        String name = readLineTrim();

        System.out.print("Enter max marks (e.g., 100): ");
        int max = readIntSafe();

        if (max <= 0) {
            System.out.println("Invalid max marks.");
            return;
        }

        boolean ok = processor.addSubject(new Subject(name, max));
        System.out.println(ok ? "Subject added." : "Could not add subject.");
    }

    private void removeSubjectFlow() {
        System.out.print("Enter subject name to remove: ");
        String name = readLineTrim();
        boolean ok = processor.removeSubject(name);
        System.out.println(ok ? "Subject removed." : "No such subject.");
    }

    // -----------------------------------
    // UPDATE GRADUATION INFO (ADMIN)
    // -----------------------------------
    private void updateGraduationFlow() {
        System.out.println("\n-- Update Graduation Details --");
        System.out.print("Enter Student ID: ");
        int id = readIntSafe();

        Student s = processor.getStudentById(id);
        if (s == null) {
            System.out.println("Student not found.");
            return;
        }

        if (!(s instanceof GraduatingStudent)) {
            System.out.println("This is NOT a graduating student. If you want to convert them, delete and re-add as graduating.");
            return;
        }

        GraduatingStudent gs = (GraduatingStudent) s;
        System.out.println("Current transcript: " + gs.getTranscript());
        System.out.print("Enter new transcript (leave blank to keep): ");
        String transcript = readLineTrim();
        if (!transcript.isEmpty()) gs.setTranscript(transcript);

        System.out.print("Has the student graduated? (y/n): ");
        boolean status = readLineTrim().equalsIgnoreCase("y");
        gs.setGraduationStatus(status);

        boolean ok = processor.updateGraduationInfo(id, gs.getTranscript(), gs.isGraduationStatus());
        System.out.println(ok ? "Graduation info updated." : "Failed to update graduation info.");
    }

    // -----------------------------------
    // STUDENT ACCESS
    // -----------------------------------
    private void studentAccess() {
        System.out.println("\n-- Student Access --");
        System.out.print("Enter Student ID: ");
        int id = readIntSafe();
        processor.displayStudentDetails(id);
    }

    // -----------------------------------
    // UTIL
    // -----------------------------------
    private int readIntSafe() {
        while (true) {
            try {
                int v = sc.nextInt();
                sc.nextLine();
                return v;
            } catch (InputMismatchException e) {
                System.out.print("Please enter a valid integer: ");
                sc.nextLine();
            }
        }
    }

    private String readLineTrim() {
        String line = sc.nextLine();
        return (line == null) ? "" : line.trim();
    }

    // -----------------------------------
    // MAIN
    // -----------------------------------
    public static void main(String[] args) {
        MainUI ui = new MainUI();
        ui.start();
    }
}
