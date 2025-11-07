package com.example.chicksevent;

public class EntrantDisplay {
    private String entrantId;
    private String status;

    public EntrantDisplay() {}

    public EntrantDisplay(String entrantId, String status) {
        this.entrantId = entrantId;
        this.status = status;
    }

    public String getEntrantId() { return entrantId; }
    public String getStatus() { return status; }
}
