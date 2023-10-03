package org.curbee.auditsystem;

import java.util.List;

public class ListUpdate<T> implements ChangeType {

    private String property;

    private List<T> added;

    private List<T> removed;

    public ListUpdate(String property, List<T> added, List<T> removed) {
        this.property = property;
        this.added = added;
        this.removed = removed;
    }

    public List<T> getAdded() {
        return added;
    }

    public List<T> getRemoved() {
        return removed;
    }

    @Override
    public String getProperty() {
        return property;
    }

    @Override
    public String toString() {
        return String.format("{\"property\": \"%s\", \"added\": %s, \"removed\": %s}", property, added, removed);
    }
}
