package tech.wundero.placeholderredux.service;

import tech.wundero.placeholderredux.api.Placeholder;
import tech.wundero.placeholderredux.api.service.PlaceholderRegisterService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

public class RegistryService implements PlaceholderRegisterService {

    private final Map<String, Placeholder<?>> store = new ConcurrentHashMap<>();

    private static final Pattern ID_PATTERN = Pattern.compile("(([a-zA-Z0-9][a-zA-Z0-9\\-_]*[a-zA-Z0-9])|[a-zA-Z0-9])");

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public static boolean matchesIDPattern(String text) {
        return ID_PATTERN.matcher(text).matches();
    }

    public boolean register(String id, Placeholder<?> placeholder) {
        if (!id.equals(placeholder.data().placeholderID())) {
            return false;
        }
        if (!matchesIDPattern(id)) {
            return false;
        }
        for (String orderedArgumentName : placeholder.data().expectedArgumentTypes().keySet()) {
            if (!matchesIDPattern(orderedArgumentName)) {
                return false;
            }
        }
        lock.readLock().lock();
        if (store.containsKey(id)) {
            lock.readLock().unlock();
            return false;
        }
        lock.readLock().unlock();
        lock.writeLock().lock();
        store.put(id, placeholder);
        lock.writeLock().unlock();
        return true;
    }

    @Override
    public boolean register(Placeholder<?> placeholder) {
        return register(placeholder.id(), placeholder);
    }

    @Override
    public Set<Placeholder<?>> getPlaceholders() {
        lock.readLock().lock();
        try {
            return new HashSet<>(store.values());
        } finally {
            lock.readLock().unlock();
        }
    }


}
