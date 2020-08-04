package tech.wundero.placeholderredux.util;

import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import org.spongepowered.api.text.format.*;
import org.spongepowered.api.util.Tuple;
import tech.wundero.placeholderredux.api.function.FunctionArg;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class TypeUtils {

    /**
     * Copied from Stream class to use in java 8.
     * <p>
     * Returns a sequential ordered {@code Stream} produced by iterative
     * application of the given {@code next} function to an initial element,
     * conditioned on satisfying the given {@code hasNext} predicate.  The
     * stream terminates as soon as the {@code hasNext} predicate returns false.
     *
     * <p>{@code Stream.iterate} should produce the same sequence of elements as
     * produced by the corresponding for-loop:
     * <pre>{@code
     *     for (T index=seed; hasNext.test(index); index = next.apply(index)) {
     *         ...
     *     }
     * }</pre>
     *
     * <p>The resulting sequence may be empty if the {@code hasNext} predicate
     * does not hold on the seed value.  Otherwise the first element will be the
     * supplied {@code seed} value, the next element (if present) will be the
     * result of applying the {@code next} function to the {@code seed} value,
     * and so on iteratively until the {@code hasNext} predicate indicates that
     * the stream should terminate.
     *
     * <p>The action of applying the {@code hasNext} predicate to an element
     * <a href="../concurrent/package-summary.html#MemoryVisibility"><i>happens-before</i></a>
     * the action of applying the {@code next} function to that element.  The
     * action of applying the {@code next} function for one element
     * <i>happens-before</i> the action of applying the {@code hasNext}
     * predicate for subsequent elements.  For any given element an action may
     * be performed in whatever thread the library chooses.
     *
     * @param <T>     the type of stream elements
     * @param seed    the initial element
     * @param hasNext a predicate to apply to elements to determine when the
     *                stream must terminate.
     * @param next    a function to be applied to the previous element to produce
     *                a new element
     * @return a new sequential {@code Stream}
     */
    public static <T> Stream<T> iterate(T seed, Predicate<? super T> hasNext, UnaryOperator<T> next) {
        Objects.requireNonNull(next);
        Objects.requireNonNull(hasNext);
        Spliterator<T> spliterator = new Spliterators.AbstractSpliterator<T>(Long.MAX_VALUE,
                Spliterator.ORDERED | Spliterator.IMMUTABLE) {
            T prev;
            boolean started, finished;

            @Override
            public boolean tryAdvance(Consumer<? super T> action) {
                Objects.requireNonNull(action);
                if (finished)
                    return false;
                T t;
                if (started)
                    t = next.apply(prev);
                else {
                    t = seed;
                    started = true;
                }
                if (!hasNext.test(t)) {
                    prev = null;
                    finished = true;
                    return false;
                }
                action.accept(prev = t);
                return true;
            }

            @Override
            public void forEachRemaining(Consumer<? super T> action) {
                Objects.requireNonNull(action);
                if (finished)
                    return;
                finished = true;
                T t = started ? next.apply(prev) : seed;
                prev = null;
                while (hasNext.test(t)) {
                    action.accept(t);
                    t = next.apply(t);
                }

            }
        };
        return StreamSupport.stream(spliterator, false);
    }

    public static <T> Class<T> getArrayClass(T[] arr) {
        return (Class<T>) arr.getClass().getComponentType();
    }

    private static final List<Tuple<Class<?>, Function<String, ?>>> parsers = new ArrayList<>();
    private static final ReadWriteLock lock = new ReentrantReadWriteLock();

    public static <T> boolean hasParser(Class<? extends T> clazz) {
        Objects.requireNonNull(clazz);
        return indexOf(clazz) >= 0;
    }


    public static <T> Function<String, ? extends T> getParser(Class<? extends T> type) {
        try {
            lock.readLock().lock();
            return (Function<String, ? extends T>) parsers.get(indexOf(type)).getSecond();
        } finally {
            lock.readLock().unlock();
        }
    }

    private static <T> int indexOf(Class<?> type) {
        Objects.requireNonNull(type);
        try {
            lock.readLock().lock();
            for (int i = 0; i < parsers.size(); i++) {
                Tuple<Class<?>, Function<String, ?>> parser = parsers.get(i);
                if (type.equals(parser.getFirst())) { // exact first, prefer specialized parsers
                    return i;
                }
            }
            for (int i = 0; i < parsers.size(); i++) {
                Tuple<Class<?>, Function<String, ?>> parser = parsers.get(i);
                if (type.isAssignableFrom(parser.getFirst())) { // subtypes second
                    return i;
                }
            }
            return -1;
        } finally {
            lock.readLock().unlock();
        }
    }

    public static <T> boolean addParser(Class<? extends T> clazz, Function<String, ? extends T> parser) {
        if (hasParser(clazz)) {
            return false;
        }
        try {
            lock.writeLock().lock();
            return parsers.add(Tuple.of(clazz, parser));
        } finally {
            lock.writeLock().unlock();
        }
    }

    static {
        lock.writeLock().lock();
        addParser(Integer.class, Integer::valueOf);
        addParser(int.class, Integer::valueOf);
        addParser(Long.class, Long::valueOf);
        addParser(long.class, Long::valueOf);
        addParser(Short.class, Short::valueOf);
        addParser(short.class, Short::valueOf);
        addParser(Byte.class, Byte::valueOf);
        addParser(byte.class, Byte::valueOf);
        addParser(Double.class, Double::valueOf);
        addParser(double.class, Double::valueOf);
        addParser(Float.class, Float::valueOf);
        addParser(float.class, Float::valueOf);
        addParser(Boolean.class, TypeUtils::booleanOf);
        addParser(boolean.class, TypeUtils::booleanOf);
        addParser(Character.class, s -> s.charAt(0));
        addParser(char.class, s -> s.charAt(0));
        addParser(String.class, s -> s);
        addParser(Number.class, Double::valueOf);
        addParser(TextColor.class, TypeUtils::colorString);
        addParser(TextStyle.class, TypeUtils::styleString);
        addParser(TextFormat.class, TypeUtils::fmtString);
        lock.writeLock().unlock(); // probably TOTALLY unnecessary but hey
    }

    public static <T> Optional<T> tryParse(String og, FunctionArg<T> to) {
        try {
            T v = getParser(to.type()).apply(og);
            if (to.isValid(v)) {
                return Optional.of(v);
            }
        } catch (Exception ignored) {
        }
        return Optional.empty();
    }

    public static <T> T tryParseUnsafe(String og, FunctionArg<T> to) {
        try {
            T v = getParser(to.type()).apply(og);
            if (to.isValid(v)) {
                return v;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static boolean booleanOf(String value) {
        switch (value.toLowerCase().trim()) {
            case "y":
            case "yes":
            case "true":
            case "t":
            case "1":
                return true;
        }
        return false;
    }

    private static TextFormat fmtString(String fmt) {
        if (!fmt.contains("&")) {
            return TextFormat.NONE;
        }
        return new FluidMatcher(Pattern.compile("(&[0-9a-fA-F])"), fmt).streamMatches().reduce(TextFormat.of(), (fm, str) -> {
            TextStyle mstyle = styleString(str);
            TextColor mcol = colorString(str);
            if (mcol.equals(TextColors.NONE)) {
                if (mstyle.equals(TextStyles.NONE)) {
                    return fm;
                }
                return fm.merge(TextFormat.of(mstyle));
            } else {
                if (mstyle.equals(TextStyles.NONE)) {
                    return fm.merge(TextFormat.of(mcol));
                }
                return fm.merge(TextFormat.of(mcol, mstyle));
            }
        }, TextFormat::merge);
    }

    private static TextStyle styleString(String color) {
        switch (color.toLowerCase().trim()) {
            case "bold":
            case "&l":
                return TextStyles.BOLD;
            case "italic":
            case "&o":
                return TextStyles.ITALIC;
            case "random":
            case "obfuscated":
            case "magic":
            case "&k":
                return TextStyles.OBFUSCATED;
            case "strike":
            case "strikethru":
            case "strikethrough":
            case "&m":
                return TextStyles.STRIKETHROUGH;
            case "reset":
            case "&r":
                return TextStyles.RESET;
            case "underline":
            case "&n":
                return TextStyles.UNDERLINE;
            default:
                return TextStyles.NONE;
        }
    }

    private static TextColor colorString(String color) {
        switch (color.toLowerCase().trim()) {
            case "blue":
            case "&9":
                return TextColors.BLUE;
            case "dark_blue":
            case "dark blue":
            case "&1":
                return TextColors.DARK_BLUE;
            case "dark red":
            case "dark_red":
            case "&4":
                return TextColors.DARK_RED;
            case "red":
            case "&c":
                return TextColors.RED;
            case "reset":
            case "&r":
                return TextColors.RESET;
            case "gold":
            case "&6":
                return TextColors.GOLD;
            case "yellow":
            case "&e":
                return TextColors.YELLOW;
            case "dark_green":
            case "dark green":
            case "&2":
                return TextColors.DARK_GREEN;
            case "green":
            case "lime":
            case "&a":
                return TextColors.GREEN;
            case "aqua":
            case "&b":
                return TextColors.AQUA;
            case "dark_aqua":
            case "dark aqua":
            case "&3":
                return TextColors.DARK_AQUA;
            case "light_purple":
            case "light purple":
            case "pink":
            case "%d":
                return TextColors.LIGHT_PURPLE;
            case "dark_purple":
            case "dark purple":
            case "purple":
            case "magenta":
            case "&5":
                return TextColors.DARK_PURPLE;
            case "white":
            case "&f":
                return TextColors.WHITE;
            case "gray":
            case "grey":
            case "&7":
                return TextColors.GRAY;
            case "dark_grey":
            case "dark_gray":
            case "dark gray":
            case "dark grey":
            case "&8":
                return TextColors.DARK_GRAY;
            case "black":
            case "&0":
                return TextColors.BLACK;
            default:
                return TextColors.NONE;
        }
    }


}
