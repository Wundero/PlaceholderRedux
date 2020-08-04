package tech.wundero.placeholderredux.util;

import org.spongepowered.api.util.Tuple;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Objects;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

// NOTE: not thread safe
public class FluidMatcher {

    private Matcher m;
    private String og;
    private int cursor = 0;
    private boolean cached = false;
    private final Queue<Tuple<String, Boolean>> cache = new ArrayDeque<>();

    public FluidMatcher(Pattern p, String str) {
        this.og = str;
        m = p.matcher(str);
    }

    private void cacheParts() {
        while (!produceOne()){
        }
        cached = true;
    }

    private boolean produceOne() {
        if (cursor == og.length()) {
            return true;
        }
        boolean res = m.find(cursor);
        if (!res) {
            if (cursor < og.length()) {
                cache.offer(Tuple.of(og.substring(cursor), false));
            }
            cursor = og.length();
            return true;
        }
        int rb = m.start(), re = m.end();
        if (rb - cursor > 0) {
            cache.offer(Tuple.of(og.substring(cursor, rb), false));
        }
        cache.offer(Tuple.of(og.substring(rb, re), true));
        cursor = re;
        return false;
    }

    private boolean hasMore(Tuple<String, Boolean> t) {
        if (!cache.isEmpty()) {
            return true;
        }
        if (m.find(cursor)) {
            return true;
        }
        return cursor < og.length() || t != null;
    }

    private Tuple<String, Boolean> retrieveOne(Tuple<String, Boolean> t) {
        if (cache.isEmpty()) {
            produceOne();
        }
        return cache.poll();
    }

    public void reset() {
        cursor = 0;
    }

    public void reset(String to) {
        this.og = to;
        this.m = m.pattern().matcher(og);
    }

    public void cache() {
        cacheParts();
    }

    public Stream<String> streamMatches() {
        return streamAllParts().filter(Tuple::getSecond).map(Tuple::getFirst);
    }

    public Stream<Tuple<String, Boolean>> streamAllParts() {
        if (cached) {
            return cache.stream();
        }
        produceOne();
        return TypeUtils.iterate(cache.poll(), this::hasMore, this::retrieveOne).filter(Objects::nonNull).onClose(this::reset);
    }

}
