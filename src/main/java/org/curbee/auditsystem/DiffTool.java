package org.curbee.auditsystem;

import org.apache.commons.lang3.ClassUtils;
import org.curbee.auditsystem.exception.AuditException;
import org.curbee.auditsystem.exception.TypeMismatchException;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class DiffTool<T> {

    public List<ChangeType> diff(T previous, T current) {
        if (previous == null && current == null) return new ArrayList<>();

        if (previous != null && current != null && !previous.getClass().equals(current.getClass())) {
            throw new TypeMismatchException("The provided objects are of different types.");
        }
        return getDifferences(previous, current, "");
    }

    private List<ChangeType> getDifferences(T prevObj, T currObj, String path) {
        List<ChangeType> diffs = new ArrayList<>();

        for (Field field : prevObj.getClass().getDeclaredFields()) {
            T prevValue;
            T currValue;
            try {
                prevValue = (T) field.get(prevObj);
                currValue = (T) field.get(currObj);
            } catch (IllegalAccessException e) {
                throw new AuditException("Failed to access field because of: " + e.getMessage(), e);
            }

            String currentPath = path.isEmpty() ? field.getName() : path + "." + field.getName();

            if (prevValue == null && currValue == null) continue;
            if (prevValue == null || !prevValue.equals(currValue)) {
                assert prevValue != null;
                if (isPrimitiveOrString(prevValue) || isPrimitiveOrString(currValue)) {
                    diffs.add(new PropertyUpdate<>(currentPath, prevValue, currValue));
                } else if (field.getType().isAssignableFrom(List.class)) {
                    List<T> prevList = (List<T>) prevValue;
                    List<T> currList = (List<T>) currValue;
                    diffs.addAll(processListDifferences(prevList, currList, currentPath));
                } else {
                    diffs.addAll(getDifferences(prevValue, currValue, currentPath));
                }
            }
        }

        return diffs;
    }

    private List<ChangeType> processListDifferences(List<T> prevList, List<T> currList, String path) {
        List<ChangeType> diffs = new ArrayList<>();
        if (prevList == null) prevList = new ArrayList<>();
        if (currList == null) currList = new ArrayList<>();

        if (prevList.isEmpty() || currList.isEmpty() || (isPrimitiveOrString(prevList.get(0)) && isPrimitiveOrString(currList.get(0)))) {
            List<T> added = new ArrayList<>(currList);
            added.removeAll(prevList);

            List<T> removed = new ArrayList<>(prevList);
            removed.removeAll(currList);

            if (!added.isEmpty() || !removed.isEmpty()) {
                diffs.add(new ListUpdate<>(path, added, removed));
            }
        } else {
            Field keyField = null;
            for (Field field : prevList.get(0).getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(AuditKey.class) || "id".equals(field.getName())) {
                    keyField = field;
                    break;
                }
            }
            if (keyField == null) {
                throw new AuditException("The audit system lacks the information it needs to determine what has changed.");
            }

            // Use a map to easily determine added/removed/updated objects
            Map<Object, T> prevMap = getObjectKeyMap(prevList, keyField);
            Map<Object, T> currMap = getObjectKeyMap(currList, keyField);

            for (Map.Entry<Object, T>  entry: prevMap.entrySet()) {
                Object id = entry.getKey();
                if (!currMap.containsKey(id)) {
                    // Object was removed
                    diffs.add(new PropertyUpdate<>(path + "[" + id + "]", entry.getValue(), null));
                } else {
                    // Check for differences within the object
                    T prevItem = prevMap.get(id);
                    T currItem = currMap.get(id);
                    diffs.addAll(getDifferences(prevItem, currItem, path + "[" + id + "]"));
                }
            }

            for (Map.Entry<Object, T>  entry: currMap.entrySet()) {
                if (!prevMap.containsKey(entry.getKey())) {
                    // Object was added
                    diffs.add(new PropertyUpdate<>(path + "[" + entry.getKey() + "]", null, entry.getValue()));
                }
            }
        }

        return diffs;
    }

    private Map<Object, T> getObjectKeyMap(List<T> currList, Field keyField) {
        return currList.stream().collect(Collectors.toMap(item -> {
            try {
                return keyField.get(item);
            } catch (IllegalAccessException e) {
                throw new AuditException("Failed to access field because of: " + e.getMessage(), e);
            }
        }, item -> item));
    }

    private boolean isPrimitiveOrString(T obj) {
        return ClassUtils.isPrimitiveWrapper(obj.getClass()) || obj.getClass() == String.class;
    }
}

