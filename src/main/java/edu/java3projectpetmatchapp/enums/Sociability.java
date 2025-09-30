package edu.java3projectpetmatchapp.enums;

public enum Sociability {

    LIKES_CATS("Likes Cats"),
    DISLIKES_CATS("Dislikes Cats"),
    LIKES_DOGS("Likes Dogs"),
    DISLIKES_DOGS("Dislikes Dogs"),
    LIKE_KIDS("Good with Kids"),
    DISLIKES_KIDS("Not Good with Kids"),
    UNKNOWN("Unknown");

    private final String label;

    Sociability(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}