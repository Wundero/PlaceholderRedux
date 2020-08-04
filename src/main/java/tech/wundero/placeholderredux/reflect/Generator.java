package tech.wundero.placeholderredux.reflect;

import com.google.common.base.MoreObjects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.joor.Reflect;
import org.spongepowered.api.util.Tuple;
import tech.wundero.placeholderredux.api.Placeholder;
import tech.wundero.placeholderredux.api.PlaceholderArgs;
import tech.wundero.placeholderredux.api.PlaceholderBuilder;
import tech.wundero.placeholderredux.api.function.FunctionArg;
import tech.wundero.placeholderredux.api.function.PlaceholderFunction;
import tech.wundero.placeholderredux.api.annotations.Async;
import tech.wundero.placeholderredux.api.annotations.Info;
import tech.wundero.placeholderredux.api.annotations.Replacer;
import tech.wundero.placeholderredux.api.data.PlaceholderData;
import tech.wundero.placeholderredux.api.data.PlaceholderDataBuilder;
import tech.wundero.placeholderredux.api.exceptions.PlaceholderException;
import tech.wundero.placeholderredux.data.Meta;
import tech.wundero.placeholderredux.data.MetaBuilder;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Generator {

    private static AtomicInteger id = new AtomicInteger(0);

    private static final String PACKAGE = "tech.wundero.placeholderredux.gen";

    private static String freshName(String name) {
        return name + "_" + id.getAndIncrement();
    }

    private static Class<?> box(Class<?> prim) {
        if (prim.equals(int.class)) {
            return Integer.class;
        }
        if (prim.equals(double.class)) {
            return Double.class;
        }
        if (prim.equals(float.class)) {
            return Float.class;
        }
        if (prim.equals(long.class)) {
            return Long.class;
        }
        if (prim.equals(boolean.class)) {
            return Boolean.class;
        }
        if (prim.equals(short.class)) {
            return Short.class;
        }
        if (prim.equals(byte.class)) {
            return Byte.class;
        }
        if (prim.equals(char.class)) {
            return Character.class;
        }
        return prim;
    }

    static String sanitizeArrays(Class<?> arrclazz, boolean box) {
        if (arrclazz.isArray()) {
            return sanitizeArrays(arrclazz.getComponentType(), box) + "[]";
        } else {
            if (box) {
                arrclazz = box(arrclazz);
            }
            return arrclazz.getName();
        }
    }

    private static final LoadingCache<Tuple<Tuple<Object, String>, Optional<Analyzer>>, PlaceholderBuilder<?, ? extends PlaceholderBuilder<?, ?>>> placeholderCache =
            CacheBuilder.newBuilder().concurrencyLevel(1).weakValues().build(new CacheLoader<Tuple<Tuple<Object, String>, Optional<Analyzer>>, PlaceholderBuilder<?, ? extends PlaceholderBuilder<?, ?>>>() {
                @Override
                public PlaceholderBuilder<?, ? extends PlaceholderBuilder<?, ?>> load(@Nonnull Tuple<Tuple<Object, String>, Optional<Analyzer>> key) throws Exception {
                    Analyzer an = key.getSecond().orElseGet(() -> new Analyzer(key.getFirst().getFirst(), key.getFirst().getSecond()));
                    return generate(key.getFirst().getFirst(), key.getFirst().getSecond(), an);
                }
            });

    private static Stream<Method> getViableMethods(Object o) {
        return Arrays.stream(o.getClass().getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .filter(m -> m.isAnnotationPresent(Replacer.class));
    }

    public static Stream<PlaceholderBuilder<?, ? extends PlaceholderBuilder<?, ?>>> createAll(Object o) {
        return getViableMethods(o)
                .map(m -> {
                    Replacer r = m.getAnnotation(Replacer.class);
                    Analyzer an = new Analyzer(o, m, r.value());
                    try {
                        return placeholderCache.get(new Tuple<>(new Tuple<>(o, r.value()), Optional.of(an)));
                    } catch (ExecutionException ignored) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .map(xo -> (PlaceholderBuilder<?, ? extends PlaceholderBuilder<?, ?>>) xo);

//
//        Map<String, Placeholder<?>> placeholders = new HashMap<>();
//        for (Method m : o.getClass().getDeclaredMethods()) {
//            int mod = m.getModifiers();
//            if (!Modifier.isPublic(mod)) {
//                continue;
//            }
//            if (!m.isAnnotationPresent(Replacer.class)) {
//                continue;
//            }
//            Replacer r = m.getAnnotation(Replacer.class);
//            Analyzer an = new Analyzer(o, m, r.value());
//            try {
//                placeholders.put(r.value(), placeholderCache.get(new Tuple<>(new Tuple<>(o, r.value()), Optional.of(an))));
//            } catch (ExecutionException ignored) {
//            }
//        }
//        return placeholders;
    }

    public static PlaceholderBuilder<?, ? extends PlaceholderBuilder<?, ?>> create(Object o, String placeholderID) throws Exception {
        return placeholderCache.get(new Tuple<>(new Tuple<>(o, placeholderID), Optional.empty()));
    }

    private static PlaceholderBuilder<?, ? extends PlaceholderBuilder<?, ?>> generate(Object o, String placeholderID, Analyzer an) {
        if (!an.valid()) {
            return null;
        }
        String name = freshName(o.getClass().getSimpleName() + "_" + placeholderID);
        String rtclname = sanitizeArrays(an.ret(), an.ret().isPrimitive());
        String clazz = classTemplate().replace("{0}", name).replace("{1}", rtclname).replace("{3}", o.getClass().getName());
        String ctemp = call(o, an);
        Optional<GenArg> src = an.source();
        Optional<GenArg> tar = an.target();
        Map<String, GenArg> args = an.args();
        List<Tuple<String, Integer>> orderedArgNames = new ArrayList<>();
        List<String> transformers = new ArrayList<>();
        int oskip = 0;
        if (src.isPresent()) {
            oskip++;
            Tuple<String, Integer> s;
            Tuple<String, String> tr = Transformer.transformSource(src.get());
            s = new Tuple<>(tr.getSecond(), an.srcind());
            transformers.add(tr.getFirst());
            orderedArgNames.add(s);
        }
        if (tar.isPresent()) {
            oskip++;
            Tuple<String, Integer> s;
            Tuple<String, String> tr = Transformer.transformTarget(tar.get());
            s = new Tuple<>(tr.getSecond(), an.trgind());
            transformers.add(tr.getFirst());
            orderedArgNames.add(s);
        }
        Map<String, Tuple<String, String>> trargs = Transformer.transformArgs(args);
        trargs.forEach((k, v) -> {
            transformers.add(v.getFirst());
            orderedArgNames.add(new Tuple<>(v.getSecond(), args.get(k).getPos()));
        });
        Map<String, FunctionArg<?>> types = new HashMap<>();
        args.forEach((n, t) -> {
            types.put(n, t.getFA());
        });
        List<String> typelst = orderedArgNames.stream().skip(oskip).sorted(Comparator.comparingInt(Tuple::getSecond)).map(Tuple::getFirst).collect(Collectors.toList());
        orderedArgNames.sort(Comparator.comparingInt(Tuple::getSecond));
        ctemp = ctemp.replace("{4}", orderedArgNames.stream().map(Tuple::getFirst).collect(Collectors.joining(", ")));
        transformers.add(ctemp);
        clazz = clazz.replace("{2}", transformers.stream().map(s -> s.replace("\n", "\n        ")).collect(Collectors.joining("")));
        clazz = clazz.replace("$", ".");

        /**
         * TODO do the multiple placeholder generation thingy
         */


        MetaBuilder mb = Meta.builder().id(placeholderID).expectedArgumentTypes(types)/*.orderedArgumentNames(typelst)*/;
        if (an.getMethod().isAnnotationPresent(Info.class)) {
            Info inf = an.getMethod().getAnnotation(Info.class);
            mb = mb.description(inf.description()).url(inf.url().isEmpty() ? null : inf.url()).authors(inf.authors());
        } else {
            mb = mb.description(null).authors(new ArrayList<>());
        }
        if (!an.getMethod().isAnnotationPresent(Async.class)) {
            String code = "if (!org.spongepowered.api.Sponge.isServerAvailable() || !org.spongepowered.api.Sponge.getServer().isMainThread()) {\n" +
                    "            throw new tech.wundero.placeholderredux.api.exceptions.PlaceholderException(\"Synchronous placeholder must be run on main thread!\");\n" +
                    "        }\n";
            clazz = clazz.replace("{5}", code);
        } else {
            clazz = clazz.replace("{5}", "\n");
        }
        System.out.println(clazz);
        PlaceholderFunction<Object> plf = Reflect.compile(PACKAGE + "." + name, clazz).create(o).get();
        GB<?> b = new GB<>().placeholderFunction(plf);
        b.mb = mb;
        return b;
    }

    private static final class GP<T> implements Placeholder<T> {
        private final PlaceholderFunction<T> fn;
        private final Meta m;

        GP(PlaceholderFunction<T> fn, Meta m) {
            this.m = m;
            this.fn = fn;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Placeholder)) {
                return false;
            }
            Placeholder<?> p = (Placeholder<?>) o;
            return p.id().equals(this.id());
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this).add("id", id()).toString();
        }

        @Override
        public int hashCode() {
            return Objects.hash(id());
        }

        @Nonnull
        @Override
        public PlaceholderData data() {
            return m;
        }

        @Override
        public T parse(Object source, Object target, PlaceholderArgs args) throws PlaceholderException {
            return fn.parse(source, target, args);
        }
    }

    private static final class GB<T> implements PlaceholderBuilder<T, GB<T>> {

        private MetaBuilder mb;
        private PlaceholderFunction<T> fn;

        @Override
        public Placeholder<T> build() {
            if (fn == null) {
                throw new IllegalArgumentException("Function cannot be null!");
            }
            return new GP<>(fn, mb.build());
        }

        @Override
        public GB<T> reset() {
            mb = Meta.builder();
            fn = null;
            return this;
        }

        @Override
        public PlaceholderDataBuilder<? extends PlaceholderData, ? extends PlaceholderDataBuilder<?, ?>> meta() {
            return mb;
        }

        @Override
        public GB<T> placeholderFunction(PlaceholderFunction<T> function) {
            this.fn = function;
            return this;
        }
    }


    private static String call(Object o, Analyzer a) {
        String out = "return ";
        if (a.isStatic()) {
            out += o.getClass().getName();
        } else {
            out += "this.src";
        }
        out += "." + a.getName() + "({4}";
        out += ");";
        return out;
    }

    private static String classTemplate() {
        return "package " + PACKAGE + ";\n\n" +
                "class {0} implements tech.wundero.placeholderredux.api.function.PlaceholderFunction<{1}> {\n\n" +
                "" +
                "    private {3} src;\n\n" +
                "" +
                "    public {0}({3} src) {\n" +
                "        this.src = src;\n" +
                "    }\n\n" +
                "" +
                "    public {1} parse(Object source, Object target, tech.wundero.placeholderredux.api.PlaceholderArgs args)" +
                " throws tech.wundero.placeholderredux.api.exceptions.PlaceholderException {\n" +
                "        {5}" +
                "        {2}" +
                "\n    }\n" +
                "}";
    }

}
