package tech.wundero.placeholderredux.util;

import com.google.common.collect.Iterators;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;

import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class TextUtils {

    public static Iterable<TextCharacter> iterate(Text on) {
        final Text ont = on;
        return () -> ont.getChildren().stream().map(TextUtils::iterate).map(Iterable::iterator).reduce(new TextIter(ont), Iterators::concat);
    }

    public static String plain(Text t) {
        // return t.toPlainSingle();
        if (t instanceof LiteralText) {
            return ((LiteralText)t).getContent();
        }
        return "";
    }

    public static String plainr(Text t) {
        // return t.toPlainSingle();
        return t.getChildren().stream().map(TextUtils::plainr).reduce(plain(t), String::concat);
    }

    private static class TextIter implements Iterator<TextCharacter> {

        private final Text t;
        private final String plain;
        private int cur;

        public TextIter(Text t) {
            this.t = t;
            cur = 0;
            plain = plain(t);
        }

        @Override
        public boolean hasNext() {
            return cur < plain.length();
        }

        @Override
        public TextCharacter next() {
            return new TextCharacter(plain.charAt(cur++), t);
        }
    }

}
