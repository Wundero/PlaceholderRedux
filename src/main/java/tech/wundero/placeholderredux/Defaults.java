package tech.wundero.placeholderredux;

import tech.wundero.placeholderredux.api.Placeholder;
import tech.wundero.placeholderredux.api.PlaceholderArgs;
import tech.wundero.placeholderredux.api.PlaceholderBuilder;
import tech.wundero.placeholderredux.api.annotations.*;
import tech.wundero.placeholderredux.api.data.PlaceholderData;
import tech.wundero.placeholderredux.api.data.PlaceholderDataBuilder;
import tech.wundero.placeholderredux.api.exceptions.PlaceholderException;
import tech.wundero.placeholderredux.api.function.FunctionArg;
import tech.wundero.placeholderredux.api.service.PlaceholderGeneratorService;
import tech.wundero.placeholderredux.api.service.PlaceholderService;
import tech.wundero.placeholderredux.function.FunctionArgs;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashMap;

public class Defaults {

    private Defaults() {

    }

    Defaults(PlaceholderService service) {
        genAndReg(service);
    }

    private void genAndReg(PlaceholderService svc) {
        svc.generateAndRegister(this);
        if (svc.generator().isPresent()) {
            PlaceholderGeneratorService gen = svc.generator().get();
            PlaceholderBuilder<String, ? extends PlaceholderBuilder<String, ?>> builder = gen.<String>builder()
                    .placeholderFunction(this::playerPlaceholder);
            builder.meta()
                    .authors("Wundero")
                    .id("player")
                    .description("")
                    .expectedArgumentTypes(new HashMap<String, FunctionArg<?>>(){{
                        put("type", FunctionArgs.choices("", ""));
                    }})
//                    .orderedArgumentNames(Arrays.asList("type"))
                    .build();
            svc.registry().ifPresent(r -> r.register(builder.build()));
        }
    }

    public String playerPlaceholder(Object source, Object target, PlaceholderArgs args) {
        return "";
    }

    @Async
    @Replacer("dtest_1")
    public String test() {
        return "test 1";
    }

    @Async
    @Replacer("dtest_2")
    public String test_2(@Source int srcint, @Arg("arg") String arg, @Target String targetName, @Arg("inttest") int intt) {
        return "Test 2 target=" + targetName + ", arg=" + arg + ", inttest=" + intt + ", srcint="+srcint;
    }
}
