package tech.wundero.placeholderredux;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.util.Tuple;
import tech.wundero.placeholderredux.api.exceptions.MissingServiceException;
import tech.wundero.placeholderredux.api.exceptions.PlaceholderException;
import tech.wundero.placeholderredux.api.format.PlaceholderFormat;
import tech.wundero.placeholderredux.api.function.FunctionArg;
import tech.wundero.placeholderredux.format.PlaceholderFormats;
import tech.wundero.placeholderredux.api.service.PlaceholderGeneratorService;
import tech.wundero.placeholderredux.api.service.PlaceholderRegisterService;
import tech.wundero.placeholderredux.api.service.PlaceholderReplacerService;
import tech.wundero.placeholderredux.api.service.PlaceholderService;
import tech.wundero.placeholderredux.format.JsonFormat;
import tech.wundero.placeholderredux.function.FunctionArgs;
import tech.wundero.placeholderredux.service.GeneratorService;
import tech.wundero.placeholderredux.service.RegistryService;
import tech.wundero.placeholderredux.util.TextUtils;
import tech.wundero.placeholderredux.util.TypeUtils;
import tech.wundero.placeholderredux.util.Utils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Plugin(id = "placeholderredux", name = "Placeholder Redux", version = "0.1.0")
public class PlaceholderRedux {

    private static PlaceholderReplacerService REPLACER_INST;
    private static PlaceholderGeneratorService GENERATOR_INST;
    private static PlaceholderRegisterService REGISTER_INST;
    private static PlaceholderService SERVICE_INST;

    private static PlaceholderRedux INST = null;

    private static boolean verbose = true;

    public static boolean isVerbose() {
        return verbose;
    }

    public static PlaceholderService uncheckedService() {
        try {
            return getService();
        } catch (PlaceholderException e) {
            return null;
        }
    }

    public static PlaceholderService getService() throws PlaceholderException {
        if (SERVICE_INST == null) {
            SERVICE_INST = Sponge.getServiceManager().provide(PlaceholderService.class).orElseThrow(() -> new MissingServiceException("Placeholder service is missing!"));
        }
        return SERVICE_INST;
    }

    public static PlaceholderGeneratorService uncheckedGenerator() {
        try {
            return getGenerator();
        } catch (PlaceholderException e) {
            return null;
        }
    }

    public static PlaceholderGeneratorService getGenerator() throws PlaceholderException {
        if (GENERATOR_INST == null) {
            GENERATOR_INST = Sponge.getServiceManager().provide(PlaceholderGeneratorService.class).orElseThrow(() -> new MissingServiceException("Generator is missing!"));
        }
        return GENERATOR_INST;
    }

    public static PlaceholderRegisterService uncheckedRegistrar() {
        try {
            return getRegistrar();
        } catch (PlaceholderException e) {
            return null;
        }
    }

    public static PlaceholderRegisterService getRegistrar() throws PlaceholderException {
        if (REGISTER_INST == null) {
            REGISTER_INST = Sponge.getServiceManager().provide(PlaceholderRegisterService.class).orElseThrow(() -> new MissingServiceException("Registrar is missing!"));
        }
        return REGISTER_INST;
    }

    public static PlaceholderReplacerService uncheckedReplacer() {
        try {
            return getReplacer();
        } catch (PlaceholderException e) {
            return null;
        }
    }

    public static PlaceholderReplacerService getReplacer() throws PlaceholderException {
        if (REPLACER_INST == null) {
            REPLACER_INST = Sponge.getServiceManager().provide(PlaceholderReplacerService.class).orElseThrow(() -> new MissingServiceException("Replacer is missing!"));
        }
        return REPLACER_INST;
    }

    public static PlaceholderRedux get() {
        return INST;
    }

    @Listener
    public void init(GameInitializationEvent e) {
        INST = this;
        Sponge.getServiceManager().setProvider(this, PlaceholderRegisterService.class, new RegistryService());
        Sponge.getServiceManager().setProvider(this, PlaceholderGeneratorService.class, new GeneratorService());
        Sponge.getServiceManager().setProvider(this, PlaceholderReplacerService.class, new PlaceholderReplacerService() {
        });
        Sponge.getServiceManager().setProvider(this, PlaceholderService.class, new PlaceholderService() {
        });
        Sponge.getRegistry().registerModule(PlaceholderFormat.class, PlaceholderFormats.VALUES); // TODO verify
    }

    @Listener
    public void setDefaults(GameStartingServerEvent e) {
        if (INST == null) {
            INST = this;
        }
        new Defaults(Sponge.getServiceManager().provideUnchecked(PlaceholderService.class));
    }


}
