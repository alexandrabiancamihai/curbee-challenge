package org.curbee.auditsystem;

public class PropertyUpdate<T> implements ChangeType {

    private final String property;

    private final T previous;

    private final T current;


    public PropertyUpdate(String propertyPath, T previous, T current) {
        this.property = propertyPath;
        this.previous = previous;
        this.current = current;
    }

    public T getPrevious() {
        return previous;
    }

    public T getCurrent() {
        return current;
    }


    @Override
    public String getProperty() {
        return property;
    }

    @Override
    public String toString() {
        return String.format("{\"property\": \"%s\", \"previous\": \"%s\", \"current\": \"%s\"}", property, previous, current);
    }
}
