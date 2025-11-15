package com.school.processor;

import com.school.models.Student;
import com.school.models.Subject;
import com.school.models.GraduatingStudent;

import java.io.*;
import java.util.*;

/**
 * ResultProcessor handles:
 *  - arrays of Students and Subjects (P7)
 *  - CRUD operations on students and subjects
 *  - file I/O persistence (subjects.txt, students.txt) (P2)
 *  - grading logic using objects as arguments (P8)
 *  - exception handling (P3)
 *
 * Note: Subject-wise analysis treats each subject with its own maxMarks.
 * For pass/fail per-subject we use 40% of the subject's maxMarks as threshold.
 */
public class ResultProcessor {

    private Subject[] subjects;
    private Student[] students;
    private int subjectCount;
    private int studentCount;

    private final int MAX_SUBJECTS;
    private final int MAX_STUDENTS;
    private final int PASS_PERCENT = 40; // percent of subject max to consider pass

    private final String SUBJECTS_FILE = "subjects.txt";
    private final String STUDENTS_FILE = "students.txt";

    public ResultProcessor(int maxStudents, int maxSubjects) {
        this.MAX_STUDENTS = maxStudents;
        this.MAX_SUBJECTS = maxSubjects;
        students = new Student[MAX_STUDENTS];
        subjects = new Subject[MAX_SUBJECTS];
        studentCount = 0;
        subjectCount = 0;

        // Auto-load existing data
        try {
            loadSubjectsFromFile();
        } catch (IOException e) {
            System.out.println("Note: subjects file not loaded: " + e.getMessage());
        }

        try {
            loadStudentsFromFile();
        } catch (IOException e) {
            System.out.println("Note: students file not loaded: " + e.getMessage());
        }
    }

    // ------------------ SUBJECT MANAGEMENT ------------------

    public boolean addSubject(Subject subj) {
        if (subj == null) return false;
        if (subjectCount >= MAX_SUBJECTS) {
            System.out.println("Maximum number of subjects reached (" + MAX_SUBJECTS + ").");
            return false;
        }
        // prevent duplicate subject names
        if (findSubjectIndexByName(subj.getName()) != -1) {
            System.out.println("Subject already exists: " + subj.getName());
            return false;
        }
        subjects[subjectCount++] = subj;

        // Expand each student's marks array to accommodate new subject
        for (int i = 0; i < studentCount; i++) {
            Student s = students[i];
            double[] old = s.getMarks();
            double[] newer = new double[subjectCount];
            for (int j = 0; j < newer.length; j++) {
                if (old != null && j < old.length) newer[j] = old[j];
                else newer[j] = -1;
            }
            s.setMarksArray(newer);
        }

        // Save subjects immediately to persist change
        try {
            saveSubjectsToFile();
        } catch (IOException e) {
            System.out.println("Warning: could not save subjects to file: " + e.getMessage());
        }
        return true;
    }

    public boolean removeSubject(String name) {
        int idx = findSubjectIndexByName(name);
        if (idx == -1) return false;

        // shift subjects left
        for (int i = idx; i < subjectCount - 1; i++) subjects[i] = subjects[i+1];
        subjects[--subjectCount] = null;

        // shrink students' marks arrays (drop the column)
        for (int i = 0; i < studentCount; i++) {
            Student s = students[i];
            double[] old = s.getMarks();
            double[] newer = new double[subjectCount];
            for (int j = 0; j < newer.length; j++) {
                newer[j] = (old != null && j < old.length ? old[j] : -1);
            }
            s.setMarksArray(newer);
        }

        try {
            saveSubjectsToFile();
        } catch (IOException e) {
            System.out.println("Warning: could not save subjects to file: " + e.getMessage());
        }
        try {
            saveStudentsToFile();
        } catch (IOException e) {
            System.out.println("Warning: could not save students after subject removal.");
        }
        return true;
    }

    public void listSubjects() {
        if (subjectCount == 0) {
            System.out.println("No subjects configured yet.");
            return;
        }
        System.out.println("\nConfigured subjects:");
        for (int i = 0; i < subjectCount; i++) {
            System.out.println((i+1) + ". " + subjects[i].getName() + " (Max: " + subjects[i].getMaxMarks() + ")");
        }
    }

    private int findSubjectIndexByName(String name) {
        if (name == null) return -1;
        for (int i = 0; i < subjectCount; i++) {
            if (subjects[i] != null && subjects[i].getName().equalsIgnoreCase(name.trim())) return i;
        }
        return -1;
    }

    // ------------------ STUDENT MANAGEMENT ------------------

    public boolean addStudent(Student s) {
        if (s == null) return false;
        if (studentCount >= MAX_STUDENTS) {
            System.out.println("Student capacity reached.");
            return false;
        }
        if (findStudentIndexById(s.getId()) != -1) {
            System.out.println("A student with ID " + s.getId() + " already exists. Cannot add duplicate.");
            return false;
        }
        // ensure marks size matches subjectCount
        if (s.getMarks() == null || s.getMarks().length != subjectCount) {
            double[] arr = new double[subjectCount];
            for (int i = 0; i < subjectCount; i++) arr[i] = -1;
            s.setMarksArray(arr);
        }
        students[studentCount++] = s;

        // persist immediately
        try {
            saveStudentsToFile();
        } catch (IOException e) {
            System.out.println("Warning: Could not save students after add: " + e.getMessage());
        }
        return true;
    }

    public boolean updateStudentName(int id, String newName) {
        int idx = findStudentIndexById(id);
        if (idx == -1) return false;
        students[idx].setName(newName);
        try { saveStudentsToFile(); } catch (IOException e) {}
        return true;
    }

    public boolean deleteStudent(int id) {
        int idx = findStudentIndexById(id);
        if (idx == -1) return false;
        for (int i = idx; i < studentCount - 1; i++) students[i] = students[i+1];
        students[--studentCount] = null;
        try { saveStudentsToFile(); } catch (IOException e) {}
        return true;
    }

    private int findStudentIndexById(int id) {
        for (int i = 0; i < studentCount; i++) {
            if (students[i] != null && students[i].getId() == id) return i;
        }
        return -1;
    }

    public Student getStudentById(int id) {
        int idx = findStudentIndexById(id);
        return (idx == -1) ? null : students[idx];
    }

    public void listAllStudentsBrief() {
        if (studentCount == 0) {
            System.out.println("No students available.");
            return;
        }
        System.out.printf("%-6s %-20s %-6s\n", "ID", "Name", "Grade");
        System.out.println("----------------------------------");
        for (int i = 0; i < studentCount; i++) {
            Student s = students[i];
            System.out.printf("%-6d %-20s %-6s\n", s.getId(), s.getName(), s.getGrade());
        }
    }

    /**
     * displayStudentDetails(int id)
     * - Prints student basic info
     * - Prints per-subject marks (or N/A)
     * - Prints TotalObtained / TotalMax and percentage
     * - Prints Grade and PASS/FAIL (grade computed using calculateGrade)
     */
    public void displayStudentDetails(int id) {
        Student s = getStudentById(id);
        if (s == null) {
            System.out.println("Student not found.");
            return;
        }
        s.displayInfo();
        // print subject-wise marks
        System.out.println("Marks:");
        double totalObtained = 0.0;
        double totalMax = 0.0;
        for (int i = 0; i < subjectCount; i++) {
            double m = s.getMarkAt(i);
            String markStr = (m == -1) ? "N/A" : String.valueOf((int)m);
            String subjName = (subjects[i] != null) ? subjects[i].getName() : "Sub" + (i+1);
            int subjMax = (subjects[i] != null) ? subjects[i].getMaxMarks() : 100;
            System.out.printf("  %-15s : %6s / %d\n", subjName, markStr, subjMax);

            if (m != -1) totalObtained += m;
            totalMax += subjMax;
        }

        // totals and percentage
        System.out.println("------------------------");
        System.out.println("Total Marks : " + (int)totalObtained + " / " + (int)totalMax);
        double percent = (totalMax == 0) ? 0.0 : (totalObtained * 100.0 / totalMax);
        System.out.printf("Percentage  : %.2f%%\n", percent);

        // grade and pass/fail
        String grade = calculateGrade(s, subjects);
        s.setGrade(grade);
        s.setPassed(checkPass(s));
        System.out.println("Grade       : " + s.getGrade());
        System.out.println("Result      : " + (s.isPassed() ? "PASS" : "FAIL"));
    }

    // ------------------ MARKS & GRADE LOGIC ------------------

    /**
     * updateMarkForStudent: sets a student's mark for a given subject (validated).
     */
    public boolean updateMarkForStudent(int studentId, String subjectName, int marksValue) {
        if (marksValue < 0) return false;
        int sidx = findStudentIndexById(studentId);
        if (sidx == -1) {
            System.out.println("Student not found.");
            return false;
        }
        int subjIdx = findSubjectIndexByName(subjectName);
        if (subjIdx == -1) {
            System.out.println("Subject not found.");
            return false;
        }
        int max = subjects[subjIdx].getMaxMarks();
        if (marksValue < 0 || marksValue > max) {
            System.out.println("Marks must be between 0 and " + max);
            return false;
        }
        students[sidx].setMarkAt(subjIdx, marksValue);

        // update grade and persist
        String grade = calculateGrade(students[sidx], subjects);
        students[sidx].setGrade(grade);
        students[sidx].setPassed(checkPass(students[sidx]));
        try { saveStudentsToFile(); } catch (IOException e) {}
        return true;
    }

    /**
     * calculateGrade (P8) - uses Student object and Subject[] as arguments.
     * Returns letter grade based on average; uses conditional logic (P9).
     * Average is computed using all subject slots (missing treated as 0 for avg but flagged as incomplete for pass).
     */
    public String calculateGrade(Student student, Subject[] subjectArr) {
        if (student == null || subjectArr == null) return "N/A";
        double total = 0;
        int count = 0;
        for (int i = 0; i < subjectCount; i++) {
            double m = student.getMarkAt(i);
            if (m >= 0) {
                total += m;
            } else {
                total += 0;
            }
            count++;
        }
        double avg = (count == 0) ? 0.0 : (total / count);

        if (avg >= 90) return "A+";
        else if (avg >= 80) return "A";
        else if (avg >= 70) return "B";
        else if (avg >= 60) return "C";
        else if (avg >= 50) return "D";
        else return "F";
    }

    /**
     * checkPass - student passes only if every subject has a mark entered (not -1) and >= PASS_PERCENT% of subject max.
     */
    public boolean checkPass(Student student) {
        for (int i = 0; i < subjectCount; i++) {
            double m = student.getMarkAt(i);
            if (m == -1) return false; // incomplete -> fail
            int subjMax = (subjects[i] != null) ? subjects[i].getMaxMarks() : 100;
            double threshold = subjMax * PASS_PERCENT / 100.0;
            if (m < threshold) return false;
        }
        return true;
    }

    // ------------------ FILE I/O: subjects.txt & students.txt (P2,P3) ------------------

    /**
     * subjects.txt format: each line -> subjectName|maxMarks
     */
    public void saveSubjectsToFile() throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(SUBJECTS_FILE))) {
            for (int i = 0; i < subjectCount; i++) {
                Subject s = subjects[i];
                bw.write(s.getName() + "|" + s.getMaxMarks());
                bw.newLine();
            }
        }
    }

    public void loadSubjectsFromFile() throws IOException {
        File f = new File(SUBJECTS_FILE);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            subjectCount = 0;
            String line;
            while ((line = br.readLine()) != null && subjectCount < MAX_SUBJECTS) {
                String[] parts = line.split("\\|");
                String name = parts[0].trim();
                int max = 100;
                if (parts.length >= 2) {
                    try { max = Integer.parseInt(parts[1].trim()); } catch (NumberFormatException ex) { max = 100; }
                }
                subjects[subjectCount++] = new Subject(name, max);
            }
        }
    }

    /**
     * students.txt format per line:
     * id|name|age|m1,m2,m3...
     */
    public void saveStudentsToFile() throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(STUDENTS_FILE))) {
            for (int i = 0; i < studentCount; i++) {
                Student s = students[i];
                bw.write(s.toFileString());
                bw.newLine();
            }
        }
    }

    public void loadStudentsFromFile() throws IOException {
        File f = new File(STUDENTS_FILE);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            studentCount = 0;
            String line;
            while ((line = br.readLine()) != null && studentCount < MAX_STUDENTS) {
                // parse id|name|age|m1,m2,...
                String[] parts = line.split("\\|");
                if (parts.length < 4) continue; // malformed, skip
                try {
                    int id = Integer.parseInt(parts[0].trim());
                    String name = parts[1].trim();
                    int age = Integer.parseInt(parts[2].trim());
                    String marksStr = parts[3].trim();
                    double[] arr = Student.parseMarksString(marksStr, subjectCount);
                    Student s = new Student(id, name, age, subjectCount);
                    s.setMarksArray(arr);
                    // compute grade/pass on load
                    s.setGrade(calculateGrade(s, subjects));
                    s.setPassed(checkPass(s));
                    students[studentCount++] = s;
                } catch (NumberFormatException ex) {
                    // skip bad line
                }
            }
        }
    }

    // ------------------ SUBJECT-WISE ANALYSIS ------------------

    /**
     * displaySubjectAnalysis
     * Prints a table:
     * Subject | Avg Marks | Highest | Lowest | Pass | Fail | Topper
     *
     * Averages and highest/lowest are computed from entered marks only (marks != -1).
     * Pass/Fail counts are computed based on PASS_PERCENT of subject max.
     */
    public void displaySubjectAnalysis() {
        if (subjectCount == 0) {
            System.out.println("No subjects configured.");
            return;
        }
        if (studentCount == 0) {
            System.out.println("No students available for analysis.");
            return;
        }

        System.out.println("--------------------------------------------------------------");
        System.out.printf("%-12s | %-9s | %-7s | %-6s | %-4s | %-4s | %-15s\n",
                "Subject", "Avg Marks", "Highest", "Lowest", "Pass", "Fail", "Topper");
        System.out.println("--------------------------------------------------------------");

        double bestAvg = -1;
        int bestSubIdx = -1;
        double worstAvg = Double.MAX_VALUE;
        int worstSubIdx = -1;

        for (int j = 0; j < subjectCount; j++) {
            double sum = 0.0;
            int countEntered = 0;
            double highest = -1;
            double lowest = Double.MAX_VALUE;
            String topperName = "N/A";
            int passCount = 0;
            int failCount = 0;

            int subjMax = (subjects[j] != null) ? subjects[j].getMaxMarks() : 100;
            double passThreshold = subjMax * PASS_PERCENT / 100.0;

            for (int i = 0; i < studentCount; i++) {
                Student s = students[i];
                if (s == null) continue;
                double m = s.getMarkAt(j);
                if (m == -1) continue; // skip not-entered

                countEntered++;
                sum += m;

                if (m > highest) {
                    highest = m;
                    topperName = s.getName();
                }
                if (m < lowest) {
                    lowest = m;
                }

                if (m >= passThreshold) passCount++;
                else failCount++;
            }

            double avg = (countEntered == 0) ? 0.0 : (sum / countEntered);

            // update best/worst subject by avg (only consider subjects with at least one entered mark)
            if (countEntered > 0) {
                if (avg > bestAvg) { bestAvg = avg; bestSubIdx = j; }
                if (avg < worstAvg) { worstAvg = avg; worstSubIdx = j; }
            }

            String highestStr = (highest < 0) ? "N/A" : String.valueOf((int)highest);
            String lowestStr = (lowest == Double.MAX_VALUE) ? "N/A" : String.valueOf((int)lowest);

            System.out.printf("%-12s | %9.2f | %7s | %6s | %4d | %4d | %-15s\n",
                    (subjects[j] != null ? subjects[j].getName() : "Sub"+(j+1)),
                    avg,
                    highestStr,
                    lowestStr,
                    passCount,
                    failCount,
                    topperName);
        }

        System.out.println("--------------------------------------------------------------");

        if (bestSubIdx != -1) {
            System.out.println("Best Performing Subject : " + subjects[bestSubIdx].getName());
        } else {
            System.out.println("Best Performing Subject : N/A (no marks entered)");
        }

        if (worstSubIdx != -1) {
            System.out.println("Toughest Subject : " + subjects[worstSubIdx].getName());
        } else {
            System.out.println("Toughest Subject : N/A (no marks entered)");
        }
    }

    // ------------------ GETTERS FOR UI ------------------
    public Subject[] getSubjectsArray() { return subjects; }
    public Student[] getStudentsArray() { return students; }
    public int getSubjectCount() { return subjectCount; }
    public int getStudentCount() { return studentCount; }
}
