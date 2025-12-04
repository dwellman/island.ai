package com.demo.island.core;

import java.util.Objects;

public final class TextFace {

    private final String name;
    private final String shortDescription;
    private final String detailDescription;
    private final String history;

    public TextFace(String name, String shortDescription, String detailDescription, String history) {
        this.name = Objects.requireNonNullElse(name, "");
        this.shortDescription = Objects.requireNonNullElse(shortDescription, "");
        this.detailDescription = Objects.requireNonNullElse(detailDescription, "");
        this.history = Objects.requireNonNullElse(history, "");
    }

    public static TextFace empty(String name) {
        return new TextFace(name, "", "", "");
    }

    public String getName() {
        return name;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getDetailDescription() {
        return detailDescription;
    }

    public String getHistory() {
        return history;
    }

    public TextFace withShortDescription(String updated) {
        return new TextFace(this.name, updated, this.detailDescription, this.history);
    }

    public TextFace withDetailDescription(String updated) {
        return new TextFace(this.name, this.shortDescription, updated, this.history);
    }

    public TextFace withHistory(String updated) {
        return new TextFace(this.name, this.shortDescription, this.detailDescription, updated);
    }

    @Override
    public String toString() {
        return name;
    }
}
