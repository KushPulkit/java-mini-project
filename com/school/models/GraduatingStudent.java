package com.school.models;

/**
 * GraduatingStudent extends Student and adds transcript info.
 * Demonstrates inheritance and overridden summary (P5,P6).
 */
public class GraduatingStudent extends Student {
    private String transcript;
    private boolean graduationStatus;

    public GraduatingStudent() { super(); }

    public GraduatingStudent(int id, String name, int age, int subjectCount, String transcript, boolean graduated) {
        super(id, name, age, subjectCount);
        this.transcript = transcript;
        this.graduationStatus = graduated;
    }

    public String getTranscript() {
        return transcript;
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }

    public boolean isGraduationStatus() {
        return graduationStatus;
    }

    public void setGraduationStatus(boolean graduationStatus) {
        this.graduationStatus = graduationStatus;
    }

    @Override
    public String getSummary() {
        String base = "Graduating Student: " + getName() + " (ID: " + getId() + ")";
        String status = graduationStatus ? "Graduated" : "Pending";
        return base + " | Status: " + status;
    }

    public String generateTranscript() {
        if (transcript == null || transcript.trim().isEmpty()) {
            return "Transcript not available for " + getName();
        }
        return "Transcript for " + getName() + ": " + transcript;
    }
}
