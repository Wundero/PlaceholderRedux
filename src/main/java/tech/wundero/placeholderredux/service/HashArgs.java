package tech.wundero.placeholderredux.service;

import tech.wundero.placeholderredux.api.PlaceholderArgs;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HashArgs implements PlaceholderArgs {

    private final Map<String, Object> delegate = new ConcurrentHashMap<>();

    @Override
    public Optional<Object> getArg(String value) {
        return Optional.ofNullable(delegate.get(value));
    }

    public void removeArg(String id) {
        delegate.remove(id);
    }

    public void putArg(String id, Object value) {
        delegate.put(id, value);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    public Map<String, Object> getArgs() {
        return delegate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HashArgs hashArgs = (HashArgs) o;
        return Objects.equals(delegate, hashArgs.delegate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(delegate);
    }
}
