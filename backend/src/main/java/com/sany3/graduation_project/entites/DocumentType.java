package com.sany3.graduation_project.entites;

public enum DocumentType {
    ID("National ID or Emirates ID"),
    LICENSE("Professional License"),
    CERTIFICATE("Professional Certificate"),
    INSURANCE("Insurance Document"),
    OTHER("Other Document");

    private final String description;

    DocumentType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}